package org.ysifhir.utils;

import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ysifhir.utils.common.CommonUtil;
import org.ysifhir.utils.helpers.CodeableConceptConversionHelper;
import org.ysifhir.utils.helpers.EnumerationConversionHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class TypeConverterUtil {

        private static final Logger logger = LoggerFactory.getLogger(TypeConverterUtil.class);
        public static Object convertValue(Object value, Class<?> targetType, String targetFieldName,
                                  String givenDataType, List<Map<String, Object>> nestedFields,
                                  Class<?> sourceClass, Object sourceObj) throws Exception {
        if (value == null) {
            // Handle null values properly
            logger.info("Received null value for conversion to target type : " + targetType.getName() + " and field name : " + targetFieldName);
            return null; // You can return null or throw an exception based on your requirements
        }

        // Print details of the conversion attempt
        logger.info("Converting value "+value+" of type "+value.getClass().getName()+" to target type "+targetType.getName());

        // Handle primitive and common types
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (targetType == String.class) {
            return value.toString();
        }

        if (targetType == Integer.class || targetType == int.class) {
            return new IntegerType(Integer.parseInt(value.toString()));
        }

        if(targetType == IntegerType.class){
            return new IntegerType((Integer) value);
        }

        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value.toString());
        }

        if(targetType == DecimalType.class || targetType == float.class){
            return new DecimalType((Double) value);
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }

        if(targetType == BooleanType.class){
            return new BooleanType((Boolean) value);
        }

        if (targetType == DateType.class) {
            return handleDate(value);
        }

        if (targetType == List.class) {
            List<Object> list = new ArrayList<>();
            list.add(handleComplexType(givenDataType, sourceObj, nestedFields, sourceClass));
            return list;
        }

        if (Enumeration.class.isAssignableFrom(targetType)) {
            return EnumerationConversionHelper.handleEnumeration(value , givenDataType); // Pass the Field object
        }

        if (CodeableConcept.class.isAssignableFrom(targetType)) {
            return CodeableConceptConversionHelper.createCodeableConcept(value , targetFieldName); // Pass the Field object
        }

        if(Type.class == targetType){
            return handlePolymorphicType(value , givenDataType);
        }

        if(!CommonUtil.isPrimitiveOrWrapperPrimitive(targetType)){
            return handleComplexType(givenDataType, sourceObj, nestedFields, sourceClass);
        }

        if (Reference.class.isAssignableFrom(targetType)) {
            return handleReference(value); // Pass the Field object
        }

        // Unsupported type
        logger.info("Unsupported conversion: Source value type "+ value.getClass().getName()+" cannot be converted to target type "+targetType.getName());

        // Unsupported type
        throw new IllegalArgumentException("Cannot convert value to " + targetType.getName());
    }

    public static Type handleDate(Object value) throws Exception {

        if( value instanceof String){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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

    private static Object handleReference(Object value) throws Exception {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Expected a String for Reference conversion.");
        }
        Reference reference = new Reference();
        reference.setReference(value.toString());
        return reference;
    }

    private static Type handlePolymorphicType(Object value, String givenDataType) throws Exception {
        if (value == null) {
            throw new IllegalArgumentException("Value is null for field.");
        }

        switch (givenDataType) {
            case "Boolean":
                if (value instanceof Boolean) {
                    return new BooleanType((Boolean) value);
                } else {
                    throw new IllegalArgumentException("Invalid value type for Boolean. Expected Boolean but got: " + value.getClass().getName());
                }

            case "DateTime":
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

    public static Object handleComplexType(String targetType, Object sourceObj, List<Map<String, Object>> subFields, Class<?> sourceClass) throws Exception {

        String targetTypeClassPath = "org.hl7.fhir.dstu3.model."+targetType;

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
            String targetSubField = (String) fieldConfig.get("to");
            String sourceSubField = (String) fieldConfig.get("from");
            String givenSubDataType = (String) fieldConfig.get("dataType");
            List<Map<String, Object>> nestedFields = (List<Map<String, Object>>) fieldConfig.get("fields");

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
                        Object nestedObject = convertValue(
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