package org.example.generator;

import org.example.generator.samples.AlwaysInvalid;
import org.example.generator.samples.BadRange;
import org.example.generator.samples.Broken;
import org.example.generator.samples.EmptyEnum;
import org.example.generator.samples.HasUnsupported;
import org.example.generator.samples.Hidden;
import org.example.generator.samples.Unimplemented;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FailureTest {
    private final Generator generator = new Generator();

    @Test
    void unsupportedPlatformTypeFails() {
        GenerationException exception = assertThrows(GenerationException.class,
                () -> generator.generateValueOfType(Instant.class));

        assertTrue(exception.getMessage().contains("java.time.Instant"));
    }

    @Test
    void objectIsNotSupported() {
        assertThrows(GenerationException.class, () -> generator.generateValueOfType(Object.class));
    }

    @Test
    void voidIsRejected() {
        assertThrows(GenerationException.class, () -> generator.generateValueOfType(void.class));
        assertThrows(GenerationException.class, () -> generator.generateValueOfType(Void.class));
    }

    @Test
    void unsupportedCollectionFails() {
        assertThrows(GenerationException.class, () -> generator.generateValueOfType(SortedSet.class));
    }

    @Test
    void emptyEnumFails() {
        assertThrows(GenerationException.class, () -> generator.generateValueOfType(EmptyEnum.class));
    }

    @Test
    void interfaceWithoutImplementationsFails() {
        GenerationException exception = assertThrows(GenerationException.class,
                () -> generator.generateValueOfType(Unimplemented.class));

        assertTrue(exception.getMessage().contains("No concrete"));
    }

    @Test
    void classWithoutUsableConstructorsFails() {
        assertThrows(GenerationException.class, () -> generator.generateValueOfType(Hidden.class));
    }

    @Test
    void constructorRejectingAllArgumentsFailsAfterAttempts() {
        GenerationException exception = assertThrows(GenerationException.class,
                () -> generator.generateValueOfType(AlwaysInvalid.class));

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void constructorThrowingOtherExceptionFailsImmediately() {
        GenerationException exception = assertThrows(GenerationException.class,
                () -> generator.generateValueOfType(Broken.class));

        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    @Test
    void unsupportedParameterTypeIsReportedWithCause() {
        GenerationException exception = assertThrows(GenerationException.class,
                () -> generator.generateValueOfType(HasUnsupported.class));

        assertInstanceOf(GenerationException.class, exception.getCause());
    }

    @Test
    void invalidRangeFails() {
        assertThrows(GenerationException.class, () -> generator.generateValueOfType(BadRange.class));
    }

    @Test
    void nullTypeIsRejected() {
        assertThrows(NullPointerException.class, () -> generator.generateValueOfType((Type) null));
    }
}
