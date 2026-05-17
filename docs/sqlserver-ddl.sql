CREATE DATABASE MarkbaEngineDB;
GO

USE MarkbaEngineDB;
GO

CREATE TABLE AppUser (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password_hash NVARCHAR(64) NOT NULL,
    full_name NVARCHAR(100) NULL
);
GO

CREATE TABLE Depot (
    depot_id INT IDENTITY(1,1) PRIMARY KEY,
    depot_name NVARCHAR(100) NOT NULL UNIQUE,
    location NVARCHAR(150) NOT NULL,
    capacity INT NOT NULL CHECK (capacity >= 0)
);
GO

CREATE TABLE Vehicle_Model (
    model_id INT IDENTITY(1,1) PRIMARY KEY,
    model_name NVARCHAR(100) NOT NULL,
    manufacturer NVARCHAR(100) NOT NULL,
    vehicle_type NVARCHAR(50) NOT NULL,
    fuel_type NVARCHAR(50) NOT NULL
);
GO

CREATE TABLE Vehicle (
    vehicle_id INT IDENTITY(1,1) PRIMARY KEY,
    vehicle_code NVARCHAR(20) NOT NULL UNIQUE,
    plate_number NVARCHAR(30) NOT NULL UNIQUE,
    vehicle_name NVARCHAR(100) NOT NULL,
    model_id INT NOT NULL,
    depot_id INT NOT NULL,
    status NVARCHAR(30) NOT NULL CHECK (status IN ('Active', 'Under Maintenance', 'Retired')),
    manufacture_year INT NOT NULL,
    passenger_capacity INT NOT NULL CHECK (passenger_capacity >= 0),
    current_mileage DECIMAL(12,2) NOT NULL CHECK (current_mileage >= 0),
    CONSTRAINT FK_Vehicle_Model FOREIGN KEY (model_id) REFERENCES Vehicle_Model(model_id),
    CONSTRAINT FK_Vehicle_Depot FOREIGN KEY (depot_id) REFERENCES Depot(depot_id)
);
GO

CREATE TABLE Mechanic (
    mechanic_id INT IDENTITY(1,1) PRIMARY KEY,
    mechanic_code NVARCHAR(20) NOT NULL UNIQUE,
    mechanic_name NVARCHAR(100) NOT NULL,
    specialization NVARCHAR(80) NOT NULL,
    phone NVARCHAR(30) NULL,
    depot_id INT NOT NULL,
    status NVARCHAR(30) NOT NULL CHECK (status IN ('Active', 'On Leave', 'Inactive')),
    CONSTRAINT FK_Mechanic_Depot FOREIGN KEY (depot_id) REFERENCES Depot(depot_id)
);
GO

CREATE TABLE Supplier (
    supplier_id INT IDENTITY(1,1) PRIMARY KEY,
    supplier_name NVARCHAR(100) NOT NULL UNIQUE,
    phone NVARCHAR(30) NULL,
    email NVARCHAR(100) NULL
);
GO

CREATE TABLE Spare_Part (
    part_id INT IDENTITY(1,1) PRIMARY KEY,
    part_code NVARCHAR(20) NOT NULL UNIQUE,
    part_name NVARCHAR(100) NOT NULL,
    unit_cost DECIMAL(12,2) NOT NULL CHECK (unit_cost >= 0),
    stock_quantity INT NOT NULL CHECK (stock_quantity >= 0),
    supplier_id INT NOT NULL,
    CONSTRAINT FK_SparePart_Supplier FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);
GO

CREATE TABLE Maintenance_Log (
    log_id INT IDENTITY(1,1) PRIMARY KEY,
    vehicle_id INT NOT NULL,
    mechanic_id INT NOT NULL,
    open_date DATE NOT NULL,
    close_date DATE NULL,
    description NVARCHAR(1000) NOT NULL,
    status NVARCHAR(30) NOT NULL CHECK (status IN ('Open', 'In Progress', 'Closed', 'Cancelled')),
    CONSTRAINT FK_Log_Vehicle FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id),
    CONSTRAINT FK_Log_Mechanic FOREIGN KEY (mechanic_id) REFERENCES Mechanic(mechanic_id)
);
GO

CREATE TABLE Part_Usage (
    usage_id INT IDENTITY(1,1) PRIMARY KEY,
    log_id INT NOT NULL,
    part_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_cost_at_time DECIMAL(12,2) NOT NULL CHECK (unit_cost_at_time >= 0),
    CONSTRAINT FK_PartUsage_Log FOREIGN KEY (log_id) REFERENCES Maintenance_Log(log_id) ON DELETE CASCADE,
    CONSTRAINT FK_PartUsage_Part FOREIGN KEY (part_id) REFERENCES Spare_Part(part_id)
);
GO

-- Required JOIN report example for Phase 3 discussion.
SELECT
    v.plate_number,
    v.vehicle_name,
    m.mechanic_name,
    l.open_date,
    l.close_date,
    l.status
FROM Maintenance_Log l
JOIN Vehicle v ON l.vehicle_id = v.vehicle_id
JOIN Mechanic m ON l.mechanic_id = m.mechanic_id;
GO
