package org.example.generator.samples;

import org.example.generator.Generatable;
import org.example.generator.Range;

@Generatable
public class Cat extends Animal {
    private final int lives;

    public Cat(String name, @Range(min = 1, max = 9) int lives) {
        super(name);
        this.lives = lives;
    }

    public int getLives() {
        return lives;
    }
}
