# MarkbaEngine Database Setup

This JavaFX application uses **Microsoft SQL Server**, not SQLite.

Pulling the GitHub project gives you the Java code only.  
Each developer must also create the local SQL Server database before running the app.

## Required software

1. Microsoft SQL Server
2. SQL Server Management Studio (SSMS)
3. Java / Maven project opened in IntelliJ or VS Code

## Required SQL Server settings

Before running the Java app:

1. Enable TCP/IP for SQL Server.
2. Set SQL Server to listen on port `1433`.
3. Enable **SQL Server and Windows Authentication mode**.
4. Restart SQL Server.

## Run scripts in this exact order

Open SSMS and execute these files one by one:

1. `01_Create_FleetMaintenanceDB.sql`
2. `02_Seed_FleetMaintenanceDB.sql`
3. `04_Create_App_Login.sql`

Optional, for testing reports manually:

4. `03_Inquiry_Reports.sql`

## Java connection

The Java app connects to:

```text
Server: localhost
Port: 1433
Database: FleetMaintenanceDB
Username: fleet_app
Password: fleetApp@12345
```

The password must match the value inside:

```text
src/main/java/com/markbaengine/db/DBConnection.java
```

## Common errors

### TCP/IP connection failed

SQL Server is not listening on port `1433`.

Fix:
- Enable TCP/IP in SQL Server Configuration Manager.
- Set TCP Port to `1433`.
- Restart SQL Server.

### Login failed for user fleet_app

The app login was not created or the password is different.

Fix:
- Run `04_Create_App_Login.sql`.
- Make sure the password matches `DBConnection.java`.

### Cannot open database FleetMaintenanceDB

The database does not exist yet.

Fix:
- Run `01_Create_FleetMaintenanceDB.sql`.

### Invalid object name

The database exists, but the tables were not created correctly.

Fix:
- Run `01_Create_FleetMaintenanceDB.sql` again.
- Then run `02_Seed_FleetMaintenanceDB.sql`.

## Notes

- The SQL files are not executed automatically by Java.
- They must be run manually in SSMS on every new machine.
- `03_Inquiry_Reports.sql` contains the six required project inquiry queries.
