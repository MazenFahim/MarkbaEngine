/*==============================================================
  Database: Fleet Maintenance Management System
  File: 04_Create_App_Login.sql
  Purpose:
    Creates the SQL Server login used by the JavaFX application.

  Important:
    The password here must match DBConnection.java exactly.
==============================================================*/

USE master;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.sql_logins
    WHERE name = 'fleet_app'
)
BEGIN
    CREATE LOGIN fleet_app
    WITH PASSWORD = 'fleetApp@12345',
    CHECK_POLICY = OFF;
END
GO

ALTER LOGIN fleet_app ENABLE;
GO

ALTER LOGIN fleet_app
WITH PASSWORD = 'fleetApp@12345',
CHECK_POLICY = OFF;
GO

USE FleetMaintenanceDB;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.database_principals
    WHERE name = 'fleet_app'
)
BEGIN
    CREATE USER fleet_app FOR LOGIN fleet_app;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.database_role_members rm
    INNER JOIN sys.database_principals r
        ON rm.role_principal_id = r.principal_id
    INNER JOIN sys.database_principals u
        ON rm.member_principal_id = u.principal_id
    WHERE r.name = 'db_owner'
      AND u.name = 'fleet_app'
)
BEGIN
    ALTER ROLE db_owner ADD MEMBER fleet_app;
END
GO

PRINT 'fleet_app login/user is ready.';
GO
