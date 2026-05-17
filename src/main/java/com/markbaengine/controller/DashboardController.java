package com.markbaengine.controller;

import com.markbaengine.util.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {
    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        welcomeLabel.setText("Ready to manage fleet data");
    }

    @FXML private void openDashboard() { Navigation.showDashboard(); }
    @FXML private void openVehicles() { Navigation.showTableEditor("vehicles"); }
    @FXML private void openModels() { Navigation.showTableEditor("models"); }
    @FXML private void openDepots() { Navigation.showTableEditor("depots"); }
    @FXML private void openMechanics() { Navigation.showTableEditor("mechanics"); }
    @FXML private void openSuppliers() { Navigation.showTableEditor("suppliers"); }
    @FXML private void openSpareParts() { Navigation.showTableEditor("spare_parts"); }
    @FXML private void openMaintenance() { Navigation.showTableEditor("maintenance"); }
    @FXML private void openPartUsage() { Navigation.showTableEditor("part_usage"); }
    @FXML private void openReports() { Navigation.showReports(); }
}
