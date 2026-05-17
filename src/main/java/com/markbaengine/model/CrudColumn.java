package com.markbaengine.model;

import java.util.List;

/**
 * Metadata used by the generic table editor screen.
 */
public class CrudColumn {

    public enum ControlType {
        TEXT,
        INTEGER,
        DECIMAL,
        DATE,
        ENUM,
        LOOKUP
    }

    private final String name;
    private final String label;
    private final ControlType controlType;
    private final boolean editable;
    private final boolean required;
    private final boolean primaryKey;
    private final boolean generated;
    private final String lookupTable;
    private final String lookupIdColumn;
    private final String lookupLabelColumn;
    private final List<String> enumValues;

    private CrudColumn(Builder builder) {
        this.name = builder.name;
        this.label = builder.label;
        this.controlType = builder.controlType;
        this.editable = builder.editable;
        this.required = builder.required;
        this.primaryKey = builder.primaryKey;
        this.generated = builder.generated;
        this.lookupTable = builder.lookupTable;
        this.lookupIdColumn = builder.lookupIdColumn;
        this.lookupLabelColumn = builder.lookupLabelColumn;
        this.enumValues = builder.enumValues;
    }

    public static Builder builder(String name, String label, ControlType controlType) {
        return new Builder(name, label, controlType);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public ControlType getControlType() {
        return controlType;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isGenerated() {
        return generated;
    }

    public String getLookupTable() {
        return lookupTable;
    }

    public String getLookupIdColumn() {
        return lookupIdColumn;
    }

    public String getLookupLabelColumn() {
        return lookupLabelColumn;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public static class Builder {
        private final String name;
        private final String label;
        private final ControlType controlType;
        private boolean editable = true;
        private boolean required;
        private boolean primaryKey;
        private boolean generated;
        private String lookupTable;
        private String lookupIdColumn;
        private String lookupLabelColumn;
        private List<String> enumValues = List.of();

        private Builder(String name, String label, ControlType controlType) {
            this.name = name;
            this.label = label;
            this.controlType = controlType;
        }

        public Builder editable(boolean editable) {
            this.editable = editable;
            return this;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder primaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        public Builder generated(boolean generated) {
            this.generated = generated;
            return this;
        }

        public Builder lookup(String table, String idColumn, String labelColumn) {
            this.lookupTable = table;
            this.lookupIdColumn = idColumn;
            this.lookupLabelColumn = labelColumn;
            return this;
        }

        public Builder enumValues(List<String> enumValues) {
            this.enumValues = enumValues;
            return this;
        }

        public CrudColumn build() {
            return new CrudColumn(this);
        }
    }
}
