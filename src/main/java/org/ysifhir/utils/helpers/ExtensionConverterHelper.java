package org.ysifhir.utils.helpers;

import org.hl7.fhir.r5.model.DomainResource;
import org.hl7.fhir.r5.model.Extension;

import org.ysifhir.constants.JsonConfigConstants;
import org.ysifhir.utils.common.CommonUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ExtensionConverterHelper {

    /**
     * Adds dynamic extensions to the given target object based on the configuration.
     *
     * @param targetObj   The FHIR resource object to which extensions are added.
     * @param extensions  List of extension mappings from the configuration.
     */
    public static void addExtensionsDynamically(DomainResource targetObj, List<Map<String, Object>> extensions, Class<?> sourceClass, Object sourceObj) throws Exception {
        for (Map<String, Object> extensionConfig : extensions) {
            Extension extension = new Extension();
            String urlFieldName = (String) extensionConfig.get(JsonConfigConstants.URL_EXTENSION_KEY);
            extension.setUrl(getUrlFieldValue(urlFieldName , sourceClass ,sourceObj));

            // Check for nested extensions
            List<Map<String, Object>> nestedExtensions = (List<Map<String, Object>>) extensionConfig.get("extension");

            if (nestedExtensions != null && !nestedExtensions.isEmpty()) {
                // Handle nested extensions recursively
                for (Map<String, Object> nestedConfig : nestedExtensions) {
                    Extension nestedExtension = createExtension(nestedConfig, sourceClass, sourceObj);
                    if (nestedExtension != null) {
                        extension.addExtension(nestedExtension);
                    }
                }
            } else {
                // Add primitive value
                Object value = getPrimitiveValue(extensionConfig, sourceClass, sourceObj);
                String dataType = (String) extensionConfig.get(JsonConfigConstants.DATA_TYPE_KEY);
                if (value != null) {
                    extension.setValue( CommonUtil.convertPrimitiveType(value, dataType));
                }
            }

            // Add the constructed extension to the target FHIR resource
            targetObj.addExtension(extension);
        }
    }

    /**
     * Creates a single Extension object from the given configuration.
     *
     * @param extensionConfig The configuration for the extension.
     * @return The constructed Extension object.
     */
    private static Extension createExtension(Map<String, Object> extensionConfig, Class<?> sourceClass, Object sourceObj) throws Exception {
        Extension extension = new Extension();
        String urlFieldName = (String) extensionConfig.get(JsonConfigConstants.URL_EXTENSION_KEY);
        extension.setUrl(getUrlFieldValue(urlFieldName , sourceClass ,sourceObj));

        // Handle nested extensions
        List<Map<String, Object>> nestedExtensions = (List<Map<String, Object>>) extensionConfig.get("extension");

        if (nestedExtensions != null && !nestedExtensions.isEmpty()) {
            for (Map<String, Object> nestedConfig : nestedExtensions) {
                Extension nestedExtension = createExtension(nestedConfig, sourceClass, sourceObj);
                if (nestedExtension != null) {
                    extension.addExtension(nestedExtension);
                }
            }
        } else {
            // Add primitive value
            Object value = getPrimitiveValue(extensionConfig, sourceClass, sourceObj);
            String dataType = (String) extensionConfig.get(JsonConfigConstants.DATA_TYPE_KEY);
            if (value != null) {
                extension.setValue(CommonUtil.convertPrimitiveType(value, dataType));
            }
        }

        return extension;
    }

    /**
     * Retrieves the value for a given field from the source object, as defined in the configuration.
     *
     * @param extensionConfig The extension configuration.
     * @param sourceClass     The class of the source object.
     * @param sourceObj       The source object.
     * @return The value of the field, or null if not found.
     */
    private static Object getPrimitiveValue(Map<String, Object> extensionConfig, Class<?> sourceClass, Object sourceObj) throws Exception {
        String fromField = (String) extensionConfig.get(JsonConfigConstants.VALUE_EXTENSION_KEY);
        if (fromField != null) {
            Field sourceField = sourceClass.getDeclaredField(fromField);
            sourceField.setAccessible(true);
            return sourceField.get(sourceObj);
        }
        return null;
    }

    /**
     * Retrieves the value of a URL field from a given source object.
     *
     * @param urlFieldName The name of the field in the source class that contains the URL value.
     * @param sourceClass  The class of the source object being inspected.
     * @param sourceObj    The source object from which the field value is extracted.
     * @return The value of the URL field as a String.
     * @throws NoSuchFieldException   If the specified field name does not exist in the source class.
     * @throws IllegalAccessException If the field cannot be accessed due to Java access control.
     */
    private static String getUrlFieldValue(String urlFieldName , Class<?> sourceClass ,Object sourceObj) throws NoSuchFieldException, IllegalAccessException {
        Field sourceField = sourceClass.getDeclaredField(urlFieldName);
        sourceField.setAccessible(true);
        return (String) sourceField.get(sourceObj);
    }
}
