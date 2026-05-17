package com.markbaengine.main;

import com.markbaengine.db.DatabaseInitializer;
import com.markbaengine.util.Navigation;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point for MarkbaEngine.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseInitializer.initialize();
        Navigation.init(primaryStage);
        Navigation.showDashboard();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
