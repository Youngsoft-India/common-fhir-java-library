package org.ysifhir;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ysifhir.constants.FHIRConstants;
import org.ysifhir.constants.JsonConfigConstants;
import org.ysifhir.utils.TypeConverterUtil;
import org.ysifhir.utils.helpers.ExtensionConverterHelper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * FHIRConverter class facilitates the dynamic conversion of objects
 * from one type to FHIR resources using a configurable mapping.
 * The mappings and configurations are provided in JSON format.
 */
public class FHIRConverter {

    private static final Logger logger = LoggerFactory.getLogger(FHIRConverter.class);
    private Map<String, Object> config;

    /**
     * Initializes the FHIRConverter with a JSON configuration.
     *
     * @param jsonConfig A JSON string containing configuration for mapping fields, types, and extensions.
     * @throws Exception If an error occurs while parsing the JSON configuration.
     */
    public FHIRConverter(String jsonConfig) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.readValue(jsonConfig, Map.class);
    }


    /**
     * Converts a source object to a FHIR-compliant object based on the configuration.
     *
     * @param sourceObj  The source object that needs to be converted.
     * @param resultType The desired format of the result ("JSON" or "XML").
     *                   If null or empty, the method returns the target object directly.
     * @return The converted FHIR object as an instance, JSON string, or XML string.
     * @throws Exception If an error occurs during the conversion process.
     */
    public Object toFHIR(Object sourceObj , String resultType) throws Exception {

            // Dynamically load classes
            String sourceClassName = (String) config.get(JsonConfigConstants.CLIENT_CLASS_KEY);
            String targetClassName = (String) config.get(JsonConfigConstants.TARGET_CLASS_KEY);

            String targetClazz = FHIRConstants.HAPI_MODEL_CLASS_PATH + targetClassName ;

            Class<?> sourceClass = Class.forName(sourceClassName);
            Class<?> targetClass =  Class.forName(targetClazz); // Cast to the correct target type

            logger.info("Converting clas "+ sourceClassName + " to "+ targetClassName);

            // Create target object dynamically
            Object targetObj = targetClass.getConstructor().newInstance(); // Instantiate the specific type

            // Field mappings
            List<Map<String, Object>> fieldMappings = (List<Map<String, Object>>) config.get(JsonConfigConstants.FIELDS_MAPPING_KEY);

            for (Map<String, Object> mapping : fieldMappings) {
                String sourceFieldName = (String) mapping.get(JsonConfigConstants.FROM_KEY);
                Map<String, Object> to = (Map<String, Object>) mapping.get(JsonConfigConstants.TO_KEY);
                List<Map<String, Object>> fields = (List<Map<String, Object>>) to.get(JsonConfigConstants.SUB_FIELDS_KEY);

                String targetFieldName = (String) to.get(JsonConfigConstants.TO_FIELD_NAME_KEY);
                String givenDataType = (String) to.get(JsonConfigConstants.DATA_TYPE_KEY);

                // Get field values
                Field sourceField = sourceClass.getDeclaredField(sourceFieldName);
                sourceField.setAccessible(true);
                Object sourceValue = sourceField.get(sourceObj);

                // Get target field
                Field targetField = targetClass.getDeclaredField(targetFieldName);
                targetField.setAccessible(true);

                // Handle nested fields
                Object convertedValue = TypeConverterUtil.convertValue(
                            sourceValue,
                            targetField.getType(),
                            targetFieldName,
                            givenDataType,
                            fields,
                            sourceClass,
                            sourceObj
                    );

                if(convertedValue == null){
                    logger.info("CONVERTED VALUE FOR THE FIELD NAME : " + sourceFieldName + " is NULL");
                }
                targetField.set(targetObj, convertedValue);
            }

            // if extension exists
            List<Map<String, Object>> extension = (List<Map<String, Object>>) config.get(JsonConfigConstants.EXTENSION_KEY);
            if (extension != null && !extension.isEmpty()) {
                ExtensionConverterHelper.addExtensionsDynamically((DomainResource) targetObj, extension , sourceClass , sourceObj);
            }

            // Convert FHIR object based on resultType
            FhirContext fhirContext = FhirContext.forDstu3();
            if (resultType != null && !resultType.isEmpty()) {
                if (resultType.equalsIgnoreCase(FHIRConstants.REQUIRED_JSON_TYPE)) {
                    return fhirContext.newJsonParser().encodeResourceToString((IBaseResource) targetObj);
                } else if (resultType.equalsIgnoreCase(FHIRConstants.REQUIRED_XML_TYPE)) {
                    return fhirContext.newXmlParser().encodeResourceToString((IBaseResource) targetObj);
                }
            }

            // Return target object directly if resultType is empty or null
            return targetObj;
    }
}
