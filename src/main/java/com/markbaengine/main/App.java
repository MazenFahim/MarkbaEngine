package com.markbaengine.main;

import com.markbaengine.controller.ScreenNavigator;
import com.markbaengine.model.Database;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point.
 *
 * MVC startup flow:
 * App -> Model connection test -> Controller navigation -> View dashboard.fxml.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        Database.testConnection();
        ScreenNavigator.init(primaryStage);
        ScreenNavigator.showDashboard();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
