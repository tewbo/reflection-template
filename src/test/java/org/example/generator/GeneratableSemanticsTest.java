package org.example.generator;

import org.example.generator.samples.Account;
import org.example.generator.samples.Animal;
import org.example.generator.samples.Car;
import org.example.generator.samples.Cat;
import org.example.generator.samples.Dog;
import org.example.generator.samples.Fish;
import org.example.generator.samples.Payment;
import org.example.generator.samples.Plain;
import org.example.generator.samples.Vehicle;
import org.example.generator.samples.explicit.CardPayment;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratableSemanticsTest {
    private final Generator generator = new Generator();

    @Test
    void classWithoutAnnotationIsRejected() {
        GenerationException exception = assertThrows(GenerationException.class,
                () -> generator.generateValueOfType(Plain.class));

        assertTrue(exception.getMessage().contains("@Generatable"));
    }

    @Test
    void interfaceWithoutAnnotationIsRejectedEvenIfImplementationIsGeneratable() {
        assertThrows(GenerationException.class, () -> generator.generateValueOfType(Vehicle.class));
        assertInstanceOf(Car.class, generator.generateValueOfType(Car.class));
    }

    @Test
    void onlyAnnotatedImplementationsAreChosen() {
        Set<Class<?>> implementations = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            implementations.add(generator.generateValueOfType(Animal.class).getClass());
        }

        assertEquals(Set.of(Dog.class, Cat.class), implementations);
    }

    @Test
    void unannotatedSubclassCannotBeGeneratedDirectly() {
        assertThrows(GenerationException.class, () -> generator.generateValueOfType(Fish.class));
    }

    @RepeatedTest(10)
    void explicitImplementationsOverridePackageScan() {
        assertInstanceOf(CardPayment.class, generator.generateValueOfType(Payment.class));
    }

    @RepeatedTest(10)
    void annotatedConstructorIsPreferredEvenIfPrivate() {
        Account account = generator.generateValueOfType(Account.class);

        assertTrue(account.isCreatedByGenerator());
    }
}
