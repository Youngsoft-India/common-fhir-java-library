package org.ysifhir.utils;

import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ysifhir.utils.common.CommonUtil;
import org.ysifhir.utils.helpers.CodeableConceptConversionHelper;
import org.ysifhir.utils.helpers.EnumerationConversionHelper;

import java.util.*;

/**
 * Utility class to handle type conversion for various data types during object transformation.
 */
public class TypeConverterUtil {

        private static final Logger logger = LoggerFactory.getLogger(TypeConverterUtil.class);

    /**
     * Converts a given value to the specified target type based on provided field name, data type,
     * and nested field mappings. This method handles common FHIR data types and primitive types.
     *
     * @param value         The source value to be converted.
     * @param targetType    The target class type to which the value is to be converted.
     * @param targetFieldName The name of the target field being populated.
     * @param givenDataType A string representing the data type of the field being processed.
     * @param nestedFields  List of nested field mappings, used for complex or polymorphic types.
     * @param sourceClass   The class of the source object being transformed.
     * @param sourceObj     The source object being transformed.
     * @return The converted value in the target type.
     * @throws Exception If the conversion process encounters an unsupported type or a critical error.
     */
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
            return CommonUtil.handleDate(value);
        }

        if (targetType == List.class) {
            List<Object> list = new ArrayList<>();
            list.add(CommonUtil.handleComplexType(givenDataType, sourceObj, nestedFields, sourceClass));
            return list;
        }

        if (Enumeration.class.isAssignableFrom(targetType)) {
            return EnumerationConversionHelper.handleEnumeration(value , givenDataType); // Pass the Field object
        }

        if (CodeableConcept.class.isAssignableFrom(targetType)) {
            return CodeableConceptConversionHelper.createCodeableConcept(value , targetFieldName); // Pass the Field object
        }

        if(Type.class == targetType){
            return CommonUtil.handlePolymorphicType(value , givenDataType);
        }

        if(!CommonUtil.isPrimitiveOrWrapperPrimitive(targetType)){
            return CommonUtil.handleComplexType(givenDataType, sourceObj, nestedFields, sourceClass);
        }

        if (Reference.class.isAssignableFrom(targetType)) {
            return CommonUtil.handleReference(value); // Pass the Field object
        }

        // Unsupported type
        logger.info("Unsupported conversion: Source value type "+ value.getClass().getName()+" cannot be converted to target type "+targetType.getName());

        // Unsupported type
        throw new IllegalArgumentException("Cannot convert value to " + targetType.getName());
    }
}