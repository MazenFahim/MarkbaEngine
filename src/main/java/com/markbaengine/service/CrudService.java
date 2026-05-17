package com.markbaengine.service;

import com.markbaengine.dao.GenericCrudDao;
import com.markbaengine.model.CrudColumn;
import com.markbaengine.model.TableConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.markbaengine.model.CrudColumn.ControlType.DATE;
import static com.markbaengine.model.CrudColumn.ControlType.DECIMAL;
import static com.markbaengine.model.CrudColumn.ControlType.ENUM;
import static com.markbaengine.model.CrudColumn.ControlType.INTEGER;
import static com.markbaengine.model.CrudColumn.ControlType.LOOKUP;
import static com.markbaengine.model.CrudColumn.ControlType.TEXT;

public class CrudService {
    private final GenericCrudDao genericCrudDao = new GenericCrudDao();
    private final Map<String, TableConfig> configs = new LinkedHashMap<>();

    public CrudService() {
        registerConfigs();
    }

    public List<TableConfig> getTableConfigs() {
        return List.copyOf(configs.values());
    }

    public TableConfig getConfig(String key) {
        TableConfig config = configs.get(key);
        if (config == null) {
            throw new IllegalArgumentException("Unknown table key: " + key);
        }
        return config;
    }

    public List<Map<String, Object>> findAll(TableConfig config) {
        return genericCrudDao.findAll(config);
    }

    public void insert(TableConfig config, Map<String, Object> values) {
        validate(config, values, false);
        genericCrudDao.insert(config, values);
    }

    public void update(TableConfig config, Object primaryKeyValue, Map<String, Object> values) {
        if (primaryKeyValue == null) {
            throw new IllegalArgumentException("Select a row first.");
        }
        validate(config, values, true);
        genericCrudDao.update(config, primaryKeyValue, values);
    }

    public void delete(TableConfig config, Object primaryKeyValue) {
        if (primaryKeyValue == null) {
            throw new IllegalArgumentException("Select a row first.");
        }
        genericCrudDao.delete(config, primaryKeyValue);
    }

    private void validate(TableConfig config, Map<String, Object> values, boolean update) {
        for (CrudColumn column : config.getEditableColumns()) {
            Object value = values.get(column.getName());
            if (column.isRequired() && (value == null || value.toString().isBlank())) {
                throw new IllegalArgumentException(column.getLabel() + " is required.");
            }
        }
    }

