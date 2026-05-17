/*==============================================================
Database: Fleet Maintenance Management System
Target DBMS: Microsoft SQL Server / T-SQL
Purpose: Final cleaned DDL based on the Physical ERD
Notes:
- Keeps table name Usage_ID as requested.
- Adds Stock_Quantity to Spare_Part because it appears in the project requirements.
- Adds CHECK constraints for important business rules.
- Uses default FK deletion protection, so parent records cannot be deleted while child records exist.
==============================================================*/

/*==============================================================
Optional: Create and use database
==============================================================*/
IF DB_ID('FleetMaintenanceDB') IS NULL
BEGIN
    CREATE DATABASE FleetMaintenanceDB;
END
GO

USE FleetMaintenanceDB;
GO

/*==============================================================
Optional reset section for development/testing
Drop child tables first, then parent tables.
Comment this section before using on a real submitted database if you do not want data removed.
==============================================================*/
IF OBJECT_ID('dbo.Usage_ID', 'U') IS NOT NULL DROP TABLE dbo.Usage_ID;
IF OBJECT_ID('dbo.Maintenance_Log', 'U') IS NOT NULL DROP TABLE dbo.Maintenance_Log;
IF OBJECT_ID('dbo.Vehicle', 'U') IS NOT NULL DROP TABLE dbo.Vehicle;
IF OBJECT_ID('dbo.Mechanic', 'U') IS NOT NULL DROP TABLE dbo.Mechanic;
IF OBJECT_ID('dbo.Spare_Part', 'U') IS NOT NULL DROP TABLE dbo.Spare_Part;
IF OBJECT_ID('dbo.Depot', 'U') IS NOT NULL DROP TABLE dbo.Depot;
IF OBJECT_ID('dbo.FuelType', 'U') IS NOT NULL DROP TABLE dbo.FuelType;
IF OBJECT_ID('dbo.VehicleModel', 'U') IS NOT NULL DROP TABLE dbo.VehicleModel;
GO

/*==============================================================
1. VehicleModel
==============================================================*/
CREATE TABLE dbo.VehicleModel
(
    model_id INT NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    vehicle_type VARCHAR(255) NOT NULL,

    CONSTRAINT PK_VehicleModel
        PRIMARY KEY (model_id),

    CONSTRAINT UQ_VehicleModel_Name_Manufacturer_Type
        UNIQUE (model_name, manufacturer, vehicle_type)
);
GO

/*==============================================================
2. FuelType
==============================================================*/
CREATE TABLE dbo.FuelType
(
    fuelType_id INT NOT NULL,
    fuel_name VARCHAR(20) NOT NULL,
    fuel_price DECIMAL(10,2) NULL,

    CONSTRAINT PK_FuelType
        PRIMARY KEY (fuelType_id),

    CONSTRAINT UQ_FuelType_Name
        UNIQUE (fuel_name),

    CONSTRAINT CK_FuelType_Price_NonNegative
        CHECK (fuel_price IS NULL OR fuel_price >= 0)
);
GO

/*==============================================================
3. Depot
==============================================================*/
CREATE TABLE dbo.Depot
(
    Depot_ID INT NOT NULL,
    Depot_Name VARCHAR(20) NOT NULL,
    Location VARCHAR(20) NOT NULL,
    Capacity INT NOT NULL,

    CONSTRAINT PK_Depot
        PRIMARY KEY (Depot_ID),

    CONSTRAINT UQ_Depot_Name
        UNIQUE (Depot_Name),

    CONSTRAINT CK_Depot_Capacity_Positive
        CHECK (Capacity > 0)
);
GO

/*==============================================================
4. Spare_Part
==============================================================*/
CREATE TABLE dbo.Spare_Part
(
    Part_Serial INT NOT NULL,
    Name VARCHAR(255) NOT NULL,
    Unit_Cost DECIMAL(10,2) NOT NULL,
    Stock_Quantity INT NOT NULL DEFAULT 0,

    CONSTRAINT PK_Spare_Part
        PRIMARY KEY (Part_Serial),

    CONSTRAINT CK_SparePart_UnitCost_NonNegative
        CHECK (Unit_Cost >= 0),

    CONSTRAINT CK_SparePart_StockQuantity_NonNegative
        CHECK (Stock_Quantity >= 0)
);
GO

