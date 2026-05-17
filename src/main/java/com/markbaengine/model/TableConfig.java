package com.markbaengine.model;

import java.util.List;

public class TableConfig {
    private final String key;
    private final String title;
    private final String tableName;
    private final String primaryKeyColumn;
    private final String selectSql;
    private final List<CrudColumn> columns;

    public TableConfig(String key, String title, String tableName, String primaryKeyColumn,
                       String selectSql, List<CrudColumn> columns) {
        this.key = key;
        this.title = title;
        this.tableName = tableName;
        this.primaryKeyColumn = primaryKeyColumn;
        this.selectSql = selectSql;
        this.columns = columns;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getTableName() {
        return tableName;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public String getSelectSql() {
        return selectSql;
    }

    public List<CrudColumn> getColumns() {
        return columns;
    }

    public List<CrudColumn> getEditableColumns() {
        return columns.stream()
                .filter(CrudColumn::isEditable)
                .filter(column -> !column.isGenerated())
                .toList();
    }
}
