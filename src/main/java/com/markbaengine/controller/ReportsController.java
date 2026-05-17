package com.markbaengine.controller;

import com.markbaengine.model.ReportDefinition;
import com.markbaengine.service.ReportService;
import com.markbaengine.util.AlertUtil;
import com.markbaengine.util.Navigation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Map;

public class ReportsController {
    @FXML private ComboBox<ReportDefinition> reportComboBox;
    @FXML private TableView<Map<String, Object>> reportTable;
    @FXML private Label resultCountLabel;

    private final ReportService reportService = new ReportService();

    @FXML
    private void initialize() {
        reportTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        reportComboBox.setItems(FXCollections.observableArrayList(reportService.getReports()));
        reportComboBox.getSelectionModel().selectFirst();
        handleRunReport();
    }

    @FXML private void handleBack() { Navigation.showDashboard(); }
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

    @FXML
    private void handleRunReport() {
        ReportDefinition selectedReport = reportComboBox.getSelectionModel().getSelectedItem();
        if (selectedReport == null) {
            updateResultCount(0);
            return;
        }
        try {
            List<Map<String, Object>> rows = reportService.runReport(selectedReport);
            buildColumns(rows);
            reportTable.setItems(FXCollections.observableArrayList(rows));
            updateResultCount(rows.size());
        } catch (RuntimeException ex) {
            AlertUtil.error("Report failed", ex.getMessage());
        }
    }

    private void buildColumns(List<Map<String, Object>> rows) {
        reportTable.getColumns().clear();
        if (rows.isEmpty()) {
            return;
        }
        for (String columnName : rows.get(0).keySet()) {
            TableColumn<Map<String, Object>, String> tableColumn = new TableColumn<>(columnName);
            tableColumn.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(columnName);
                return new SimpleStringProperty(value == null ? "" : value.toString());
            });
            double width = Math.max(140, columnName.length() * 12.0);
            tableColumn.setMinWidth(Math.min(100, width));
            tableColumn.setPrefWidth(width);
            reportTable.getColumns().add(tableColumn);
        }
    }

    private void updateResultCount(int count) {
        if (resultCountLabel != null) {
            resultCountLabel.setText(count + (count == 1 ? " row" : " rows"));
        }
    }
}
