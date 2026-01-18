package com.offlix.distributed_graph_engine.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class EngineProperties {

    private static final Set<Class<?>> SIMPLE_TYPES = new HashSet<>(Arrays.asList(
            String.class, Long.class, Integer.class, Double.class,
            Boolean.class, Float.class, Long.TYPE, Integer.TYPE,
            Boolean.TYPE, Double.TYPE, Object.class, Void.class
    ));

    public static Map<String, Object> getProperties(Class<?> clazz, Set<Class<?>> visitedClasses) {
        return getProperties(clazz, visitedClasses, Collections.emptyList());
    }

    public static Map<String, Object> getProperties(Class<?> clazz, Set<Class<?>> visitedClasses, List<String> excludeFields) {
        Map<String, Object> propertyMap = new LinkedHashMap<>();

        if (clazz == null || clazz.isPrimitive() || SIMPLE_TYPES.contains(clazz) || clazz.equals(Object.class)) {
            return propertyMap;
        }

        if (visitedClasses.contains(clazz)) {
            propertyMap.put("_note", "Circular reference to " + clazz.getSimpleName());
            return propertyMap;
        }

        visitedClasses.add(clazz);

        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();

            for (Field field : fields) {
                if (excludeFields.contains(field.getName()) || Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }

                Class<?> fieldType = field.getType();
                String fieldName = field.getName();

                if (SIMPLE_TYPES.contains(fieldType) || fieldType.isPrimitive() || fieldType.isEnum()) {
                    propertyMap.put(fieldName, fieldType.getSimpleName());
                }
                else if (Collection.class.isAssignableFrom(fieldType) || fieldType.isArray()) {
                    handleGenericCollection(field, propertyMap, visitedClasses, excludeFields);
                }
                else if (Map.class.isAssignableFrom(fieldType)) {
                    propertyMap.put(fieldName, fieldType.getSimpleName());
                }
                else {
                    Set<Class<?>> nextVisited = new HashSet<>(visitedClasses);
                    propertyMap.put(fieldName, getProperties(fieldType, nextVisited, excludeFields));
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return propertyMap;
    }

    private static void handleGenericCollection(Field field, Map<String, Object> propertyMap, Set<Class<?>> visited, List<String> excludes) {
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] typeArgs = pt.getActualTypeArguments();

            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                Class<?> innerClass = (Class<?>) typeArgs[0];

                if (!SIMPLE_TYPES.contains(innerClass)) {
                    Map<String, Object> innerSchema = getProperties(innerClass, new HashSet<>(visited), excludes);
                    propertyMap.put(field.getName(), Collections.singletonList(innerSchema));
                    return;
                }
            }
        }
        propertyMap.put(field.getName(), field.getType().getSimpleName());
    }
}