    private void registerConfigs() {
        configs.put("vehicles", new TableConfig(
                "vehicles",
                "Vehicles",
                "Vehicle",
                "vehicle_id",
                """
                SELECT v.vehicle_id,
                       v.vehicle_code,
                       v.vehicle_name,
                       v.model_id,
                       vm.model_name,
                       v.fuelType_id,
                       ft.fuel_name,
                       v.Depot_ID,
                       d.Depot_Name,
                       v.status,
                       v.manufacture_year,
                       v.passenger_capacity,
                       v.Current_Mileage
                FROM Vehicle v
                JOIN VehicleModel vm ON v.model_id = vm.model_id
                JOIN FuelType ft ON v.fuelType_id = ft.fuelType_id
                JOIN Depot d ON v.Depot_ID = d.Depot_ID
                ORDER BY v.vehicle_id
                """,
                List.of(
                        CrudColumn.builder("vehicle_id", "Vehicle ID", INTEGER).primaryKey(true).required(true).build(),
                        CrudColumn.builder("vehicle_code", "Vehicle Code", TEXT).required(true).build(),
                        CrudColumn.builder("vehicle_name", "Vehicle Name", TEXT).required(true).build(),
                        CrudColumn.builder("model_id", "Model", LOOKUP).required(true).lookup("VehicleModel", "model_id", "model_name").build(),
                        CrudColumn.builder("model_name", "Model Name", TEXT).editable(false).build(),
                        CrudColumn.builder("fuelType_id", "Fuel Type", LOOKUP).required(true).lookup("FuelType", "fuelType_id", "fuel_name").build(),
                        CrudColumn.builder("fuel_name", "Fuel Name", TEXT).editable(false).build(),
                        CrudColumn.builder("Depot_ID", "Depot", LOOKUP).required(true).lookup("Depot", "Depot_ID", "Depot_Name").build(),
                        CrudColumn.builder("Depot_Name", "Depot Name", TEXT).editable(false).build(),
                        CrudColumn.builder("status", "Status", ENUM).required(true).enumValues(List.of("Operational", "Under Repair", "Out of Service")).build(),
                        CrudColumn.builder("manufacture_year", "Manufacture Year", INTEGER).required(true).build(),
                        CrudColumn.builder("passenger_capacity", "Passenger Capacity", INTEGER).required(true).build(),
                        CrudColumn.builder("Current_Mileage", "Current Mileage", INTEGER).required(true).build()
                )
        ));

        configs.put("models", new TableConfig(
                "models",
                "Vehicle Models",
                "VehicleModel",
                "model_id",
                "SELECT model_id, model_name, manufacturer, vehicle_type FROM VehicleModel ORDER BY model_id",
                List.of(
                        CrudColumn.builder("model_id", "Model ID", INTEGER).primaryKey(true).required(true).build(),
                        CrudColumn.builder("model_name", "Model Name", TEXT).required(true).build(),
                        CrudColumn.builder("manufacturer", "Manufacturer", TEXT).required(true).build(),
                        CrudColumn.builder("vehicle_type", "Vehicle Type", ENUM).required(true).enumValues(List.of("Van", "Bus", "Truck", "Electric Bus")).build()
                )
        ));

        configs.put("depots", new TableConfig(
                "depots",
                "Depots / Workshops",
                "Depot",
                "Depot_ID",
                "SELECT Depot_ID, Depot_Name, Location, Capacity FROM Depot ORDER BY Depot_ID",
                List.of(
                        CrudColumn.builder("Depot_ID", "Depot ID", INTEGER).primaryKey(true).required(true).build(),
                        CrudColumn.builder("Depot_Name", "Depot Name", TEXT).required(true).build(),
                        CrudColumn.builder("Location", "Location", TEXT).required(true).build(),
                        CrudColumn.builder("Capacity", "Capacity", INTEGER).required(true).build()
                )
        ));

        configs.put("mechanics", new TableConfig(
                "mechanics",
                "Mechanics",
                "Mechanic",
                "Mechanic_ID",
                """
                SELECT m.Mechanic_ID,
                       m.Mechanic_name,
                       m.Specialization,
                       m.Experience_Level,
                       m.Hire_Date,
                       m.Depot_ID,
                       d.Depot_Name
                FROM Mechanic m
                JOIN Depot d ON m.Depot_ID = d.Depot_ID
                ORDER BY m.Mechanic_ID
                """,
                List.of(
                        CrudColumn.builder("Mechanic_ID", "Mechanic ID", INTEGER).primaryKey(true).required(true).build(),
                        CrudColumn.builder("Mechanic_name", "Mechanic Name", TEXT).required(true).build(),
                        CrudColumn.builder("Specialization", "Specialization", ENUM).required(true).enumValues(List.of("Engine Repair", "Electrical", "Brake Systems", "General", "Cooling System")).build(),
                        CrudColumn.builder("Experience_Level", "Experience Level", ENUM).required(true).enumValues(List.of("Junior", "Mid-Level", "Senior")).build(),
                        CrudColumn.builder("Hire_Date", "Hire Date", DATE).required(true).build(),
                        CrudColumn.builder("Depot_ID", "Depot", LOOKUP).required(true).lookup("Depot", "Depot_ID", "Depot_Name").build(),
                        CrudColumn.builder("Depot_Name", "Depot Name", TEXT).editable(false).build()
                )
        ));

        /*
         * Temporary compatibility:
         * The old dashboard still has a "Suppliers" button.
         * Since Supplier is no longer part of the final ERD, this key now opens FuelType.
         * Later we will rename the UI button from "Suppliers" to "Fuel Types".
         */
        configs.put("suppliers", new TableConfig(
                "suppliers",
                "Fuel Types",
                "FuelType",
                "fuelType_id",
                "SELECT fuelType_id, fuel_name, fuel_price FROM FuelType ORDER BY fuelType_id",
                List.of(
                        CrudColumn.builder("fuelType_id", "Fuel Type ID", INTEGER).primaryKey(true).required(true).build(),
                        CrudColumn.builder("fuel_name", "Fuel Name", TEXT).required(true).build(),
                        CrudColumn.builder("fuel_price", "Fuel Price", DECIMAL).build()
                )
        ));

        configs.put("spare_parts", new TableConfig(
                "spare_parts",
                "Spare Parts",
                "Spare_Part",
                "Part_Serial",
                "SELECT Part_Serial, Name, Unit_Cost, Stock_Quantity FROM Spare_Part ORDER BY Part_Serial",
                List.of(
                        CrudColumn.builder("Part_Serial", "Part Serial", INTEGER).primaryKey(true).required(true).build(),
                        CrudColumn.builder("Name", "Part Name", TEXT).required(true).build(),
                        CrudColumn.builder("Unit_Cost", "Unit Cost", DECIMAL).required(true).build(),
                        CrudColumn.builder("Stock_Quantity", "Stock Quantity", INTEGER).required(true).build()
                )
        ));

        configs.put("maintenance", new TableConfig(
                "maintenance",
                "Maintenance Logs",
                "Maintenance_Log",
                "Log_ID",
                """
                SELECT ml.Log_ID,
                       ml.Vehicle_ID,
                       v.vehicle_code,
                       v.vehicle_name,
                       ml.Mechanic_ID,
                       m.Mechanic_name,
                       ml.Depot_ID,
                       d.Depot_Name,
                       ml.Open_Date,
                       ml.Close_Date,
                       ml.Issue_Description,
                       ml.Log_Status
                FROM Maintenance_Log ml
                JOIN Vehicle v ON ml.Vehicle_ID = v.vehicle_id
                JOIN Mechanic m ON ml.Mechanic_ID = m.Mechanic_ID
                JOIN Depot d ON ml.Depot_ID = d.Depot_ID
                ORDER BY ml.Log_ID
                """,
                List.of(
                        CrudColumn.builder("Log_ID", "Log ID", INTEGER).primaryKey(true).required(true).build(),
                        CrudColumn.builder("Vehicle_ID", "Vehicle", LOOKUP).required(true).lookup("Vehicle", "vehicle_id", "vehicle_code").build(),
                        CrudColumn.builder("vehicle_code", "Vehicle Code", TEXT).editable(false).build(),
                        CrudColumn.builder("vehicle_name", "Vehicle Name", TEXT).editable(false).build(),
                        CrudColumn.builder("Mechanic_ID", "Mechanic", LOOKUP).required(true).lookup("Mechanic", "Mechanic_ID", "Mechanic_name").build(),
                        CrudColumn.builder("Mechanic_name", "Mechanic Name", TEXT).editable(false).build(),
                        CrudColumn.builder("Depot_ID", "Depot", LOOKUP).required(true).lookup("Depot", "Depot_ID", "Depot_Name").build(),
                        CrudColumn.builder("Depot_Name", "Depot Name", TEXT).editable(false).build(),
                        CrudColumn.builder("Open_Date", "Open Date", DATE).required(true).build(),
                        CrudColumn.builder("Close_Date", "Close Date", DATE).build(),
                        CrudColumn.builder("Issue_Description", "Issue Description", TEXT).required(true).build(),
                        CrudColumn.builder("Log_Status", "Status", ENUM).required(true).enumValues(List.of("Open", "In Progress", "Closed")).build()
                )
        ));

        configs.put("part_usage", new TableConfig(
                "part_usage",
                "Part Usage",
                "Usage_ID",
                "Usage_ID",
                """
                SELECT u.Usage_ID,
                       u.Log_ID,
                       v.vehicle_code,
                       v.vehicle_name,
                       u.Part_Serial,
                       sp.Name AS part_name,
                       u.Quantity,
                       u.Price_at_Transaction
                FROM Usage_ID u
                JOIN Maintenance_Log ml ON u.Log_ID = ml.Log_ID
                JOIN Vehicle v ON ml.Vehicle_ID = v.vehicle_id
                JOIN Spare_Part sp ON u.Part_Serial = sp.Part_Serial
                ORDER BY u.Usage_ID
                """,
                List.of(
                        CrudColumn.builder("Usage_ID", "Usage ID", INTEGER).primaryKey(true).required(true).build(),
                        CrudColumn.builder("Log_ID", "Maintenance Log", LOOKUP).required(true).lookup("Maintenance_Log", "Log_ID", "Log_ID").build(),
                        CrudColumn.builder("vehicle_code", "Vehicle Code", TEXT).editable(false).build(),
                        CrudColumn.builder("vehicle_name", "Vehicle Name", TEXT).editable(false).build(),
                        CrudColumn.builder("Part_Serial", "Spare Part", LOOKUP).required(true).lookup("Spare_Part", "Part_Serial", "Name").build(),
                        CrudColumn.builder("part_name", "Part Name", TEXT).editable(false).build(),
                        CrudColumn.builder("Quantity", "Quantity", INTEGER).required(true).build(),
                        CrudColumn.builder("Price_at_Transaction", "Price at Transaction", DECIMAL).required(true).build()
                )
        ));
    }
}