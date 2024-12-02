package org.ysifhir.utils.helpers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;

public class CodeableConceptConversionHelper {

    public static CodeableConcept createCodeableConcept(Object value , String fieldName) {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(value.toString());
        coding.setSystem(getSystemUrl(fieldName));
        coding.setDisplay(value.toString());
        codeableConcept.addCoding(coding);
        return codeableConcept;
    }

    /**
     * Dynamically constructs a FHIR-compliant system URL based on the field type.
     * Example: fieldType "maritalStatus" -> "http://hl7.org/fhir/v3/MaritalStatus"
     *
     * @param fieldType the type of the field (e.g., "maritalStatus")
     * @return the constructed system URL
     */
    public static String getSystemUrl(String fieldType) {
        // Base URL for HL7 v3 codes
        String baseUrl = "http://hl7.org/fhir/v3/";

        // Convert the field type to PascalCase
        String pascalCaseFieldType = convertFieldTypeInPascalCase(fieldType);

        return baseUrl + pascalCaseFieldType;
    }

    /**
     * Converts a given field type to PascalCase.
     * Example: "maritalStatus" -> "MaritalStatus"
     *
     * @param fieldType the field type in camelCase or snake_case
     * @return the field type converted to PascalCase
     */
    private static String convertFieldTypeInPascalCase(String fieldType) {
        if (fieldType == null || fieldType.isEmpty()) {
            throw new IllegalArgumentException("Field type cannot be null or empty");
        }

        // Split camelCase or snake_case into parts
        String[] parts = fieldType.split("(?<!^)(?=[A-Z])|_");

        // Capitalize each part and join them
        StringBuilder pascalCase = new StringBuilder();
        for (String part : parts) {
            pascalCase.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1).toLowerCase());
        }

        return pascalCase.toString();
    }
}
