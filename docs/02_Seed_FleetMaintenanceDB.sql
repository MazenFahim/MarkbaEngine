/*==============================================================
Database: Fleet Maintenance Management System
File: 02_Seed_FleetMaintenanceDB.sql
Purpose:
  Populate the SQL Server database with sample data that proves
  the Urban Fleet & Maintenance Hub requirements and inquiry queries.

Important:
  This script uses dynamic "last month" dates based on GETDATE().
  That means the inquiry reports will continue to work even if the TA runs
  the script in a different month/year.
==============================================================*/

USE FleetMaintenanceDB;
GO

/*==============================================================
Clean existing data
Delete child tables first, then parent tables.
==============================================================*/
DELETE FROM Usage_ID;
DELETE FROM Maintenance_Log;
DELETE FROM Vehicle;
DELETE FROM Mechanic;
DELETE FROM Spare_Part;
DELETE FROM Depot;
DELETE FROM FuelType;
DELETE FROM VehicleModel;
GO

/*==============================================================
Dynamic dates for "last month"
==============================================================*/
DECLARE @StartOfThisMonth DATE = DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1);
DECLARE @StartOfLastMonth DATE = DATEADD(MONTH, -1, @StartOfThisMonth);

/*==============================================================
Seed Data
==============================================================*/

-- 1. VehicleModel
INSERT INTO VehicleModel (model_id, model_name, manufacturer, vehicle_type)
VALUES
(1, 'Sprinter 316', 'Mercedes-Benz', 'Van'),
(2, 'Transit 350', 'Ford', 'Bus'),
(3, 'Daily 70C', 'Iveco', 'Truck'),
(4, 'E-Citaro', 'Mercedes-Benz', 'Electric Bus');

-- 2. FuelType
INSERT INTO FuelType (fuelType_id, fuel_name, fuel_price)
VALUES
(1, 'Diesel', 7.50),
(2, 'Petrol', 8.10),
(3, 'Electric', 3.20),
(4, 'Hybrid', 5.80);

-- 3. Depot
-- Depot Delta is intentionally included with no maintenance logs last month
-- so Inquiry 4 has a clear answer.
INSERT INTO Depot (Depot_ID, Depot_Name, Location, Capacity)
VALUES
(1, 'Depot Alpha', 'Cairo', 50),
(2, 'Depot Beta', 'Alexandria', 35),
(3, 'Depot Gamma', 'Giza', 40),
(4, 'Depot Delta', 'Mansoura', 25);

-- 4. Spare_Part
-- Stock_Quantity was added because it is part of the requirements.
INSERT INTO Spare_Part (Part_Serial, Name, Unit_Cost, Stock_Quantity)
VALUES
(101, 'Engine Oil Filter', 45.00, 120),
(102, 'Brake Pads Set', 120.00, 80),
(103, 'Air Filter', 30.00, 150),
(104, 'Timing Belt', 200.00, 40),
(105, 'Alternator', 350.00, 20),
(106, 'Radiator Hose', 55.00, 90),
(107, 'Battery Pack', 600.00, 15),
(108, 'Wheel Bearing', 180.00, 35);

-- 5. Mechanic
-- Each mechanic belongs to one depot.
INSERT INTO Mechanic
(Mechanic_ID, Mechanic_name, Specialization, Experience_Level, Hire_Date, Depot_ID)
VALUES
(1, 'Ahmed Saber', 'Engine Repair', 'Senior', '2018-03-15', 1),
(2, 'Mona Khaled', 'Electrical', 'Mid-Level', '2020-07-01', 2),
(3, 'Omar Fathy', 'Brake Systems', 'Junior', '2022-01-10', 1),
(4, 'Sara Mahmoud', 'General', 'Senior', '2016-09-22', 3),
(5, 'Karim Nasser', 'Cooling System', 'Mid-Level', '2019-05-18', 4);

-- 6. Vehicle
-- Vehicle 6 is intentionally included with no maintenance logs last month
-- so Inquiry 2 has a clear answer.
INSERT INTO Vehicle
(vehicle_id, model_id, fuelType_id, Depot_ID, vehicle_code, vehicle_name, status,
 manufacture_year, passenger_capacity, Current_Mileage)
VALUES
(1, 1, 1, 1, 'VH-001', 'Alpha-1', 'Operational', 2019, 16, 85000),
(2, 2, 1, 2, 'VH-002', 'Beta-1', 'Operational', 2020, 40, 60000),
(3, 1, 1, 1, 'VH-003', 'Alpha-2', 'Under Repair', 2018, 16, 120000),
(4, 4, 3, 3, 'VH-004', 'Gamma-1', 'Operational', 2021, 45, 30000),
(5, 3, 2, 3, 'VH-005', 'Gamma-2', 'Out of Service', 2017, 0, 200000),
(6, 2, 4, 4, 'VH-006', 'Delta-1', 'Operational', 2022, 40, 15000);

