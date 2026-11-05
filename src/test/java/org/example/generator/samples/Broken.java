package org.example.generator.samples;

import org.example.generator.Generatable;

@Generatable
public class Broken {
    public Broken(int value) {
        throw new IllegalStateException("broken");
    }
}
