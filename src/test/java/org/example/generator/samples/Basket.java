package org.example.generator.samples;

import org.example.generator.Generatable;

import java.util.AbstractList;
import java.util.List;

@Generatable
public class Basket extends AbstractList<String> {
    private final List<String> items;

    public Basket(List<String> items) {
        this.items = List.copyOf(items);
    }

    @Override
    public String get(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
