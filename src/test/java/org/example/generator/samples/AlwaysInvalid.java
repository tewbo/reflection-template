package org.example.generator.samples;

import org.example.generator.Generatable;

@Generatable
public class AlwaysInvalid {
    public AlwaysInvalid(int value) {
        throw new IllegalArgumentException("never valid: " + value);
    }
}
