package com.markbaengine.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Controller helper responsible only for changing JavaFX screens.
 */
public final class ScreenNavigator {
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 760;
    private static final double MIN_WIDTH = 1100;
    private static final double MIN_HEIGHT = 720;

    private static Stage primaryStage;
    private static boolean firstSceneShown = false;

    private ScreenNavigator() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("MarkbaEngine");
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
    }

    public static void showDashboard() {
        show("dashboard.fxml", "/com/markbaengine/styles/screens/dashboard.css");
    }

    public static void showReports() {
        show("reports.fxml", "/com/markbaengine/styles/screens/reports.css");
    }

    public static void showTableEditor(String tableKey) {
        try {
            FXMLLoader loader = new FXMLLoader(resource("/com/markbaengine/view/table-editor.fxml"));
            Parent root = loader.load();

            TableEditorController controller = loader.getController();
            controller.loadTable(tableKey);

            setScene(root, "/com/markbaengine/styles/screens/table-editor.css");
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to open table editor", ex);
        }
    }

    private static void show(String fxmlFile, String extraStylesheet) {
        try {
            FXMLLoader loader = new FXMLLoader(resource("/com/markbaengine/view/" + fxmlFile));
            Parent root = loader.load();
            setScene(root, extraStylesheet);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to open screen: " + fxmlFile, ex);
        }
    }

    private static void setScene(Parent root, String extraStylesheet) {
        if (primaryStage == null) {
            throw new IllegalStateException("ScreenNavigator.init(stage) must be called before showing screens.");
        }

        WindowState previousState = captureWindowState();
        Scene scene = createScene(root, previousState);
        addStylesheets(scene, extraStylesheet);

        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setScene(scene);

        if (!firstSceneShown) {
            firstSceneShown = true;
            primaryStage.show();
            centerInsideVisualBounds();
            return;
        }

        primaryStage.show();
        restoreWindowState(previousState);
    }

    private static Scene createScene(Parent root, WindowState previousState) {
        if (!firstSceneShown || previousState == null) {
            return new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        double width = clamp(previousState.width, MIN_WIDTH, getCurrentVisualBounds().getWidth());
        double height = clamp(previousState.height, MIN_HEIGHT, getCurrentVisualBounds().getHeight());
        return new Scene(root, width, height);
    }

    private static void addStylesheets(Scene scene, String extraStylesheet) {
        scene.getStylesheets().add(css("/com/markbaengine/styles/variables.css"));
        scene.getStylesheets().add(css("/com/markbaengine/styles/base.css"));
        scene.getStylesheets().add(css("/com/markbaengine/styles/buttons.css"));
        scene.getStylesheets().add(css("/com/markbaengine/styles/forms.css"));
        scene.getStylesheets().add(css("/com/markbaengine/styles/tables.css"));
        scene.getStylesheets().add(css("/com/markbaengine/styles/layout.css"));
        scene.getStylesheets().add(css("/com/markbaengine/styles/app.css"));
        if (extraStylesheet != null) {
            scene.getStylesheets().add(css(extraStylesheet));
        }
    }

    private static WindowState captureWindowState() {
        if (primaryStage == null || !primaryStage.isShowing()) {
            return null;
        }
        return new WindowState(
                primaryStage.getX(),
                primaryStage.getY(),
                primaryStage.getWidth(),
                primaryStage.getHeight(),
                primaryStage.isMaximized(),
                primaryStage.isFullScreen()
        );
    }

    private static void restoreWindowState(WindowState state) {
        if (state == null) {
            return;
        }
        if (state.fullScreen) {
            Platform.runLater(() -> primaryStage.setFullScreen(true));
            return;
        }
        if (state.maximized) {
            Platform.runLater(() -> {
                primaryStage.setMaximized(true);
                keepStageInsideVisualBounds();
            });
            return;
        }

        Rectangle2D visualBounds = getVisualBoundsFor(state.x, state.y, state.width, state.height);
        double width = clamp(state.width, MIN_WIDTH, visualBounds.getWidth());
        double height = clamp(state.height, MIN_HEIGHT, visualBounds.getHeight());
        double x = clamp(state.x, visualBounds.getMinX(), visualBounds.getMaxX() - width);
        double y = clamp(state.y, visualBounds.getMinY(), visualBounds.getMaxY() - height);

        primaryStage.setMaximized(false);
        primaryStage.setX(x);
        primaryStage.setY(y);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);

        Platform.runLater(ScreenNavigator::keepStageInsideVisualBounds);
    }

    private static void centerInsideVisualBounds() {
        Rectangle2D visualBounds = getCurrentVisualBounds();
        double width = clamp(primaryStage.getWidth(), MIN_WIDTH, visualBounds.getWidth());
        double height = clamp(primaryStage.getHeight(), MIN_HEIGHT, visualBounds.getHeight());
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.setX(visualBounds.getMinX() + (visualBounds.getWidth() - width) / 2.0);
        primaryStage.setY(visualBounds.getMinY() + (visualBounds.getHeight() - height) / 2.0);
    }

    private static void keepStageInsideVisualBounds() {
        if (primaryStage == null || primaryStage.isFullScreen()) {
            return;
        }
        Rectangle2D visualBounds = getCurrentVisualBounds();
        if (!primaryStage.isMaximized()) {
            double width = Math.min(primaryStage.getWidth(), visualBounds.getWidth());
            double height = Math.min(primaryStage.getHeight(), visualBounds.getHeight());
            double x = clamp(primaryStage.getX(), visualBounds.getMinX(), visualBounds.getMaxX() - width);
            double y = clamp(primaryStage.getY(), visualBounds.getMinY(), visualBounds.getMaxY() - height);
            primaryStage.setX(x);
            primaryStage.setY(y);
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
        }
    }

    private static Rectangle2D getCurrentVisualBounds() {
        return getVisualBoundsFor(
                primaryStage == null ? 0 : primaryStage.getX(),
                primaryStage == null ? 0 : primaryStage.getY(),
                primaryStage == null ? DEFAULT_WIDTH : primaryStage.getWidth(),
                primaryStage == null ? DEFAULT_HEIGHT : primaryStage.getHeight()
        );
    }

    private static Rectangle2D getVisualBoundsFor(double x, double y, double width, double height) {
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        return Screen.getScreensForRectangle(centerX, centerY, 1, 1)
                .stream()
                .findFirst()
                .orElse(Screen.getPrimary())
                .getVisualBounds();
    }

    private static double clamp(double value, double min, double max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }

    private static String css(String path) {
        return resource(path).toExternalForm();
    }

    private static java.net.URL resource(String path) {
        return Objects.requireNonNull(ScreenNavigator.class.getResource(path), "Missing resource: " + path);
    }

    private record WindowState(double x, double y, double width, double height, boolean maximized, boolean fullScreen) {
    }
}
