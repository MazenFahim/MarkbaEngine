package com.markbaengine.service;

import com.markbaengine.dao.ReportDao;
import com.markbaengine.model.ReportDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private final ReportDao reportDao = new ReportDao();
    private final Map<String, ReportDefinition> reports = new LinkedHashMap<>();

    public ReportService() {
        reports.put("model_max_logs_last_month", new ReportDefinition(
                "model_max_logs_last_month",
                "Inquiry 1: Vehicle Model With Maximum Maintenance Logs Last Month",
                """
                DECLARE @StartOfThisMonth DATE = DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1);
                DECLARE @StartOfLastMonth DATE = DATEADD(MONTH, -1, @StartOfThisMonth);

                SELECT TOP 1 WITH TIES
                    vm.model_id AS [Model ID],
                    vm.model_name AS [Model Name],
                    vm.manufacturer AS [Manufacturer],
                    vm.vehicle_type AS [Vehicle Type],
                    COUNT(ml.Log_ID) AS [Maintenance Log Count]
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
                ORDER BY COUNT(ml.Log_ID) DESC
                """
        ));

        reports.put("vehicles_without_logs_last_month", new ReportDefinition(
                "vehicles_without_logs_last_month",
                "Inquiry 2: Vehicles Without Maintenance Logs Last Month",
                """
                DECLARE @StartOfThisMonth DATE = DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1);
                DECLARE @StartOfLastMonth DATE = DATEADD(MONTH, -1, @StartOfThisMonth);

                SELECT
                    v.vehicle_id AS [Vehicle ID],
                    v.vehicle_code AS [Vehicle Code],
                    v.vehicle_name AS [Vehicle Name],
                    vm.model_name AS [Model Name],
                    d.Depot_Name AS [Depot],
                    v.status AS [Status]
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
                ORDER BY v.vehicle_id
                """
        ));

        reports.put("top_mechanic_last_month", new ReportDefinition(
                "top_mechanic_last_month",
                "Inquiry 3: Mechanic With Highest Completed Tasks Last Month",
                """
                DECLARE @StartOfThisMonth DATE = DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1);
                DECLARE @StartOfLastMonth DATE = DATEADD(MONTH, -1, @StartOfThisMonth);

                SELECT TOP 1 WITH TIES
                    m.Mechanic_ID AS [Mechanic ID],
                    m.Mechanic_name AS [Mechanic Name],
                    m.Specialization AS [Specialization],
                    m.Experience_Level AS [Experience Level],
                    d.Depot_Name AS [Depot],
                    COUNT(ml.Log_ID) AS [Completed Tasks Count]
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
                ORDER BY COUNT(ml.Log_ID) DESC
                """
        ));

        reports.put("depots_without_logs_last_month", new ReportDefinition(
                "depots_without_logs_last_month",
                "Inquiry 4: Depots Without Maintenance Logs Last Month",
                """
                DECLARE @StartOfThisMonth DATE = DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1);
                DECLARE @StartOfLastMonth DATE = DATEADD(MONTH, -1, @StartOfThisMonth);

                SELECT
                    d.Depot_ID AS [Depot ID],
                    d.Depot_Name AS [Depot Name],
                    d.Location AS [Location],
                    d.Capacity AS [Capacity]
                FROM Depot d
                LEFT JOIN Maintenance_Log ml
                    ON d.Depot_ID = ml.Depot_ID
                   AND ml.Open_Date >= @StartOfLastMonth
                   AND ml.Open_Date < @StartOfThisMonth
                WHERE ml.Log_ID IS NULL
                ORDER BY d.Depot_ID
                """
        ));

        reports.put("spare_parts_per_vehicle_last_month", new ReportDefinition(
                "spare_parts_per_vehicle_last_month",
                "Inquiry 5: Spare Parts Used For Each Vehicle Last Month",
                """
                DECLARE @StartOfThisMonth DATE = DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1);
                DECLARE @StartOfLastMonth DATE = DATEADD(MONTH, -1, @StartOfThisMonth);

                SELECT
                    v.vehicle_id AS [Vehicle ID],
                    v.vehicle_code AS [Vehicle Code],
                    v.vehicle_name AS [Vehicle Name],
                    ml.Log_ID AS [Log ID],
                    ml.Open_Date AS [Open Date],
                    sp.Part_Serial AS [Part Serial],
                    sp.Name AS [Part Name],
                    u.Quantity AS [Quantity],
                    u.Price_at_Transaction AS [Price at Transaction],
                    (u.Quantity * u.Price_at_Transaction) AS [Line Total Cost]
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
                    sp.Part_Serial
                """
        ));

        reports.put("total_parts_cost_per_vehicle_last_month", new ReportDefinition(
                "total_parts_cost_per_vehicle_last_month",
                "Inquiry 6: Total Parts Cost Per Vehicle Last Month",
                """
                DECLARE @StartOfThisMonth DATE = DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1);
                DECLARE @StartOfLastMonth DATE = DATEADD(MONTH, -1, @StartOfThisMonth);

                SELECT
                    v.vehicle_id AS [Vehicle ID],
                    v.vehicle_code AS [Vehicle Code],
                    v.vehicle_name AS [Vehicle Name],
                    COALESCE(SUM(u.Quantity * u.Price_at_Transaction), 0) AS [Total Parts Cost Last Month]
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
                    [Total Parts Cost Last Month] DESC,
                    v.vehicle_id
                """
        ));
    }

    public List<ReportDefinition> getReports() {
        return List.copyOf(reports.values());
    }

    public List<Map<String, Object>> runReport(ReportDefinition reportDefinition) {
        return reportDao.runReport(reportDefinition);
    }
}