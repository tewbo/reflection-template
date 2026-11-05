package org.example.generator;

import org.example.generator.samples.Account;
import org.example.generator.samples.Animal;
import org.example.generator.samples.Basket;
import org.example.generator.samples.Car;
import org.example.generator.samples.Cat;
import org.example.generator.samples.Dog;
import org.example.generator.samples.Fish;
import org.example.generator.samples.Plain;
import org.example.generator.samples.PlainBasket;
import org.example.generator.samples.Vehicle;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void allListedImplementationsAreChosen() {
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

    @Test
    void annotatedUserCollectionIsCreatedThroughItsConstructor() {
        Basket basket = generator.generateValueOfType(Basket.class);

        assertFalse(basket.isEmpty());
        basket.forEach(item -> assertInstanceOf(String.class, item));
    }

    @Test
    void unannotatedUserCollectionIsRejectedAsNotGeneratable() {
        GenerationException exception = assertThrows(GenerationException.class,
                () -> generator.generateValueOfType(PlainBasket.class));

        assertTrue(exception.getMessage().contains("@Generatable"));
    }

    @RepeatedTest(10)
    void annotatedConstructorIsPreferredEvenIfPrivate() {
        Account account = generator.generateValueOfType(Account.class);

        assertTrue(account.isCreatedByGenerator());
    }
}