/*==============================================================
5. Mechanic
==============================================================*/
CREATE TABLE dbo.Mechanic
(
    Mechanic_ID INT NOT NULL,
    Mechanic_name VARCHAR(20) NOT NULL,
    Specialization VARCHAR(20) NOT NULL,
    Experience_Level VARCHAR(20) NOT NULL,
    Hire_Date DATE NOT NULL,
    Depot_ID INT NOT NULL,

    CONSTRAINT PK_Mechanic
        PRIMARY KEY (Mechanic_ID),

    CONSTRAINT UQ_Mechanic_ID_Depot
        UNIQUE (Mechanic_ID, Depot_ID),

    CONSTRAINT FK_Mechanic_Depot
        FOREIGN KEY (Depot_ID)
        REFERENCES dbo.Depot(Depot_ID)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
GO

/*==============================================================
6. Vehicle
==============================================================*/
CREATE TABLE dbo.Vehicle
(
    vehicle_id INT NOT NULL,
    model_id INT NOT NULL,
    fuelType_id INT NOT NULL,
    Depot_ID INT NOT NULL,
    vehicle_code VARCHAR(20) NOT NULL,
    vehicle_name VARCHAR(20) NOT NULL,
    status VARCHAR(100) NOT NULL,
    manufacture_year INT NOT NULL,
    passenger_capacity INT NOT NULL,
    Current_Mileage INT NOT NULL,

    CONSTRAINT PK_Vehicle
        PRIMARY KEY (vehicle_id),

    CONSTRAINT UQ_Vehicle_Code
        UNIQUE (vehicle_code),

    CONSTRAINT FK_Vehicle_VehicleModel
        FOREIGN KEY (model_id)
        REFERENCES dbo.VehicleModel(model_id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT FK_Vehicle_FuelType
        FOREIGN KEY (fuelType_id)
        REFERENCES dbo.FuelType(fuelType_id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT FK_Vehicle_Depot
        FOREIGN KEY (Depot_ID)
        REFERENCES dbo.Depot(Depot_ID)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT CK_Vehicle_Status
        CHECK (status IN ('Operational', 'Under Repair', 'Out of Service')),

    CONSTRAINT CK_Vehicle_ManufactureYear
        CHECK (manufacture_year >= 1900),

    CONSTRAINT CK_Vehicle_PassengerCapacity_NonNegative
        CHECK (passenger_capacity >= 0),

    CONSTRAINT CK_Vehicle_CurrentMileage_NonNegative
        CHECK (Current_Mileage >= 0)
);
GO

/*==============================================================
7. Maintenance_Log
==============================================================*/
CREATE TABLE dbo.Maintenance_Log
(
    Log_ID INT NOT NULL,
    Open_Date DATE NOT NULL,
    Close_Date DATE NULL,
    Issue_Description VARCHAR(1000) NOT NULL,
    Log_Status VARCHAR(20) NOT NULL,
    Vehicle_ID INT NOT NULL,
    Mechanic_ID INT NOT NULL,
    Depot_ID INT NOT NULL,

    CONSTRAINT PK_Maintenance_Log
        PRIMARY KEY (Log_ID),

    CONSTRAINT FK_MaintenanceLog_Vehicle
        FOREIGN KEY (Vehicle_ID)
        REFERENCES dbo.Vehicle(vehicle_id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT FK_MaintenanceLog_Mechanic
        FOREIGN KEY (Mechanic_ID)
        REFERENCES dbo.Mechanic(Mechanic_ID)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT FK_MaintenanceLog_Depot
        FOREIGN KEY (Depot_ID)
        REFERENCES dbo.Depot(Depot_ID)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    /* Enforces that the mechanic assigned to the log belongs to the same depot as the log. */
    CONSTRAINT FK_MaintenanceLog_Mechanic_Depot
        FOREIGN KEY (Mechanic_ID, Depot_ID)
        REFERENCES dbo.Mechanic(Mechanic_ID, Depot_ID)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT CK_MaintenanceLog_Status
        CHECK (Log_Status IN ('Open', 'In Progress', 'Closed')),

    CONSTRAINT CK_MaintenanceLog_CloseDate_AfterOpenDate
        CHECK (Close_Date IS NULL OR Close_Date >= Open_Date),

    CONSTRAINT CK_MaintenanceLog_Closed_HasCloseDate
        CHECK (Log_Status <> 'Closed' OR Close_Date IS NOT NULL)
);
GO

/*==============================================================
8. Usage_ID
Represents spare parts used in maintenance logs.
Table name kept as Usage_ID to match the current physical ERD/documentation.
==============================================================*/
CREATE TABLE dbo.Usage_ID
(
    Usage_ID INT NOT NULL,
    Part_Serial INT NOT NULL,
    Log_ID INT NOT NULL,
    Quantity INT NOT NULL,
    Price_at_Transaction DECIMAL(10,2) NOT NULL,

    CONSTRAINT PK_Usage_ID
        PRIMARY KEY (Usage_ID),

    CONSTRAINT FK_Usage_SparePart
        FOREIGN KEY (Part_Serial)
        REFERENCES dbo.Spare_Part(Part_Serial)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT FK_Usage_MaintenanceLog
        FOREIGN KEY (Log_ID)
        REFERENCES dbo.Maintenance_Log(Log_ID)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT CK_Usage_Quantity_Positive
        CHECK (Quantity > 0),

    CONSTRAINT CK_Usage_PriceAtTransaction_NonNegative
        CHECK (Price_at_Transaction >= 0)
);
GO

PRINT 'FleetMaintenanceDB schema created successfully.';
GO
