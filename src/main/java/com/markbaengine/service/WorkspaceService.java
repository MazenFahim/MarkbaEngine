package com.markbaengine.service;

import com.markbaengine.db.DBConnection;
import com.markbaengine.db.DatabaseInitializer;
import com.markbaengine.model.SchemaValidationResult;
import com.markbaengine.util.WorkspaceSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates or loads the current database workspace used by table screens and reports.
 */
public class WorkspaceService {
    private final SqlSchemaValidatorService validatorService = new SqlSchemaValidatorService();

    public SchemaValidationResult validateSqlFile(Path sqlFile) {
        return validatorService.validateFile(sqlFile);
    }

    public Path loadSqlFileAsWorkspace(Path sqlFile) {
        SchemaValidationResult validationResult = validateSqlFile(sqlFile);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException(validationResult.toDisplayText());
        }

        try {
            String script = Files.readString(sqlFile, StandardCharsets.UTF_8);
            Path workspaceDir = Path.of("workspaces").toAbsolutePath().normalize();
            Files.createDirectories(workspaceDir);
            String baseName = stripExtension(sqlFile.getFileName().toString());
            Path databasePath = uniqueDatabasePath(workspaceDir, baseName);
            recreateDatabase(databasePath, script);
            DBConnection.setWorkspaceDatabase(databasePath);
            WorkspaceSession.setWorkspace(sqlFile.toAbsolutePath().normalize(), databasePath, "Loaded from SQL file: " + sqlFile.getFileName());
            return databasePath;
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read or prepare the SQL workspace file.", ex);
        }
    }

    public Path createEmptySqlWorkspace(Path sqlOutputFile) {
        if (sqlOutputFile == null) {
            throw new IllegalArgumentException("Choose where to save the empty SQL file first.");
        }
        if (!sqlOutputFile.getFileName().toString().toLowerCase().endsWith(".sql")) {
            sqlOutputFile = sqlOutputFile.resolveSibling(sqlOutputFile.getFileName() + ".sql");
        }

        String schema = DatabaseInitializer.getBundledWorkspaceSchema();
        SchemaValidationResult validationResult = validatorService.validateSql(schema);
        if (!validationResult.isValid()) {
            throw new IllegalStateException("The bundled empty schema is invalid. " + validationResult.toDisplayText());
        }

        try {
            Path absoluteSqlFile = sqlOutputFile.toAbsolutePath().normalize();
            Path parent = absoluteSqlFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(absoluteSqlFile, schema, StandardCharsets.UTF_8);

            Path databasePath = absoluteSqlFile.resolveSibling(stripExtension(absoluteSqlFile.getFileName().toString()) + ".db");
            recreateDatabase(databasePath, schema);
            DBConnection.setWorkspaceDatabase(databasePath);
            WorkspaceSession.setWorkspace(absoluteSqlFile, databasePath, "Created empty Urban Fleet database workspace.");
            return databasePath;
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create the empty SQL workspace.", ex);
        }
    }

    public String getRequiredTablesText() {
        return String.join(", ", validatorService.getRequiredTableNames());
    }

    private void recreateDatabase(Path databasePath, String script) {
        try {
            Files.deleteIfExists(databasePath);
            DBConnection.setWorkspaceDatabase(databasePath);
            try (Connection connection = DBConnection.getConnection();
                 Statement statement = connection.createStatement()) {
                for (String sql : DatabaseInitializer.splitSqlStatements(script)) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        statement.execute(trimmed);
                    }
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not reset workspace database file.", ex);
        } catch (SQLException ex) {
            throw new IllegalStateException("The selected SQL file matched the schema names, but SQLite could not execute it. Use the MarkbaEngine-generated SQLite schema file for this version.", ex);
        }
    }

    private Path uniqueDatabasePath(Path workspaceDir, String baseName) {
        Path candidate = workspaceDir.resolve(baseName + ".db");
        int counter = 2;
        while (Files.exists(candidate)) {
            candidate = workspaceDir.resolve(baseName + "-" + counter + ".db");
            counter++;
        }
        return candidate;
    }

    private String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}
