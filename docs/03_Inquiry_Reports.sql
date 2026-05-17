/*==============================================================
Database: Fleet Maintenance Management System
File: 03_Inquiry_Reports.sql
Purpose:
  Exact SQL Server queries for the 6 Urban Fleet & Maintenance Hub inquiries.

Definition of "last month":
  The previous calendar month.
  Example: if today is 2026-05-17, last month means 2026-04-01 to 2026-04-30.
==============================================================*/

USE FleetMaintenanceDB;
GO

DECLARE @StartOfThisMonth DATE = DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1);
DECLARE @StartOfLastMonth DATE = DATEADD(MONTH, -1, @StartOfThisMonth);

/*==============================================================
Inquiry 1
Which vehicle model required the maximum number of maintenance logs last month?
==============================================================*/
SELECT TOP 1 WITH TIES
    vm.model_id,
    vm.model_name,
    vm.manufacturer,
    vm.vehicle_type,
    COUNT(ml.Log_ID) AS maintenance_log_count
FROM VehicleModel vm
INNER JOIN Vehicle v
    ON vm.model_id = v.model_id
INNER JOIN Maintenance_Log ml
    ON v.vehicle_id = ml.Vehicle_ID
WHERE ml.Open_Date >= @StartOfLastMonth
  AND ml.Open_Date < @StartOfThisMonth
GROUP BY
    vm.model_id,
    vm.model_name,
    vm.manufacturer,
    vm.vehicle_type
ORDER BY COUNT(ml.Log_ID) DESC;

/*==============================================================
Inquiry 2
Which vehicle had no maintenance or repair logs opened during the last month?
==============================================================*/
SELECT
    v.vehicle_id,
    v.vehicle_code,
    v.vehicle_name,
    vm.model_name,
    d.Depot_Name,
    v.status
FROM Vehicle v
INNER JOIN VehicleModel vm
    ON v.model_id = vm.model_id
INNER JOIN Depot d
    ON v.Depot_ID = d.Depot_ID
LEFT JOIN Maintenance_Log ml
    ON v.vehicle_id = ml.Vehicle_ID
   AND ml.Open_Date >= @StartOfLastMonth
   AND ml.Open_Date < @StartOfThisMonth
WHERE ml.Log_ID IS NULL
ORDER BY v.vehicle_id;

/*==============================================================
Inquiry 3
Who was the mechanic who completed the highest number of repair tasks last month?
Completed means Log_Status = 'Closed'.
==============================================================*/
SELECT TOP 1 WITH TIES
    m.Mechanic_ID,
    m.Mechanic_name,
    m.Specialization,
    m.Experience_Level,
    d.Depot_Name,
    COUNT(ml.Log_ID) AS completed_tasks_count
FROM Mechanic m
INNER JOIN Depot d
    ON m.Depot_ID = d.Depot_ID
INNER JOIN Maintenance_Log ml
    ON m.Mechanic_ID = ml.Mechanic_ID
WHERE ml.Log_Status = 'Closed'
  AND ml.Open_Date >= @StartOfLastMonth
  AND ml.Open_Date < @StartOfThisMonth
GROUP BY
    m.Mechanic_ID,
    m.Mechanic_name,
    m.Specialization,
    m.Experience_Level,
    d.Depot_Name
ORDER BY COUNT(ml.Log_ID) DESC;

/*==============================================================
Inquiry 4
Identify depots that did not handle any maintenance logs last month.
==============================================================*/
SELECT
    d.Depot_ID,
    d.Depot_Name,
    d.Location,
    d.Capacity
FROM Depot d
LEFT JOIN Maintenance_Log ml
    ON d.Depot_ID = ml.Depot_ID
   AND ml.Open_Date >= @StartOfLastMonth
   AND ml.Open_Date < @StartOfThisMonth
WHERE ml.Log_ID IS NULL
ORDER BY d.Depot_ID;

/*==============================================================
Inquiry 5
What were the spare parts used for each vehicle during its repairs last month?
==============================================================*/
SELECT
    v.vehicle_id,
    v.vehicle_code,
    v.vehicle_name,
    ml.Log_ID,
    ml.Open_Date,
    sp.Part_Serial,
    sp.Name AS part_name,
    u.Quantity,
    u.Price_at_Transaction,
    (u.Quantity * u.Price_at_Transaction) AS line_total_cost
FROM Vehicle v
INNER JOIN Maintenance_Log ml
    ON v.vehicle_id = ml.Vehicle_ID
INNER JOIN Usage_ID u
    ON ml.Log_ID = u.Log_ID
INNER JOIN Spare_Part sp
    ON u.Part_Serial = sp.Part_Serial
WHERE ml.Open_Date >= @StartOfLastMonth
  AND ml.Open_Date < @StartOfThisMonth
ORDER BY
    v.vehicle_id,
    ml.Log_ID,
    sp.Part_Serial;

/*==============================================================
Inquiry 6
For each vehicle, retrieve its identification and the total cost of all parts used on it last month.
==============================================================*/
SELECT
    v.vehicle_id,
    v.vehicle_code,
    v.vehicle_name,
    COALESCE(SUM(u.Quantity * u.Price_at_Transaction), 0) AS total_parts_cost_last_month
FROM Vehicle v
LEFT JOIN Maintenance_Log ml
    ON v.vehicle_id = ml.Vehicle_ID
   AND ml.Open_Date >= @StartOfLastMonth
   AND ml.Open_Date < @StartOfThisMonth
LEFT JOIN Usage_ID u
    ON ml.Log_ID = u.Log_ID
GROUP BY
    v.vehicle_id,
    v.vehicle_code,
    v.vehicle_name
ORDER BY
    total_parts_cost_last_month DESC,
    v.vehicle_id;
GO
