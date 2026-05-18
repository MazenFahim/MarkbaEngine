package com.markbaengine.controller;

import com.markbaengine.model.CrudColumn;
import com.markbaengine.model.FleetDataModel;
import com.markbaengine.model.LookupOption;
import com.markbaengine.model.TableConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * MVC Controller for table-editor.fxml.
 *
 * This is the only controller used for all CRUD tables. It does not know the
 * schema by itself. It asks FleetDataModel for the selected TableConfig.
 */
public class TableEditorController {
    @FXML private Label titleLabel;
    @FXML private Label selectedIdLabel;
    @FXML private Label recordCountLabel;
    @FXML private TableView<Map<String, Object>> dataTable;
    @FXML private GridPane formGrid;
    @FXML private TextField searchField;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;

    @FXML private Button navVehicles;
    @FXML private Button navModels;
    @FXML private Button navDepots;
    @FXML private Button navMechanics;
    @FXML private Button navSuppliers;
    @FXML private Button navSpareParts;
    @FXML private Button navMaintenance;
    @FXML private Button navPartUsage;

    private static final String ACTIVE_NAV_CLASS = "nav-button-active";

    private final FleetDataModel model = new FleetDataModel();
    private final Map<String, Node> inputControls = new HashMap<>();

    private TableConfig table;
    private List<Map<String, Object>> currentRows = new ArrayList<>();

    /**
     * Called by ScreenNavigator when the user opens a table.
     * Example: loadTable("vehicles") loads the Vehicle table definition.
     */
    public void loadTable(String tableKey) {
        table = model.getTable(tableKey);
        titleLabel.setText(table.getTitle());
        markActiveNavigation(tableKey);
        buildTableColumns();
        buildForm();
        setupSelectionListener();
        refreshTable();
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

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            dataTable.setItems(FXCollections.observableArrayList(currentRows));
            updateRecordCount(currentRows.size());
            return;
        }

