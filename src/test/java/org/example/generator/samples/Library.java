package org.example.generator.samples;

import org.example.generator.Generatable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Generatable
public class Library {
    private final Map<String, List<Book>> booksByShelf;
    private final Set<Color> colors;
    private final Animal[] pets;

    public Library(Map<String, List<Book>> booksByShelf, Set<Color> colors, Animal[] pets) {
        this.booksByShelf = booksByShelf;
        this.colors = colors;
        this.pets = pets;
    }

    public Map<String, List<Book>> getBooksByShelf() {
        return booksByShelf;
    }

    public Set<Color> getColors() {
        return colors;
    }

    public Animal[] getPets() {
        return pets;
    }
}
