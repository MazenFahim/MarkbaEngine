# MarkbaEngine

MarkbaEngine is a JavaFX Maven application for the **Urban Fleet & Maintenance Hub** database project.

It is a guided DDL/DML-style database engine for one specific schema. Users can manage the Urban Fleet database through forms and tables without writing SQL manually.

## Run

```bash
mvn clean javafx:run
```

## Application flow

1. Choose a database workspace:
   - Select a valid Urban Fleet `.sql` file.
   - Or create an empty Urban Fleet database `.sql` file.
   - Or use the bundled demo workspace.
2. Continue to the dashboard.
3. Open tables and perform insert, update, delete, select, and join-report operations.

## Workspace notes

The selected Urban Fleet tables are stored in the active workspace database. When importing a `.sql` file, the app validates that it contains the required schema, then creates a local SQLite workspace under:

```text
workspaces/
```

When using **Create Empty Database**, the app saves an empty `.sql` schema file and creates a matching `.db` workspace beside it.

## Required Urban Fleet tables

```text
depot
vehicle_model
vehicle
mechanic
supplier
spare_part
maintenance_log
part_usage
```

## SQL Server deliverable

The SQL Server DDL file is available at:

```text
docs/sqlserver-ddl.sql
```

The JavaFX application uses SQLite locally for easy testing, but the SQL Server DDL is included for the database course deliverable.
