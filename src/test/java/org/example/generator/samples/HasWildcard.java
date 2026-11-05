package org.example.generator.samples;

import org.example.generator.Generatable;

import java.util.List;

@Generatable
public class HasWildcard {
    public HasWildcard(List<?> items) {
    }
}
