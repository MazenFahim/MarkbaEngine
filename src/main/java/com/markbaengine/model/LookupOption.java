package com.markbaengine.model;

/**
 * Represents one item in a ComboBox that stores an ID but displays readable text.
 */
public class LookupOption {
    private final Object id;
    private final String label;

    public LookupOption(Object id, String label) {
        this.id = id;
        this.label = label;
    }

    public Object getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
