package org.ysifhir.utils.helpers;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Type;
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
            String urlFieldName = (String) extensionConfig.get("url");
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
                String dataType = (String) extensionConfig.get("dataType");
                if (value != null) {
                    extension.setValue((Type) CommonUtil.convertPrimitiveType(value, dataType));
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
        String urlFieldName = (String) extensionConfig.get("url");
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
            String dataType = (String) extensionConfig.get("dataType");
            if (value != null) {
                extension.setValue((Type) CommonUtil.convertPrimitiveType(value, dataType));
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
        String fromField = (String) extensionConfig.get("value");
        if (fromField != null) {
            Field sourceField = sourceClass.getDeclaredField(fromField);
            sourceField.setAccessible(true);
            return sourceField.get(sourceObj);
        }
        return null;
    }

    private static String getUrlFieldValue(String urlFieldName , Class<?> sourceClass ,Object sourceObj) throws NoSuchFieldException, IllegalAccessException {
        Field sourceField = sourceClass.getDeclaredField(urlFieldName);
        sourceField.setAccessible(true);
        return (String) sourceField.get(sourceObj);
    }
}
