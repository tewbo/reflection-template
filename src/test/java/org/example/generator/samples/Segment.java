package org.example.generator.samples;

import org.example.generator.Generatable;
import org.example.generator.Range;

@Generatable
public class Segment {
    private final int start;
    private final int end;

    public Segment(@Range(min = 0, max = 100) int start, @Range(min = 0, max = 100) int end) {
        if (start > end) {
            throw new IllegalArgumentException("start > end");
        }
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
