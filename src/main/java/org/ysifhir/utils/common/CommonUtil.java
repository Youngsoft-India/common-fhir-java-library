package org.ysifhir.utils.common;

import java.util.Set;

public class CommonUtil {
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
}
