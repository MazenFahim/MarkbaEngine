package com.markbaengine.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * MVC Controller for dashboard.fxml.
 *
 * It does not run SQL. It only reacts to dashboard buttons and asks the
 * navigator to open the correct view.
 */
public class DashboardController {
    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        welcomeLabel.setText("Ready to manage fleet data");
    }

    @FXML private void openDashboard() { ScreenNavigator.showDashboard(); }
    @FXML private void openVehicles() { openTable("vehicles"); }
    @FXML private void openModels() { openTable("models"); }
    @FXML private void openDepots() { openTable("depots"); }
    @FXML private void openMechanics() { openTable("mechanics"); }
    @FXML private void openSuppliers() { openTable("fuel_types"); }
    @FXML private void openSpareParts() { openTable("spare_parts"); }
    @FXML private void openMaintenance() { openTable("maintenance"); }
    @FXML private void openPartUsage() { openTable("part_usage"); }
    @FXML private void openReports() { ScreenNavigator.showReports(); }

    private void openTable(String tableKey) {
        ScreenNavigator.showTableEditor(tableKey);
    }
}
