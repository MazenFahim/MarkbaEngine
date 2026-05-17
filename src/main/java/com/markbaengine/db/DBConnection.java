package com.markbaengine.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens Microsoft SQL Server database connections.
 * The application connects to the FleetMaintenanceDB database in SQL Server
 * using a SQL Server login created for the app.
 */
public final class DBConnection {

    private static final String DEFAULT_SQL_SERVER_URL =
            "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=FleetMaintenanceDB;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;";

    private static final String databaseUrl =
            System.getProperty("markbaengine.db.url", DEFAULT_SQL_SERVER_URL);

    private static final String databaseUser =
            System.getProperty("markbaengine.db.user", "fleet_app");

    private static final String databasePassword =
            System.getProperty("markbaengine.db.password", "fleetApp@12345");
    /**
     * Returns a connection to the FleetMaintenanceDB SQL Server database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
    }
}