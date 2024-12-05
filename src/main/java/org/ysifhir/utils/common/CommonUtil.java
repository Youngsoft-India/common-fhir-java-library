package org.ysifhir.utils.common;

import org.hl7.fhir.dstu3.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ysifhir.FHIRConverter;
import org.ysifhir.constants.FHIRConstants;
import org.ysifhir.constants.JsonConfigConstants;
import org.ysifhir.utils.TypeConverterUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A utility class containing methods for handling primitive types, conversions, and mappings
 * to support FHIR data types.
 */
public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(FHIRConverter.class);
    private static final Set<Class<?>> PRIMITIVE_WRAPPERS_AND_STRING = Set.of(
            Boolean.class, Byte.class, Character.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Void.class,
            String.class // Adding String as "primitive-like"
    );

    /**
     * Checks if the given object is of a primitive type or its wrapper, or String.
     *
     * @param value The object to check.
     * @return True if the object is primitive-like, false otherwise.
     */
    public static boolean isPrimitiveOrWrapperPrimitive(Object value) {
        if (value == null) {
            return false;
        }
        Class<?> clazz = value.getClass();
        // Check if it's primitive or one of the types in the set
        return clazz.isPrimitive() || PRIMITIVE_WRAPPERS_AND_STRING.contains(clazz);
    }

    /**
     * Converts a primitive or primitive-like value to a FHIR `Type` object.
     *
     * @param value      The value to convert.
     * @param targetType The target type as a string (e.g., "StringType", "IntegerType").
     * @return The converted FHIR `Type` object.
     * @throws Exception If the conversion fails.
     */
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
                return handleDate(value);
            default:
                logger.info("Unsupported conversion: Source value type " + value.getClass().getName() +
                        " cannot be converted to target type " + targetType);
                throw new IllegalArgumentException("Unsupported target type: " + targetType);
        }
    }

    /**
     * Handles conversion of date values to a FHIR `DateType` object.
     *
     * @param value The date value as a `String` or `Date`.
     * @return A `DateType` object.
     * @throws Exception If the value is of an unsupported type or cannot be parsed.
     */
    public static Type handleDate(Object value) throws Exception {

        if( value instanceof String){
            SimpleDateFormat sdf = new SimpleDateFormat(FHIRConstants.DATE_FORMAT);
            Date date = sdf.parse(value.toString());
            DateType dateType = new DateType();
            dateType.setValue(date);
            return dateType;

        } else if (value instanceof Date) {
            return new DateType((Date) value);

        } else {
            throw new IllegalArgumentException(
                    "Invalid value type for DateType. Expected String or Date but got: " + value.getClass().getName()
            );
        }
    }

    /**
     * Converts a `String` value to a FHIR `Reference` object.
     *
     * @param value The value to convert.
     * @return A `Reference` object.
     * @throws Exception If the value is not a string.
     */
    public static Object handleReference(Object value) throws Exception {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Expected a String for Reference conversion.");
        }
        Reference reference = new Reference();
        reference.setReference(value.toString());
        return reference;
    }

    /**
     * Handles polymorphic type conversion based on the given data type.
     *
     * @param value         The value to convert.
     * @param givenDataType The target data type (e.g., "BooleanType", "DateTime").
     * @return A corresponding FHIR `Type` object.
     * @throws Exception If the conversion fails or the type is unsupported.
     */
    public static Type handlePolymorphicType(Object value, String givenDataType) throws Exception {
        if (value == null) {
            throw new IllegalArgumentException("Value is null for field.");
        }

        switch (givenDataType) {
            case "Boolean":
            case "boolean":
            case "BooleanType":
                if (value instanceof Boolean) {
                    return new BooleanType((Boolean) value);
                } else {
                    throw new IllegalArgumentException("Invalid value type for Boolean. Expected Boolean but got: " + value.getClass().getName());
                }

            case "DateTime":
            case "Date":
                if (value instanceof String) {
                    // Assuming ISO format string for date
                    return new DateTimeType((String) value);
                } else if (value instanceof Date) {
                    // Handle java.util.Date
                    return new DateTimeType((Date) value);
                } else {
                    throw new IllegalArgumentException(
                            "Invalid value type for DateTimeType. Expected String or Date but got: " + value.getClass().getName()
                    );
                }

            case "String":
            case "StringType":
                if(value instanceof String || value instanceof StringType){
                    return  new StringType((String)value);
                }
                else {
                    throw new IllegalArgumentException(
                            "Invalid value type for Integer. Expected int or Integer type but got: " + value.getClass().getName()
                    );
                }

            case "Integer":
            case "int":
            case "IntegerType":
                if(value instanceof Integer){
                    return  new IntegerType((Integer)value);
                }
                else {
                    throw new IllegalArgumentException(
                            "Invalid value type for Integer. Expected int or Integer type but got: " + value.getClass().getName()
                    );
                }

            case "Double":
            case "double":
            case "DecimalType":
                if(value instanceof Double || value instanceof DecimalType){
                    return new DecimalType(Double.valueOf(value.toString()));
                }
                else {
                    throw new IllegalArgumentException(
                            "Invalid value type for Double. Expected int or Integer type but got: " + value.getClass().getName()
                    );
                }

            default:
                throw new IllegalArgumentException("Unsupported dataType: " + givenDataType);
        }
    }

    /**
     * Handles the conversion of complex fields by mapping source object fields to target fields.
     *
     * @param targetType  The target FHIR type.
     * @param sourceObj   The source object containing the data.
     * @param subFields   A list of subfield mappings.
     * @param sourceClass The source object's class.
     * @return The converted object of the target type.
     * @throws Exception If an error occurs during conversion.
     */
    public static Object handleComplexType(String targetType, Object sourceObj, List<Map<String, Object>> subFields, Class<?> sourceClass) throws Exception {

        String targetTypeClassPath = FHIRConstants.HAPI_MODEL_CLASS_PATH+targetType;

        Class<?> targetTypeClass = Class.forName(targetTypeClassPath);

        // Create an instance of the target type dynamically
        Object targetObject = targetTypeClass.getConstructor().newInstance();

        if (subFields == null || subFields.isEmpty()) {
            logger.info("No subFields provided for target type : " + targetType);
            return null; // Or handle as needed (e.g., return default object)
        }

        // Track processed fields to avoid recursion issues
//        Set<String> processedFields = new HashSet<>();

        for (Map<String, Object> fieldConfig : subFields) {
            String targetSubField = (String) fieldConfig.get(JsonConfigConstants.TO_KEY);
            String sourceSubField = (String) fieldConfig.get(JsonConfigConstants.FROM_KEY);
            String givenSubDataType = (String) fieldConfig.get(JsonConfigConstants.DATA_TYPE_KEY);
            List<Map<String, Object>> nestedFields = (List<Map<String, Object>>) fieldConfig.get(JsonConfigConstants.SUB_FIELDS_KEY);

//            if (processedFields.contains(targetSubField)) {
//                logger.info("Skipping already processed field: " + targetSubField);
//                continue;
//            }

            // Access source field
            Field sourceField = sourceClass.getDeclaredField(sourceSubField);
            sourceField.setAccessible(true);
            Object subFieldValue = sourceField.get(sourceObj);
            // Access target field
            Field targetField = targetTypeClass.getDeclaredField(targetSubField);
            targetField.setAccessible(true);

            // Handle specific data types if necessary (e.g., List, Period)
            if (targetField.getType() == List.class && subFieldValue instanceof List) {
                targetField.set(targetObject, List.of(subFieldValue));

//            } else if (targetField.getType() == Period.class) {
//                Period period = new Period();
//                // Handle period-specific mapping if required
//                targetField.set(targetObject, period);

            } else if (targetField.getType() == StringType.class){
                // Set simple field values directly
                StringType stringType = new StringType(subFieldValue.toString());
                targetField.set(targetObject, stringType);
            }
            else {
                try{
                    String targetTypeClazz = targetField.getType().getName();
                    Class<?> targetTypeClz = Class.forName(targetTypeClazz);
                    Constructor<?> constructor = targetTypeClz.getConstructor(String.class);
                    // Create an instance of the target class
                    Object instance = constructor.newInstance(subFieldValue.toString());
                    targetField.set(targetObject , instance);
                }
                catch (NoSuchMethodException e){
                    logger.info("No suitable constructor for field: " + sourceSubField);
                    if (nestedFields != null && !nestedFields.isEmpty()) {
                        // Recursive handling using convertValue
                        Object nestedObject = TypeConverterUtil.convertValue(
                                subFieldValue,                  // Current field value
                                targetField.getType(),          // Target field type
                                targetSubField,                 // Target field name
                                givenSubDataType,               // Data type for conversion
                                nestedFields,                   // Nested fields to process
                                sourceClass,                    // Source class
                                sourceObj                       // Source object
                        );
                        targetField.set(targetObject, nestedObject);
                    } else {
                        logger.info("No fields found for complex mapping: " + sourceSubField);
                        throw new IllegalArgumentException("Field cannot be mapped: " + sourceSubField, e);
                    }
                }
            }
        }

        return targetObject;
    }

}
