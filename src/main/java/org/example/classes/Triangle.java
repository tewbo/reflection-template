package org.example.classes;

import org.example.generator.Generatable;
import org.example.generator.Range;

@Generatable
public class Triangle implements Shape {
    private final double sideA;
    private final double sideB;
    private final double sideC;

    public Triangle(@Range(min = 0.1, max = 100) double sideA,
                    @Range(min = 0.1, max = 100) double sideB,
                    @Range(min = 0.1, max = 100) double sideC) {
        if (!isValid(sideA, sideB, sideC)) {
            throw new IllegalArgumentException("Sides do not form a triangle: " + sideA + ", " + sideB + ", " + sideC);
        }
        this.sideA = sideA;
        this.sideB = sideB;
        this.sideC = sideC;
    }

    public static boolean isValid(double sideA, double sideB, double sideC) {
        return sideA > 0 && sideB > 0 && sideC > 0
                && sideA + sideB > sideC
                && sideA + sideC > sideB
                && sideB + sideC > sideA;
    }

    public double getSideA() {
        return sideA;
    }

    public double getSideB() {
        return sideB;
    }

    public double getSideC() {
        return sideC;
    }

    @Override
    public double getArea() {
        double s = (sideA + sideB + sideC) / 2;
        return Math.sqrt(s * (s - sideA) * (s - sideB) * (s - sideC));
    }

    @Override
    public double getPerimeter() {
        return sideA + sideB + sideC;
    }
}
