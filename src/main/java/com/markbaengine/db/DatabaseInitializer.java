package com.markbaengine.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Validates the SQL Server database connection on application startup.
 * The database schema and seed data are created manually in SQL Server
 * using the DDL and DML scripts.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    /**
     * Called on application startup.
     * It only checks that the app can connect to SQL Server.
     */
    public static void initialize() {
        testConnection();
    }

    /**
     * Checks whether the SQL Server database is reachable.
     */
    public static void testConnection() {
        try (Connection ignored = DBConnection.getConnection()) {
            // Connection successful.
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to connect to SQL Server database", ex);
        }
    }
}