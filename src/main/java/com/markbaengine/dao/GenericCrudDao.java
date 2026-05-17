package com.markbaengine.dao;

import com.markbaengine.db.DBConnection;
import com.markbaengine.model.CrudColumn;
import com.markbaengine.model.TableConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic DAO for pre-configured application tables.
 * Table/column names come only from trusted TableConfig objects, while values use PreparedStatement parameters.
 */
public class GenericCrudDao {

    public List<Map<String, Object>> findAll(TableConfig config) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(config.getSelectSql())) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }
                rows.add(row);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load table data for " + config.getTitle(), ex);
        }
        return rows;
    }

    public void insert(TableConfig config, Map<String, Object> values) {
        List<CrudColumn> columns = config.getEditableColumns();
        String columnNames = columns.stream().map(CrudColumn::getName).collect(Collectors.joining(", "));
        String placeholders = columns.stream().map(column -> "?").collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + config.getTableName() + " (" + columnNames + ") VALUES (" + placeholders + ")";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindValues(statement, columns, values);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to insert into " + config.getTitle(), ex);
        }
    }

    public void update(TableConfig config, Object primaryKeyValue, Map<String, Object> values) {
        List<CrudColumn> columns = config.getEditableColumns().stream()
                .filter(column -> !column.isPrimaryKey())
                .toList();
        String setClause = columns.stream()
                .map(column -> column.getName() + " = ?")
                .collect(Collectors.joining(", "));
        String sql = "UPDATE " + config.getTableName() + " SET " + setClause + " WHERE " + config.getPrimaryKeyColumn() + " = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindValues(statement, columns, values);
            statement.setObject(columns.size() + 1, primaryKeyValue);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update " + config.getTitle(), ex);
        }
    }

    public void delete(TableConfig config, Object primaryKeyValue) {
        String sql = "DELETE FROM " + config.getTableName() + " WHERE " + config.getPrimaryKeyColumn() + " = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, primaryKeyValue);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete from " + config.getTitle(), ex);
        }
    }

    private void bindValues(PreparedStatement statement, List<CrudColumn> columns, Map<String, Object> values) throws SQLException {
        for (int i = 0; i < columns.size(); i++) {
            CrudColumn column = columns.get(i);
            Object value = values.get(column.getName());
            if (value instanceof LocalDate localDate) {
                statement.setDate(i + 1, Date.valueOf(localDate));
            } else {
                statement.setObject(i + 1, value);
            }
        }
    }
}
