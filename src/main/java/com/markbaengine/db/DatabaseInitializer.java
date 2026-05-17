package com.markbaengine.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Validates the SQL Server database connection.
 *
 * The database schema and seed data are now created manually in SQL Server
 * using the DDL and DML scripts, so the Java application should not run
 * SQLite schema/seed scripts anymore.
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

    /**
     * Kept temporarily for old code compatibility.
     * SQL Server scripts should be executed from SSMS, not from the app.
     */
    public static void runWorkspaceScript(String script) {
        throw new UnsupportedOperationException(
                "Workspace SQL scripts are no longer supported after migrating to SQL Server."
        );
    }

    /**
     * Kept temporarily for old code compatibility.
     */
    public static String getBundledWorkspaceSchema() {
        return "";
    }

    /**
     * Kept temporarily for old code compatibility.
     */
    public static String[] splitSqlStatements(String script) {
        return script.split(";\\s*(\\r?\\n|$)");
    }
}