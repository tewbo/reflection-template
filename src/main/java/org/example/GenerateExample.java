package org.example;

import org.example.classes.BinaryTreeNode;
import org.example.classes.Cart;
import org.example.classes.Example;
import org.example.classes.Shape;
import org.example.generator.Generator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GenerateExample {
    public static void main(String[] args) {
        Generator generator = new Generator();
        System.out.println("Integer: " + generator.generateValueOfType(Integer.class));
        System.out.println("String: " + generator.generateValueOfType(String.class));
        System.out.println("int[]: " + Arrays.toString(generator.generateValueOfType(int[].class)));
        System.out.println("List: " + generator.generateValueOfType(List.class));
        System.out.println("Map: " + generator.generateValueOfType(Map.class));
        System.out.println("Example: " + generator.generateValueOfType(Example.class));
        Shape shape = generator.generateValueOfType(Shape.class);
        System.out.println("Shape: " + shape.getClass().getSimpleName() + ", area = " + shape.getArea());
        System.out.println("Cart items: " + generator.generateValueOfType(Cart.class).getItems().size());
        System.out.println("BinaryTreeNode root: " + generator.generateValueOfType(BinaryTreeNode.class).getData());
    }
}
