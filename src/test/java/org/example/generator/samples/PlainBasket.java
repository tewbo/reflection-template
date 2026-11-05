package org.example.generator.samples;

import java.util.AbstractList;

public class PlainBasket extends AbstractList<String> {
    @Override
    public String get(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int size() {
        return 0;
    }
}
