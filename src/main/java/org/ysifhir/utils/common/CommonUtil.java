package org.ysifhir.utils.common;

import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ysifhir.FHIRConverter;
import org.ysifhir.utils.TypeConverterUtil;
import java.util.Set;

public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(FHIRConverter.class);
    private static final Set<Class<?>> PRIMITIVE_WRAPPERS_AND_STRING = Set.of(
            Boolean.class, Byte.class, Character.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Void.class,
            String.class // Adding String as "primitive-like"
    );

    public static boolean isPrimitiveOrWrapperPrimitive(Object value) {
        if (value == null) {
            return false;
        }
        Class<?> clazz = value.getClass();
        // Check if it's primitive or one of the types in the set
        return clazz.isPrimitive() || PRIMITIVE_WRAPPERS_AND_STRING.contains(clazz);
    }

    public static Type convertPrimitiveType(Object value, String targetType) throws Exception {
        if (value == null) {
            // Handle null values properly
            logger.info("Received null value for conversion to target type: " + targetType);
            return new StringType(null); // Return an appropriate Type subclass
        }

        if(targetType == null){
            throw new IllegalArgumentException("dataType con not be : " + null);
        }

        // Print details of the conversion attempt
        logger.info("Converting value " + value + " of type " + value.getClass().getName() + " to target type " + targetType);

        switch (targetType) {
            case "String":
            case "StringType":
                return new StringType(value.toString());
            case "Integer":
            case "int":
            case "IntegerType":
                return new IntegerType(Integer.valueOf(value.toString())); // Ensure conversion to Integer
            case "Double":
            case "double":
            case "DecimalType":
                return new DecimalType(Double.valueOf(value.toString()));
            case "Boolean":
            case "boolean":
            case "BooleanType":
                return new BooleanType(Boolean.valueOf(value.toString()));
            case "Date":
            case "DateType":
                return TypeConverterUtil.handleDate(value); // Assuming this correctly handles Date conversion
            default:
                logger.info("Unsupported conversion: Source value type " + value.getClass().getName() +
                        " cannot be converted to target type " + targetType);
                throw new IllegalArgumentException("Unsupported target type: " + targetType);
        }
    }

}
