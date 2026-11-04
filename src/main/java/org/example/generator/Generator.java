package org.example.generator;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class Generator {
    public static final int DEFAULT_MAX_DEPTH = 4;
    public static final int MIN_CONTAINER_SIZE = 1;
    public static final int MAX_CONTAINER_SIZE = 5;
    public static final int MIN_STRING_LENGTH = 1;
    public static final int MAX_STRING_LENGTH = 12;
    public static final int MAX_CONSTRUCTION_ATTEMPTS = 100;
    public static final double DEFAULT_NUMBER_BOUND = 1000;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Type DEFAULT_ELEMENT_TYPE = Integer.class;
    private static final Type DEFAULT_KEY_TYPE = String.class;
    private static final List<Class<?>> COLLECTION_IMPLEMENTATIONS =
            List.of(ArrayList.class, LinkedList.class, HashSet.class, LinkedHashSet.class);
    private static final List<Class<?>> MAP_IMPLEMENTATIONS = List.of(HashMap.class, LinkedHashMap.class);
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = Map.of(
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Double.class, double.class,
            Float.class, float.class,
            Integer.class, int.class,
            Long.class, long.class,
            Short.class, short.class);

    private final Random random;
    private final int maxDepth;
    private final ImplementationFinder implementationFinder = new ImplementationFinder();

    public Generator() {
        this(new Random());
    }

    public Generator(Random random) {
        this(random, DEFAULT_MAX_DEPTH);
    }

    public Generator(Random random, int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be non-negative: " + maxDepth);
        }
        this.random = Objects.requireNonNull(random, "random");
        this.maxDepth = maxDepth;
    }

    @SuppressWarnings("unchecked")
    public <T> T generateValueOfType(Class<T> type) {
        return (T) generateValueOfType((Type) type);
    }

    public Object generateValueOfType(Type type) {
        Objects.requireNonNull(type, "type");
        return generate(type, null, 0);
    }

    private Object generate(Type type, Range range, int depth) {
        if (type instanceof Class<?> rawType) {
            return generateOfClass(rawType, new Type[0], range, depth);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return generateOfClass((Class<?>) parameterizedType.getRawType(),
                    parameterizedType.getActualTypeArguments(), range, depth);
        }
        if (type instanceof GenericArrayType arrayType) {
            Type componentType = arrayType.getGenericComponentType();
            return generateArray(rawClassOf(componentType), componentType, range, depth);
        }
        if (type instanceof WildcardType wildcardType) {
            if (wildcardType.getLowerBounds().length > 0) {
                return generate(wildcardType.getLowerBounds()[0], range, depth);
            }
            Type upperBound = wildcardType.getUpperBounds()[0];
            return generate(upperBound == Object.class ? DEFAULT_ELEMENT_TYPE : upperBound, range, depth);
        }
        if (type instanceof TypeVariable<?> variable) {
            throw new GenerationException("Cannot generate a value of unresolved type variable '"
                    + variable.getName() + "' declared in " + variable.getGenericDeclaration());
        }
        throw new GenerationException("Unsupported kind of type: " + type);
    }

    private Object generateOfClass(Class<?> type, Type[] typeArguments, Range range, int depth) {
        if (type == void.class || type == Void.class) {
            throw new GenerationException("Cannot generate a value of type " + type.getName());
        }
        if (type.isPrimitive() || WRAPPER_TO_PRIMITIVE.containsKey(type)) {
            return generatePrimitive(type.isPrimitive() ? type : WRAPPER_TO_PRIMITIVE.get(type), range);
        }
        if (type == String.class || type == CharSequence.class) {
            return generateString();
        }
        if (type.isEnum()) {
            return generateEnumConstant(type);
        }
        if (type.isArray()) {
            return generateArray(type.getComponentType(), type.getComponentType(), range, depth);
        }
        if (Collection.class.isAssignableFrom(type)) {
            return generateCollection(type, typeArgumentOrDefault(typeArguments, 0, DEFAULT_ELEMENT_TYPE), range, depth);
        }
        if (Map.class.isAssignableFrom(type)) {
            return generateMap(type,
                    typeArgumentOrDefault(typeArguments, 0, DEFAULT_KEY_TYPE),
                    typeArgumentOrDefault(typeArguments, 1, DEFAULT_ELEMENT_TYPE),
                    range, depth);
        }
        if (isPlatformType(type)) {
            throw new GenerationException("Unsupported platform type " + type.getName()
                    + ": only primitives, wrappers, String, enums, arrays, collections, maps "
                    + "and @Generatable user types are supported");
        }
        return generateUserObject(type, depth);
    }

    private Object generatePrimitive(Class<?> primitive, Range range) {
        if (primitive == boolean.class) {
            requireNoRange(primitive, range);
            return random.nextBoolean();
        }
        if (primitive == char.class) {
            requireNoRange(primitive, range);
            return ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }
        if (primitive == double.class) {
            return generateDouble(primitive, range, -Double.MAX_VALUE, Double.MAX_VALUE);
        }
        if (primitive == float.class) {
            return (float) generateDouble(primitive, range, -Float.MAX_VALUE, Float.MAX_VALUE);
        }
        if (primitive == byte.class) {
            return (byte) generateLong(primitive, range, Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        if (primitive == short.class) {
            return (short) generateLong(primitive, range, Short.MIN_VALUE, Short.MAX_VALUE);
        }
        if (primitive == int.class) {
            return (int) generateLong(primitive, range, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        return generateLong(primitive, range, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    private double generateDouble(Class<?> primitive, Range range, double typeMin, double typeMax) {
        double min = range == null ? -DEFAULT_NUMBER_BOUND : range.min();
        double max = range == null ? DEFAULT_NUMBER_BOUND : range.max();
        if (!(min <= max) || min < typeMin || max > typeMax) {
            throw new GenerationException("Invalid @Range(min = " + min + ", max = " + max + ") for type "
                    + primitive.getName());
        }
        return min == max ? min : random.nextDouble(min, max);
    }

    private long generateLong(Class<?> primitive, Range range, long typeMin, long typeMax) {
        double min = Math.ceil(range == null ? Math.max(typeMin, -DEFAULT_NUMBER_BOUND) : range.min());
        double max = Math.floor(range == null ? Math.min(typeMax, DEFAULT_NUMBER_BOUND) : range.max());
        if (!(min <= max) || min < typeMin || max > typeMax) {
            throw new GenerationException("Invalid @Range for type " + primitive.getName()
                    + ": no integer values in [" + min + ", " + max + "] fit the type");
        }
        long lower = (long) min;
        long upper = (long) max;
        return upper == Long.MAX_VALUE ? random.nextLong(lower, upper) : random.nextLong(lower, upper + 1);
    }

    private static void requireNoRange(Class<?> primitive, Range range) {
        if (range != null) {
            throw new GenerationException("@Range is not applicable to type " + primitive.getName());
        }
    }

    private String generateString() {
        int length = random.nextInt(MIN_STRING_LENGTH, MAX_STRING_LENGTH + 1);
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }

    private Object generateEnumConstant(Class<?> type) {
        Object[] constants = type.getEnumConstants();
        if (constants.length == 0) {
            throw new GenerationException("Enum " + type.getName() + " has no constants");
        }
        return constants[random.nextInt(constants.length)];
    }

    private Object generateArray(Class<?> componentClass, Type componentType, Range range, int depth) {
        int length = randomContainerSize();
        Object array = Array.newInstance(componentClass, length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, generate(componentType, range, depth));
        }
        return array;
    }

    private Object generateCollection(Class<?> type, Type elementType, Range range, int depth) {
        @SuppressWarnings("unchecked")
        Collection<Object> collection = (Collection<Object>) instantiateContainer(type, COLLECTION_IMPLEMENTATIONS);
        int size = randomContainerSize();
        for (int i = 0; i < size; i++) {
            collection.add(generate(elementType, range, depth));
        }
        return collection;
    }

    private Object generateMap(Class<?> type, Type keyType, Type valueType, Range range, int depth) {
        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object, Object>) instantiateContainer(type, MAP_IMPLEMENTATIONS);
        int size = randomContainerSize();
        for (int i = 0; i < size; i++) {
            map.put(generate(keyType, range, depth), generate(valueType, range, depth));
        }
        return map;
    }

    private static Object instantiateContainer(Class<?> requestedType, List<Class<?>> implementations) {
        Class<?> implementation = implementations.stream()
                .filter(requestedType::isAssignableFrom)
                .findFirst()
                .orElseThrow(() -> new GenerationException("Unsupported container type " + requestedType.getName()
                        + ": supported implementations are " + implementations.stream().map(Class::getName).toList()));
        try {
            return implementation.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new GenerationException("Failed to instantiate container " + implementation.getName(), e);
        }
    }

    private Object generateUserObject(Class<?> type, int depth) {
        Generatable annotation = type.getAnnotation(Generatable.class);
        if (annotation == null) {
            throw new GenerationException("Type " + type.getName() + " is not annotated with @"
                    + Generatable.class.getSimpleName());
        }
        if (depth > maxDepth) {
            return null;
        }
        if (ImplementationFinder.isInstantiable(type)) {
            return instantiate(type, depth);
        }
        List<Class<?>> implementations = implementationFinder.findImplementations(type);
        if (implementations.isEmpty()) {
            throw new GenerationException("No concrete @" + Generatable.class.getSimpleName()
                    + " implementations found for " + type.getName());
        }
        return instantiate(implementations.get(random.nextInt(implementations.size())), depth);
    }

    private Object instantiate(Class<?> type, int depth) {
        if (type.isMemberClass() && !Modifier.isStatic(type.getModifiers())) {
            throw new GenerationException("Non-static inner class " + type.getName() + " is not supported");
        }
        List<Constructor<?>> constructors = selectConstructors(type);
        Throwable lastRejection = null;
        for (int attempt = 0; attempt < MAX_CONSTRUCTION_ATTEMPTS; attempt++) {
            Constructor<?> constructor = constructors.get(random.nextInt(constructors.size()));
            Object[] arguments = generateArguments(constructor, depth + 1);
            try {
                constructor.setAccessible(true);
                return constructor.newInstance(arguments);
            } catch (InvocationTargetException e) {
                if (!(e.getCause() instanceof IllegalArgumentException)) {
                    throw new GenerationException("Constructor " + constructor + " threw an exception", e.getCause());
                }
                lastRejection = e.getCause();
            } catch (ReflectiveOperationException | RuntimeException e) {
                throw new GenerationException("Failed to invoke constructor " + constructor, e);
            }
        }
        throw new GenerationException("Failed to create a valid " + type.getName() + " in "
                + MAX_CONSTRUCTION_ATTEMPTS + " attempts: constructors kept rejecting generated arguments",
                lastRejection);
    }

    private static List<Constructor<?>> selectConstructors(Class<?> type) {
        Constructor<?>[] declared = type.getDeclaredConstructors();
        List<Constructor<?>> annotated = Arrays.stream(declared)
                .filter(constructor -> constructor.isAnnotationPresent(Generatable.class))
                .toList();
        if (!annotated.isEmpty()) {
            return annotated;
        }
        List<Constructor<?>> publicConstructors = Arrays.stream(declared)
                .filter(constructor -> Modifier.isPublic(constructor.getModifiers()))
                .toList();
        if (publicConstructors.isEmpty()) {
            throw new GenerationException("Type " + type.getName() + " has neither public nor @"
                    + Generatable.class.getSimpleName() + " constructors");
        }
        return publicConstructors;
    }

    private Object[] generateArguments(Constructor<?> constructor, int depth) {
        Parameter[] parameters = constructor.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            try {
                arguments[i] = generate(parameter.getParameterizedType(), parameter.getAnnotation(Range.class), depth);
            } catch (GenerationException e) {
                throw new GenerationException("Cannot generate parameter '" + parameter.getName() + "' of "
                        + constructor, e);
            }
        }
        return arguments;
    }

    private int randomContainerSize() {
        return random.nextInt(MIN_CONTAINER_SIZE, MAX_CONTAINER_SIZE + 1);
    }

    private static Type typeArgumentOrDefault(Type[] typeArguments, int index, Type defaultType) {
        return index < typeArguments.length ? typeArguments[index] : defaultType;
    }

    private static Class<?> rawClassOf(Type type) {
        if (type instanceof Class<?> rawType) {
            return rawType;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return (Class<?>) parameterizedType.getRawType();
        }
        if (type instanceof GenericArrayType arrayType) {
            return rawClassOf(arrayType.getGenericComponentType()).arrayType();
        }
        return Object.class;
    }

    private static boolean isPlatformType(Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        return loader == null || loader == ClassLoader.getPlatformClassLoader();
    }
}
