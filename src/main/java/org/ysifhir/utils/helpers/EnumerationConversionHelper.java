package org.ysifhir.utils.helpers;

import org.hl7.fhir.dstu3.model.EnumFactory;
import org.hl7.fhir.dstu3.model.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ysifhir.FHIRConverter;

public class EnumerationConversionHelper {

    private static final Logger logger = LoggerFactory.getLogger(EnumerationConversionHelper.class);
    public static Object handleEnumeration(Object value, String givenDataType) throws Exception {

        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null for enumeration conversion.");
        }
        try {

            // Load EnumFactory and Enum from DSTU3
            String enumFactoryClassName = "org.hl7.fhir.dstu3.model.codesystems." + givenDataType + "EnumFactory";
            String enumClassName = "org.hl7.fhir.dstu3.model.codesystems." + givenDataType;

            Class<?> enumFactoryClass = Class.forName(enumFactoryClassName);
            Object enumFactory = enumFactoryClass.getDeclaredConstructor().newInstance();

            Class<?> enumClass = Class.forName(enumClassName);
            Enum<?> enumConstant = Enum.valueOf((Class<Enum>) enumClass, value.toString().toUpperCase());

            // Create DSTU3 Enumeration object
            Enumeration<Enum<?>> enumeration = new Enumeration<>((EnumFactory<Enum<?>>) enumFactory);
            enumeration.setValue(enumConstant);

            return enumeration;
        } catch (Exception e) {
            logger.info("Error during DSTU3 enumeration handling: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

}
