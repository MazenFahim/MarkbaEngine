package com.markbaengine.controller;

import com.markbaengine.model.ReportDefinition;
import com.markbaengine.model.ReportModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Map;

/**
 * MVC Controller for reports.fxml.
 *
 * It asks ReportModel for the inquiry SQL results, then displays the returned rows.
 */
public class ReportsController {
    @FXML private ComboBox<ReportDefinition> reportComboBox;
    @FXML private TableView<Map<String, Object>> reportTable;
    @FXML private Label resultCountLabel;

    private final ReportModel reportModel = new ReportModel();

    @FXML
    private void initialize() {
        reportTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        reportComboBox.setItems(FXCollections.observableArrayList(reportModel.getReports()));
        reportComboBox.getSelectionModel().selectFirst();
        handleRunReport();
    }

    @FXML private void handleBack() { ScreenNavigator.showDashboard(); }
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

    @FXML
    private void handleRunReport() {
        ReportDefinition selectedReport = reportComboBox.getSelectionModel().getSelectedItem();
        if (selectedReport == null) {
            updateResultCount(0);
            return;
        }

        try {
            List<Map<String, Object>> rows = reportModel.runReport(selectedReport);
            buildColumns(rows);
            reportTable.setItems(FXCollections.observableArrayList(rows));
            updateResultCount(rows.size());
        } catch (RuntimeException ex) {
            DialogController.error("Report failed", rootMessage(ex));
        }
    }

    private void openTable(String tableKey) {
        ScreenNavigator.showTableEditor(tableKey);
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

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getMessage() : current.getMessage();
    }
}
