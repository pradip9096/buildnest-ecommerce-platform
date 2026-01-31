package com.example.buildnest_ecommerce.util;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Section 4.2 - DTO Mapping Consistency
 * Generic mapper utility for consistent entity-to-DTO transformations
 * Provides type-safe mapping with null-safety and reflection capabilities
 */
@Component
public class GenericMapperUtil {

    /**
     * Maps source object to target class using reflection
     */
    public <S, T> T map(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map object: " + e.getMessage(), e);
        }
    }

    /**
     * Maps list of source objects to target class
     */
    public <S, T> List<T> mapList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null) {
            return Collections.emptyList();
        }

        return sourceList.stream()
                .map(source -> map(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * Copies properties from source to target
     */
    private void copyProperties(Object source, Object target) {
        Map<String, Field> sourceFields = getAllFields(source.getClass());
        Map<String, Field> targetFields = getAllFields(target.getClass());

        for (String fieldName : sourceFields.keySet()) {
            if (targetFields.containsKey(fieldName)) {
                try {
                    Field sourceField = sourceFields.get(fieldName);
                    Field targetField = targetFields.get(fieldName);

                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);

                    Object value = sourceField.get(source);

                    // Only copy if types match
                    if (value != null && isAssignable(sourceField.getType(), targetField.getType())) {
                        targetField.set(target, value);
                    }
                } catch (Exception e) {
                    // Skip this field
                }
            }
        }
    }

    /**
     * Gets all fields including inherited ones
     */
    private Map<String, Field> getAllFields(Class<?> clazz) {
        Map<String, Field> fields = new HashMap<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                fields.put(field.getName(), field);
            }
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * Checks if types are assignable (with primitive compatibility)
     */
    private boolean isAssignable(Class<?> source, Class<?> target) {
        if (target.isAssignableFrom(source)) {
            return true;
        }

        // Handle primitive types
        Map<Class<?>, Class<?>> primitiveWrappers = new HashMap<>();
        primitiveWrappers.put(boolean.class, Boolean.class);
        primitiveWrappers.put(byte.class, Byte.class);
        primitiveWrappers.put(char.class, Character.class);
        primitiveWrappers.put(short.class, Short.class);
        primitiveWrappers.put(int.class, Integer.class);
        primitiveWrappers.put(long.class, Long.class);
        primitiveWrappers.put(float.class, Float.class);
        primitiveWrappers.put(double.class, Double.class);

        return (primitiveWrappers.containsKey(source) && primitiveWrappers.get(source).equals(target)) ||
                (primitiveWrappers.containsKey(target) && primitiveWrappers.get(target).equals(source));
    }

    /**
     * Merges partial update from source to target (only non-null values)
     */
    public <T> void mergeUpdate(T source, T target) {
        if (source == null || target == null) {
            return;
        }

        Map<String, Field> fields = getAllFields(source.getClass());

        for (Field field : fields.values()) {
            try {
                field.setAccessible(true);
                Object value = field.get(source);

                if (value != null) {
                    field.set(target, value);
                }
            } catch (Exception e) {
                // Skip this field
            }
        }
    }

    /**
     * Creates a shallow copy of an object
     */
    public <T> T shallowCopy(T source) {
        if (source == null) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            T copy = (T) source.getClass().getDeclaredConstructor().newInstance();
            copyProperties(source, copy);
            return copy;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create shallow copy: " + e.getMessage(), e);
        }
    }

    /**
     * Maps object to Map representation
     */
    public Map<String, Object> toMap(Object source) {
        if (source == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Field> fields = getAllFields(source.getClass());

        for (Map.Entry<String, Field> entry : fields.entrySet()) {
            try {
                Field field = entry.getValue();
                field.setAccessible(true);
                Object value = field.get(source);

                if (value != null) {
                    result.put(entry.getKey(), value);
                }
            } catch (Exception e) {
                // Skip this field
            }
        }

        return result;
    }

    /**
     * Builder pattern for mapping configuration
     */
    public static class MappingBuilder<S, T> {
        private S source;
        private T target;
        private Map<String, FieldMapper> fieldMappers = new HashMap<>();
        private Set<String> excludeFields = new HashSet<>();

        public MappingBuilder(S source, T target) {
            this.source = source;
            this.target = target;
        }

        public MappingBuilder<S, T> mapField(String sourceField, String targetField) {
            fieldMappers.put(sourceField, new FieldMapper(sourceField, targetField, null));
            return this;
        }

        public MappingBuilder<S, T> mapFieldWithTransform(String sourceField, String targetField,
                FieldTransformer transformer) {
            fieldMappers.put(sourceField, new FieldMapper(sourceField, targetField, transformer));
            return this;
        }

        public MappingBuilder<S, T> excludeField(String fieldName) {
            excludeFields.add(fieldName);
            return this;
        }

        public T build() {
            Map<String, Field> sourceFields = getAllFieldsStatic(source.getClass());
            Map<String, Field> targetFields = getAllFieldsStatic(target.getClass());

            // Copy default fields
            for (String fieldName : sourceFields.keySet()) {
                if (excludeFields.contains(fieldName)) {
                    continue;
                }

                if (targetFields.containsKey(fieldName)) {
                    try {
                        Field sourceField = sourceFields.get(fieldName);
                        Field targetField = targetFields.get(fieldName);

                        sourceField.setAccessible(true);
                        targetField.setAccessible(true);

                        Object value = sourceField.get(source);
                        if (value != null) {
                            targetField.set(target, value);
                        }
                    } catch (Exception e) {
                        // Skip this field
                    }
                }
            }

            // Apply custom field mappers
            for (FieldMapper mapper : fieldMappers.values()) {
                try {
                    Field sourceField = sourceFields.get(mapper.sourceField);
                    Field targetField = targetFields.get(mapper.targetField);

                    if (sourceField != null && targetField != null) {
                        sourceField.setAccessible(true);
                        targetField.setAccessible(true);

                        Object value = sourceField.get(source);
                        if (value != null && mapper.transformer != null) {
                            value = mapper.transformer.transform(value);
                        }

                        if (value != null) {
                            targetField.set(target, value);
                        }
                    }
                } catch (Exception e) {
                    // Skip this field
                }
            }

            return target;
        }

        private static Map<String, Field> getAllFieldsStatic(Class<?> clazz) {
            Map<String, Field> fields = new HashMap<>();
            Class<?> currentClass = clazz;

            while (currentClass != null && currentClass != Object.class) {
                for (Field field : currentClass.getDeclaredFields()) {
                    fields.put(field.getName(), field);
                }
                currentClass = currentClass.getSuperclass();
            }

            return fields;
        }
    }

    /**
     * Field mapper configuration
     */
    private static class FieldMapper {
        String sourceField;
        String targetField;
        FieldTransformer transformer;

        FieldMapper(String sourceField, String targetField, FieldTransformer transformer) {
            this.sourceField = sourceField;
            this.targetField = targetField;
            this.transformer = transformer;
        }
    }

    /**
     * Field transformer interface
     */
    @FunctionalInterface
    public interface FieldTransformer {
        Object transform(Object value);
    }
}
