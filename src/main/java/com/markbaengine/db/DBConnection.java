package com.markbaengine.db;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens Microsoft SQL Server database connections.
 *
 * The application connects to the FleetMaintenanceDB database in SQL Server
 * using a SQL Server login created for the app.
 */
public final class DBConnection {

    private static final String DEFAULT_SQL_SERVER_URL =
            "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=FleetMaintenanceDB;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;";

    private static String databaseUrl =
            System.getProperty("markbaengine.db.url", DEFAULT_SQL_SERVER_URL);

    private static String databaseUser =
            System.getProperty("markbaengine.db.user", "fleet_app");

    private static String databasePassword =
            System.getProperty("markbaengine.db.password", "fleetApp@12345");

    private DBConnection() {
    }

    /**
     * Returns a connection to the FleetMaintenanceDB SQL Server database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
    }

    /**
     * Kept only for compatibility with older SQLite workspace code.
     * SQL Server does not use a local database file path.
     */
    public static void setWorkspaceDatabase(Path databasePath) {
        // No longer used after migrating to SQL Server.
    }

    /**
     * Resets the connection back to the default SQL Server database.
     */
    public static void useDefaultWorkspaceDatabase() {
        databaseUrl = System.getProperty("markbaengine.db.url", DEFAULT_SQL_SERVER_URL);
        databaseUser = System.getProperty("markbaengine.db.user", "fleet_app");
        databasePassword = System.getProperty("markbaengine.db.password", "FleetApp@12345");
    }

    public static String getWorkspaceUrl() {
        return databaseUrl;
    }

    /**
     * Kept only for compatibility with older code that expected a SQLite path.
     */
    public static Path getWorkspacePath() {
        return Path.of("FleetMaintenanceDB").toAbsolutePath().normalize();
    }
}