-- 7. Maintenance_Log
-- Last-month logs are inserted using @StartOfLastMonth.
-- Model 1 / Sprinter 316 intentionally has the most logs last month:
--   Vehicle 1 has 2 logs
--   Vehicle 3 has 2 logs
-- This makes Inquiry 1 meaningful.
-- Mechanic 1 intentionally has the most closed logs last month.
INSERT INTO Maintenance_Log
(Log_ID, Open_Date, Close_Date, Issue_Description, Log_Status, Vehicle_ID, Mechanic_ID, Depot_ID)
VALUES
-- Last month: Depot Alpha / Model Sprinter 316
(1, DATEADD(DAY, 1, @StartOfLastMonth), DATEADD(DAY, 3, @StartOfLastMonth),
 'Engine oil leak detected and fixed', 'Closed', 1, 1, 1),

(2, DATEADD(DAY, 5, @StartOfLastMonth), DATEADD(DAY, 7, @StartOfLastMonth),
 'Brake pads worn out and replaced', 'Closed', 1, 3, 1),

(3, DATEADD(DAY, 9, @StartOfLastMonth), DATEADD(DAY, 12, @StartOfLastMonth),
 'Timing belt replacement', 'Closed', 3, 1, 1),

(4, DATEADD(DAY, 14, @StartOfLastMonth), NULL,
 'Engine overheating issue still under diagnosis', 'Open', 3, 1, 1),

-- Last month: Depot Beta / Model Transit 350
(5, DATEADD(DAY, 4, @StartOfLastMonth), DATEADD(DAY, 6, @StartOfLastMonth),
 'Electrical wiring issue fixed', 'Closed', 2, 2, 2),

-- Last month: Depot Gamma / Models E-Citaro and Daily 70C
(6, DATEADD(DAY, 10, @StartOfLastMonth), DATEADD(DAY, 11, @StartOfLastMonth),
 'Routine air filter replacement', 'Closed', 4, 4, 3),

(7, DATEADD(DAY, 15, @StartOfLastMonth), DATEADD(DAY, 17, @StartOfLastMonth),
 'Alternator failure repaired', 'Closed', 5, 4, 3),

-- Older logs: should NOT appear in "last month" reports
(8, DATEADD(MONTH, -3, @StartOfLastMonth), DATEADD(DAY, 2, DATEADD(MONTH, -3, @StartOfLastMonth)),
 'Old radiator hose repair', 'Closed', 6, 5, 4),

-- Current month: should NOT appear in "last month" reports
(9, DATEADD(DAY, 1, @StartOfThisMonth), NULL,
 'Current month inspection request', 'Open', 2, 2, 2);

-- 8. Usage_ID
-- Prices are captured at transaction time.
INSERT INTO Usage_ID
(Usage_ID, Part_Serial, Log_ID, Quantity, Price_at_Transaction)
VALUES
-- Log 1 / Vehicle 1
(1, 101, 1, 2, 45.00),
(2, 103, 1, 1, 30.00),

-- Log 2 / Vehicle 1
(3, 102, 2, 1, 120.00),

-- Log 3 / Vehicle 3
(4, 104, 3, 1, 200.00),
(5, 108, 3, 2, 180.00),

-- Log 4 / Vehicle 3 open repair
(6, 106, 4, 2, 55.00),

-- Log 5 / Vehicle 2
(7, 107, 5, 1, 600.00),

-- Log 6 / Vehicle 4
(8, 103, 6, 2, 30.00),

-- Log 7 / Vehicle 5
(9, 105, 7, 1, 350.00),
(10, 101, 7, 1, 45.00),

-- Older log / Vehicle 6, should not appear in last-month reports
(11, 106, 8, 1, 55.00),

-- Current-month log / Vehicle 2, should not appear in last-month reports
(12, 102, 9, 1, 120.00);

PRINT 'FleetMaintenanceDB seed data inserted successfully.';

SELECT 'VehicleModel' AS TableName, COUNT(*) AS [RowCount] FROM VehicleModel
UNION ALL SELECT 'FuelType', COUNT(*) FROM FuelType
UNION ALL SELECT 'Depot', COUNT(*) FROM Depot
UNION ALL SELECT 'Spare_Part', COUNT(*) FROM Spare_Part
UNION ALL SELECT 'Mechanic', COUNT(*) FROM Mechanic
UNION ALL SELECT 'Vehicle', COUNT(*) FROM Vehicle
UNION ALL SELECT 'Maintenance_Log', COUNT(*) FROM Maintenance_Log
UNION ALL SELECT 'Usage_ID', COUNT(*) FROM Usage_ID;
GO
