package org.example.generator;

import org.example.classes.BinaryTreeNode;
import org.example.classes.Cart;
import org.example.classes.Example;
import org.example.classes.Product;
import org.example.classes.Rectangle;
import org.example.classes.Shape;
import org.example.classes.Triangle;
import org.example.generator.samples.Animal;
import org.example.generator.samples.Book;
import org.example.generator.samples.Library;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserObjectsTest {
    private final Generator generator = new Generator();

    @Test
    void generatesExample() {
        assertInstanceOf(Example.class, generator.generateValueOfType(Example.class));
    }

    @Test
    void generatesProduct() {
        Product product = generator.generateValueOfType(Product.class);

        assertNotNull(product.getName());
        assertFalse(product.getName().isEmpty());
    }

    @Test
    void generatesCartWithProducts() {
        Cart cart = generator.generateValueOfType(Cart.class);

        assertFalse(cart.getItems().isEmpty());
        cart.getItems().forEach(item -> assertInstanceOf(Product.class, item));
    }

    @Test
    void generatesRectangleAndTriangle() {
        assertInstanceOf(Rectangle.class, generator.generateValueOfType(Rectangle.class));
        assertInstanceOf(Triangle.class, generator.generateValueOfType(Triangle.class));
    }

    @Test
    void generatesShapeFromBothImplementations() {
        Set<Class<?>> implementations = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            Shape shape = generator.generateValueOfType(Shape.class);
            assertNotNull(shape);
            implementations.add(shape.getClass());
        }

        assertEquals(Set.of(Rectangle.class, Triangle.class), implementations);
    }

    @Test
    void generatesShapeArray() {
        Shape[] shapes = generator.generateValueOfType(Shape[].class);

        assertTrue(shapes.length > 0);
        for (Shape shape : shapes) {
            assertTrue(shape.getArea() > 0);
        }
    }

    @RepeatedTest(10)
    void binaryTreeDepthIsLimited() {
        BinaryTreeNode root = generator.generateValueOfType(BinaryTreeNode.class);

        assertNotNull(root.getData());
        assertTrue(depth(root) <= Generator.DEFAULT_MAX_DEPTH + 1);
    }

    @Test
    void zeroMaxDepthGivesSingleNode() {
        BinaryTreeNode root = new Generator(new Random(), 0).generateValueOfType(BinaryTreeNode.class);

        assertEquals(1, depth(root));
    }

    @Test
    void generatesAbstractClassImplementation() {
        Animal animal = generator.generateValueOfType(Animal.class);

        assertNotNull(animal.getName());
    }

    @Test
    void generatesRecordWithArrayComponent() {
        Book book = generator.generateValueOfType(Book.class);

        assertNotNull(book.title());
        assertTrue(book.isbn().length > 0);
    }

    @Test
    void generatesNestedGenericContainers() {
        Library library = generator.generateValueOfType(Library.class);

        assertFalse(library.getBooksByShelf().isEmpty());
        library.getBooksByShelf().forEach((shelf, books) -> {
            assertInstanceOf(String.class, shelf);
            assertFalse(books.isEmpty());
            books.forEach(book -> assertInstanceOf(Book.class, book));
        });
        assertFalse(library.getColors().isEmpty());
        assertTrue(library.getPets().length > 0);
    }

    private static int depth(BinaryTreeNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(depth(node.getLeft()), depth(node.getRight()));
    }
}
