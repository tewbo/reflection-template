package org.example.generator.samples;

import org.example.generator.Generatable;
import org.example.generator.Range;

@Generatable
public record Book(String title, @Range(min = 1, max = 2000) int pages, char[] isbn) {
}
