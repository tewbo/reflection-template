package org.example.generator.samples;

import org.example.generator.Generatable;

@Generatable(implementations = {Dog.class, Cat.class})
public abstract class Animal {
    private final String name;

    protected Animal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