        List<Map<String, Object>> filtered = currentRows.stream()
                .filter(row -> row.values().stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::toLowerCase)
                        .anyMatch(value -> value.contains(query)))
                .toList();

        dataTable.setItems(FXCollections.observableArrayList(filtered));
        updateRecordCount(filtered.size());
    }

    @FXML
    private void handleAdd() {
        try {
            model.insert(table, readFormValues());
            DialogController.info("Inserted", "Record inserted successfully.");
            refreshTable();
            clearForm();
        } catch (RuntimeException ex) {
            DialogController.error("Insert failed", rootMessage(ex));
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            Map<String, Object> selectedRow = dataTable.getSelectionModel().getSelectedItem();
            Object primaryKeyValue = selectedRow == null ? null : selectedRow.get(table.getPrimaryKeyColumn());

            model.update(table, primaryKeyValue, readFormValues());
            DialogController.info("Updated", "Record updated successfully.");
            refreshTable();
        } catch (RuntimeException ex) {
            DialogController.error("Update failed", rootMessage(ex));
        }
    }

    @FXML
    private void handleDelete() {
        try {
            Map<String, Object> selectedRow = dataTable.getSelectionModel().getSelectedItem();
            Object primaryKeyValue = selectedRow == null ? null : selectedRow.get(table.getPrimaryKeyColumn());

            if (primaryKeyValue == null) {
                throw new IllegalArgumentException("Select a row first.");
            }

            if (!DialogController.confirm("Delete record", "Are you sure you want to delete the selected record?")) {
                return;
            }

            model.delete(table, primaryKeyValue);
            DialogController.info("Deleted", "Record deleted successfully.");
            refreshTable();
            clearForm();
        } catch (RuntimeException ex) {
            DialogController.error("Delete failed", rootMessage(ex));
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void openTable(String tableKey) {
        ScreenNavigator.showTableEditor(tableKey);
    }

    private void buildTableColumns() {
        dataTable.getColumns().clear();
        dataTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        for (CrudColumn column : table.getColumns()) {
            TableColumn<Map<String, Object>, String> tableColumn = new TableColumn<>(column.getLabel());
            tableColumn.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(column.getName());
                return new SimpleStringProperty(value == null ? "" : value.toString());
            });
            double preferredWidth = preferredColumnWidth(column);
            tableColumn.setMinWidth(Math.min(90, preferredWidth));
            tableColumn.setPrefWidth(preferredWidth);
            dataTable.getColumns().add(tableColumn);
        }
    }

    private double preferredColumnWidth(CrudColumn column) {
        String label = column.getLabel() == null ? "" : column.getLabel();
        String name = column.getName() == null ? "" : column.getName().toLowerCase();
        if (name.equals("id") || name.endsWith("_id")) {
            return Math.max(80, label.length() * 12.0);
        }
        if (name.contains("name") || name.contains("description") || name.contains("address")) {
            return Math.max(180, label.length() * 12.0);
        }
        if (name.contains("date") || name.contains("year") || name.contains("status")) {
            return Math.max(130, label.length() * 12.0);
        }
        return Math.max(140, label.length() * 12.0);
    }

    private void buildForm() {
        formGrid.getChildren().clear();
        inputControls.clear();

        int row = 0;
        for (CrudColumn column : table.getColumns()) {
            if (!column.isEditable() || column.isGenerated()) {
                continue;
            }

            Label label = new Label(column.getLabel() + (column.isRequired() ? " *" : ""));
            label.getStyleClass().add("form-field-label");

            Node input = createInputControl(column);
            inputControls.put(column.getName(), input);

            formGrid.add(label, 0, row);
            formGrid.add(input, 1, row);
            GridPane.setMargin(label, new Insets(4, 8, 4, 0));
            GridPane.setMargin(input, new Insets(4, 0, 4, 0));
            GridPane.setHgrow(input, Priority.ALWAYS);
            row++;
        }
    }

    private Node createInputControl(CrudColumn column) {
        Node control = switch (column.getControlType()) {
            case ENUM -> {
                ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(column.getEnumValues()));
                comboBox.setMaxWidth(Double.MAX_VALUE);
                yield comboBox;
            }
            case LOOKUP -> {
                ComboBox<LookupOption> comboBox = new ComboBox<>();
                comboBox.setItems(FXCollections.observableArrayList(
                        model.lookupOptions(column.getLookupTable(), column.getLookupIdColumn(), column.getLookupLabelColumn())
                ));
                comboBox.setMaxWidth(Double.MAX_VALUE);
                yield comboBox;
            }
            case DATE -> {
                DatePicker datePicker = new DatePicker();
                datePicker.setMaxWidth(Double.MAX_VALUE);
                yield datePicker;
            }
            default -> {
                TextField textField = new TextField();
                textField.setMaxWidth(Double.MAX_VALUE);
                yield textField;
            }
        };

        Region region = (Region) control;
        region.setMaxWidth(Double.MAX_VALUE);
        region.setMinHeight(38);
        region.setPrefHeight(38);
        control.getStyleClass().add("form-control");
        return control;
    }

    private void setupSelectionListener() {
        dataTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedRow) -> {
            if (selectedRow == null) {
                selectedIdLabel.setText("No row selected");
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
                return;
            }
            selectedIdLabel.setText("Selected ID: " + selectedRow.get(table.getPrimaryKeyColumn()));
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
            fillForm(selectedRow);
        });
    }

    private void refreshTable() {
        currentRows = model.selectAll(table);
        dataTable.setItems(FXCollections.observableArrayList(currentRows));
        searchField.clear();
        selectedIdLabel.setText("No row selected");
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        updateRecordCount(currentRows.size());
    }

    private void updateRecordCount(int count) {
        if (recordCountLabel != null) {
            recordCountLabel.setText(count + (count == 1 ? " record" : " records"));
        }
    }

    private Map<String, Object> readFormValues() {
        Map<String, Object> values = new HashMap<>();
        for (CrudColumn column : table.getEditableColumns()) {
            Node input = inputControls.get(column.getName());
            values.put(column.getName(), readValue(column, input));
        }
        return values;
    }

    private Object readValue(CrudColumn column, Node input) {
        if (input instanceof TextField textField) {
            String text = textField.getText() == null ? "" : textField.getText().trim();
            if (text.isEmpty()) {
                return null;
            }
            return switch (column.getControlType()) {
                case INTEGER -> Integer.parseInt(text);
                case DECIMAL -> new BigDecimal(text);
                default -> text;
            };
        }
        if (input instanceof DatePicker datePicker) {
            return datePicker.getValue();
        }
        if (input instanceof ComboBox<?> comboBox) {
            Object selected = comboBox.getSelectionModel().getSelectedItem();
            if (selected instanceof LookupOption option) {
                return option.getId();
            }
            return selected;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void fillForm(Map<String, Object> row) {
        for (CrudColumn column : table.getEditableColumns()) {
            Node input = inputControls.get(column.getName());
            Object value = row.get(column.getName());

            if (input instanceof TextField textField) {
                textField.setText(value == null ? "" : value.toString());
            } else if (input instanceof DatePicker datePicker) {
                datePicker.setValue(toLocalDate(value));
            } else if (input instanceof ComboBox<?> comboBox) {
                if (column.getControlType() == CrudColumn.ControlType.LOOKUP) {
                    ComboBox<LookupOption> lookupComboBox = (ComboBox<LookupOption>) comboBox;
                    lookupComboBox.getSelectionModel().clearSelection();
                    for (LookupOption option : lookupComboBox.getItems()) {
                        if (idsEqual(option.getId(), value)) {
                            lookupComboBox.getSelectionModel().select(option);
                            break;
                        }
                    }
                } else {
                    ComboBox<String> enumComboBox = (ComboBox<String>) comboBox;
                    enumComboBox.getSelectionModel().select(value == null ? null : value.toString());
                }
            }
        }
    }

    private void clearForm() {
        dataTable.getSelectionModel().clearSelection();
        selectedIdLabel.setText("No row selected");
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        for (Node input : inputControls.values()) {
            if (input instanceof TextField textField) {
                textField.clear();
            } else if (input instanceof DatePicker datePicker) {
                datePicker.setValue(null);
            } else if (input instanceof ComboBox<?> comboBox) {
                comboBox.getSelectionModel().clearSelection();
            }
        }
    }

    private void markActiveNavigation(String tableKey) {
        setActive(navVehicles, "vehicles".equals(tableKey));
        setActive(navModels, "models".equals(tableKey));
        setActive(navDepots, "depots".equals(tableKey));
        setActive(navMechanics, "mechanics".equals(tableKey));
        setActive(navSuppliers, "fuel_types".equals(tableKey));
        setActive(navSpareParts, "spare_parts".equals(tableKey));
        setActive(navMaintenance, "maintenance".equals(tableKey));
        setActive(navPartUsage, "part_usage".equals(tableKey));
    }

    private void setActive(Button button, boolean active) {
        if (button == null) {
            return;
        }
        if (active && !button.getStyleClass().contains(ACTIVE_NAV_CLASS)) {
            button.getStyleClass().add(ACTIVE_NAV_CLASS);
        } else if (!active) {
            button.getStyleClass().remove(ACTIVE_NAV_CLASS);
        }
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        return LocalDate.parse(value.toString());
    }

    private boolean idsEqual(Object first, Object second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.toString().equals(second.toString());
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getMessage() : current.getMessage();
    }
}
