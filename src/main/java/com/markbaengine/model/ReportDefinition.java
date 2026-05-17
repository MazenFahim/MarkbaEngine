package com.markbaengine.model;

public class ReportDefinition {
    private final String key;
    private final String title;
    private final String sql;

    public ReportDefinition(String key, String title, String sql) {
        this.key = key;
        this.title = title;
        this.sql = sql;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return title;
    }
}
