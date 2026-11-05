package org.example.generator.samples;

import org.example.generator.Generatable;

@Generatable
public class Dog extends Animal {
    private final Color color;

    public Dog(String name, Color color) {
        super(name);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
