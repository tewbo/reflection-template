package org.example.generator;

import org.example.generator.samples.Color;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicTypesTest {
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = Map.of(
            Integer.class, int.class,
            Long.class, long.class,
            Short.class, short.class,
            Byte.class, byte.class,
            Double.class, double.class,
            Float.class, float.class,
            Boolean.class, boolean.class,
            Character.class, char.class);

    private final Generator generator = new Generator();

    @ParameterizedTest
    @ValueSource(classes = {int.class, long.class, short.class, byte.class, double.class, float.class,
            boolean.class, char.class})
    void generatesPrimitivesAsMatchingWrappers(Class<?> primitive) {
        Object value = generator.generateValueOfType(primitive);

        assertNotNull(value);
        assertEquals(primitive, WRAPPER_TO_PRIMITIVE.get(value.getClass()));
    }

    @ParameterizedTest
    @ValueSource(classes = {Integer.class, Long.class, Short.class, Byte.class, Double.class, Float.class,
            Boolean.class, Character.class})
    void generatesWrappers(Class<?> wrapper) {
        assertInstanceOf(wrapper, generator.generateValueOfType(wrapper));
    }

    @RepeatedTest(20)
    void numbersStayInDefaultBounds() {
        int number = generator.generateValueOfType(Integer.class);
        double real = generator.generateValueOfType(double.class);

        assertTrue(Math.abs(number) <= Generator.DEFAULT_NUMBER_BOUND);
        assertTrue(Math.abs(real) <= Generator.DEFAULT_NUMBER_BOUND);
    }

    @Test
    void generatedValuesAreNotConstant() {
        Set<Integer> values = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            values.add(generator.generateValueOfType(Integer.class));
        }

        assertTrue(values.size() > 1);
    }

    @RepeatedTest(20)
    void generatesNonEmptyStrings() {
        String text = generator.generateValueOfType(String.class);

        assertTrue(text.length() >= Generator.MIN_STRING_LENGTH && text.length() <= Generator.MAX_STRING_LENGTH);
        assertTrue(text.chars().allMatch(Character::isLetterOrDigit));
    }

    @Test
    void generatesAllEnumConstantsEventually() {
        Set<Color> colors = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            colors.add(generator.generateValueOfType(Color.class));
        }

        assertEquals(Set.of(Color.values()), colors);
    }

    @Test
    void generatesPrimitiveArray() {
        int[] numbers = generator.generateValueOfType(int[].class);

        assertTrue(numbers.length >= Generator.MIN_CONTAINER_SIZE && numbers.length <= Generator.MAX_CONTAINER_SIZE);
    }

    @Test
    void generatesObjectArrays() {
        String[] texts = generator.generateValueOfType(String[].class);
        Color[] colors = generator.generateValueOfType(Color[].class);

        assertTrue(texts.length > 0);
        assertTrue(colors.length > 0);
        for (String text : texts) {
            assertFalse(text.isEmpty());
        }
        for (Color color : colors) {
            assertNotNull(color);
        }
    }

    @Test
    void generatesMultidimensionalArray() {
        int[][] matrix = generator.generateValueOfType(int[][].class);

        assertTrue(matrix.length > 0);
        for (int[] row : matrix) {
            assertTrue(row.length > 0);
        }
    }

    @Test
    void generatesRawListWithIntegers() {
        List<?> items = generator.generateValueOfType(List.class);

        assertFalse(items.isEmpty());
        items.forEach(item -> assertInstanceOf(Integer.class, item));
    }

    @Test
    void generatesSet() {
        Set<?> items = generator.generateValueOfType(Set.class);

        assertFalse(items.isEmpty());
    }

    @Test
    void generatesRawMapWithStringKeysAndIntegerValues() {
        Map<?, ?> entries = generator.generateValueOfType(Map.class);

        assertFalse(entries.isEmpty());
        entries.forEach((key, value) -> {
            assertInstanceOf(String.class, key);
            assertInstanceOf(Integer.class, value);
        });
    }
}
