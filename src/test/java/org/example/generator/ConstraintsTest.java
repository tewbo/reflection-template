package org.example.generator;

import org.example.classes.Product;
import org.example.classes.Rectangle;
import org.example.classes.Triangle;
import org.example.generator.samples.Cat;
import org.example.generator.samples.Segment;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintsTest {
    private final Generator generator = new Generator();

    @RepeatedTest(50)
    void rectangleHasPositiveSidesInRange() {
        Rectangle rectangle = generator.generateValueOfType(Rectangle.class);

        assertTrue(rectangle.getLength() >= 0.1 && rectangle.getLength() <= 100);
        assertTrue(rectangle.getWidth() >= 0.1 && rectangle.getWidth() <= 100);
        assertTrue(rectangle.getArea() > 0);
    }

    @RepeatedTest(50)
    void triangleSidesSatisfyTriangleInequality() {
        Triangle triangle = generator.generateValueOfType(Triangle.class);

        assertTrue(Triangle.isValid(triangle.getSideA(), triangle.getSideB(), triangle.getSideC()));
        assertTrue(triangle.getArea() > 0);
    }

    @RepeatedTest(50)
    void productPriceIsPositive() {
        Product product = generator.generateValueOfType(Product.class);

        assertTrue(product.getPrice() > 0);
    }

    @RepeatedTest(50)
    void integerRangeIsRespected() {
        Cat cat = generator.generateValueOfType(Cat.class);

        assertTrue(cat.getLives() >= 1 && cat.getLives() <= 9);
    }

    @RepeatedTest(50)
    void crossParameterConstraintIsSatisfiedByRetries() {
        Segment segment = generator.generateValueOfType(Segment.class);

        assertTrue(segment.getStart() <= segment.getEnd());
    }
}
