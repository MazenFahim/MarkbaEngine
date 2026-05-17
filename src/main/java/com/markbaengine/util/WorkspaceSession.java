package com.markbaengine.util;

import java.nio.file.Path;

public final class WorkspaceSession {
    private static Path sqlFilePath;
    private static Path databasePath;
    private static boolean loaded;
    private static String statusMessage = "No workspace selected yet.";

    private WorkspaceSession() {
    }

    public static void setWorkspace(Path sqlFilePath, Path databasePath, String statusMessage) {
        WorkspaceSession.sqlFilePath = sqlFilePath;
        WorkspaceSession.databasePath = databasePath;
        WorkspaceSession.statusMessage = statusMessage;
        WorkspaceSession.loaded = true;
    }

    public static Path getSqlFilePath() {
        return sqlFilePath;
    }

    public static Path getDatabasePath() {
        return databasePath;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static String getStatusMessage() {
        return statusMessage;
    }

    public static void clear() {
        sqlFilePath = null;
        databasePath = null;
        loaded = false;
        statusMessage = "No workspace selected yet.";
    }
}
