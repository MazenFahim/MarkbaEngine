package com.markbaengine.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MVC Model helper.
 * This is the only class that knows how to open a SQL Server connection and
 * convert a ResultSet into Java rows.
 */
public final class Database {
    private static final String DEFAULT_SQL_SERVER_URL =
            "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=FleetMaintenanceDB;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;";

    private static final String URL = System.getProperty("markbaengine.db.url", DEFAULT_SQL_SERVER_URL);
    private static final String USER = System.getProperty("markbaengine.db.user", "fleet_app");
    private static final String PASSWORD = System.getProperty("markbaengine.db.password", "fleetApp@12345");

    private Database() {
    }

    public static void testConnection() {
        try (Connection ignored = getConnection()) {
            // Connection successful.
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to connect to SQL Server database", ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static List<Map<String, Object>> selectRows(String sql) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return readRows(resultSet);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to run SELECT query", ex);
        }
    }

    public static int executeUpdate(String sql, Object... parameters) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to execute SQL update", ex);
        }
    }

    private static List<Map<String, Object>> readRows(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
}
