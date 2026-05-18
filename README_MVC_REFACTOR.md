# MarkbaEngine MVC Simple Refactor

This version keeps the same purpose of the original JavaFX + SQL Server project, but simplifies the Java code into a clearer MVC structure.

## Main idea

The database is still created by the SQL files in `docs/`:

- `01_Create_FleetMaintenanceDB.sql` = DDL / database structure
- `02_Seed_FleetMaintenanceDB.sql` = DML / sample data
- `03_Inquiry_Reports.sql` = inquiry SELECT reports

The Java app is now separated as:

```text
Model      = database connection, table definitions, CRUD SQL, report SQL
View       = FXML screens + CSS files
Controller = JavaFX event handlers and screen navigation
```

## MVC structure

```text
src/main/java/com/markbaengine/main/App.java
```

Starts the app, tests the SQL Server connection, then opens the dashboard.

```text
src/main/java/com/markbaengine/controller/
```

Contains JavaFX controllers:

- `DashboardController.java` handles dashboard buttons.
- `TableEditorController.java` handles add, update, delete, select, search, and form input for the current table.
- `ReportsController.java` handles report selection and display.
- `ScreenNavigator.java` switches screens.
- `DialogController.java` shows messages and confirmations.

```text
src/main/java/com/markbaengine/model/
```

Contains the application model:

- `Database.java` opens SQL Server connections and runs SQL.
- `FleetDataModel.java` stores all editable table definitions and executes CRUD.
- `ReportModel.java` stores the six inquiry SQL queries and runs them.
- `TableConfig.java` describes one database table.
- `CrudColumn.java` describes one column/input field.
- `LookupOption.java` represents one foreign-key dropdown option.
- `ReportDefinition.java` represents one inquiry report.

```text
src/main/resources/com/markbaengine/view/
```

Contains the FXML views:

- `dashboard.fxml`
- `table-editor.fxml`
- `reports.fxml`

## Important TA explanation

The app uses one generic table editor instead of separate screens for each table.

Example flow when opening Vehicles:

```text
Dashboard button
→ DashboardController.openVehicles()
→ ScreenNavigator.showTableEditor("vehicles")
→ TableEditorController.loadTable("vehicles")
→ FleetDataModel.getTable("vehicles")
→ TableConfig tells the app:
   - SQL table = Vehicle
   - Primary key = vehicle_id
   - SELECT query = Vehicle joined with VehicleModel, FuelType, Depot
   - Editable columns = vehicle_id, vehicle_code, model_id, fuelType_id, Depot_ID, etc.
```

## Where SQL operations are now

All runtime table operations are in:

```text
src/main/java/com/markbaengine/model/FleetDataModel.java
```

- `selectAll()` runs the current table SELECT query.
- `insert()` builds `INSERT INTO table (...) VALUES (...)`.
- `update()` builds `UPDATE table SET ... WHERE primaryKey = ?`.
- `delete()` builds `DELETE FROM table WHERE primaryKey = ?`.
- `lookupOptions()` loads dropdown values for foreign keys.

The actual connection and query execution are in:

```text
src/main/java/com/markbaengine/model/Database.java
```

## Reports

Report queries are in:

```text
src/main/java/com/markbaengine/model/ReportModel.java
```

The reports controller calls `reportModel.runReport(selectedReport)`, and the model executes the SQL using `Database.selectRows()`.

## What changed from the old version

Old structure:

```text
Controller → Service → DAO → DBConnection
```

New structure:

```text
Controller → Model → Database
```

This is simpler and easier to explain in a TA discussion.
