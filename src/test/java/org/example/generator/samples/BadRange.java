package org.example.generator.samples;

import org.example.generator.Generatable;
import org.example.generator.Range;

@Generatable
public class BadRange {
    public BadRange(@Range(min = 10, max = 1) int value) {
    }
}
