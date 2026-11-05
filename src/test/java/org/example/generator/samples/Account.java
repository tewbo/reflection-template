package org.example.generator.samples;

import org.example.generator.Generatable;

@Generatable
public class Account {
    private final String owner;
    private final boolean createdByGenerator;

    public Account() {
        this.owner = "manual";
        this.createdByGenerator = false;
    }

    @Generatable
    private Account(String owner) {
        this.owner = owner;
        this.createdByGenerator = true;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isCreatedByGenerator() {
        return createdByGenerator;
    }
}
