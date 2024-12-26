package org.ysifhir.utils.helpers;

import org.hl7.fhir.r5.model.EnumFactory;
import org.hl7.fhir.r5.model.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ysifhir.constants.FHIRConstants;

/**
 * A utility class for handling the conversion of enumeration values to FHIR r5 `Enumeration` objects.
 */
public class EnumerationConversionHelper {

    private static final Logger logger = LoggerFactory.getLogger(EnumerationConversionHelper.class);

    /**
     * Converts a given value to a FHIR r5 `Enumeration` object based on the specified data type.
     *
     * @param value         The value to be converted, which should match an enumeration constant in the target type.
     * @param givenDataType The name of the FHIR enumeration data type (e.g., "ContactPointSystem").
     * @return An instance of `Enumeration` containing the converted value.
     * @throws Exception If an error occurs during the conversion process, such as missing classes or invalid values.
     */
    public static Object handleEnumeration(Object value, String givenDataType) throws Exception {

        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null for enumeration conversion.");
        }
        try {

            // Load EnumFactory and Enum from r5
            String enumFactoryClassName = FHIRConstants.HAPI_CODESYSTEMS_CLASS_PATH + givenDataType + FHIRConstants.HAPI_ENUM_FACTORY_CLASS_NAME;
            String enumClassName = FHIRConstants.HAPI_CODESYSTEMS_CLASS_PATH + givenDataType;

            Class<?> enumFactoryClass = Class.forName(enumFactoryClassName);
            Object enumFactory = enumFactoryClass.getDeclaredConstructor().newInstance();

            Class<?> enumClass = Class.forName(enumClassName);
            Enum<?> enumConstant = Enum.valueOf((Class<Enum>) enumClass, value.toString().toUpperCase());

            // Create r5 Enumeration object
            Enumeration<Enum<?>> enumeration = new Enumeration<>((EnumFactory<Enum<?>>) enumFactory);
            enumeration.setValue(enumConstant);

            return enumeration;
        } catch (Exception e) {
            logger.info("Error during r5 enumeration handling: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

}
