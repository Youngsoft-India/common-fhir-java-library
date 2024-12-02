package org.ysifhir;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ysifhir.utils.TypeConverterUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class FHIRConverter {

    private static final Logger logger = LoggerFactory.getLogger(FHIRConverter.class);
    private Map<String, Object> config;

    public FHIRConverter(String jsonConfig) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.readValue(jsonConfig, Map.class);
    }

        public String toFHIR(Object sourceObj) throws Exception {


            // Dynamically load classes
            String sourceClassName = (String) config.get("clientClass");
            String targetClassName = (String) config.get("fhirMapperClass");

            String targetClazz = "org.hl7.fhir.dstu3.model." + targetClassName ;

            Class<?> sourceClass = Class.forName(sourceClassName);
            Class<?> targetClass =  Class.forName(targetClazz); // Cast to the correct target type

            logger.info("Converting clas "+ sourceClassName + " to "+ targetClassName);

            // Create target object dynamically
            Object targetObj = targetClass.getConstructor().newInstance(); // Instantiate the specific type

            // Field mappings
            List<Map<String, Object>> fieldMappings = (List<Map<String, Object>>) config.get("fieldMapping");

            for (Map<String, Object> mapping : fieldMappings) {
                String sourceFieldName = (String) mapping.get("from");
                Map<String, Object> to = (Map<String, Object>) mapping.get("to");
                List<Map<String, Object>> fields = (List<Map<String, Object>>) to.get("fields");

                String targetFieldName = (String) to.get("fieldName");
                String givenDataType = (String) to.get("dataType");

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

            // Convert FHIR object to JSON string
            FhirContext fhirContext = FhirContext.forDstu3();
            String jsonString = fhirContext.newJsonParser().encodeResourceToString((IBaseResource) targetObj);

            return jsonString;
        }
}
