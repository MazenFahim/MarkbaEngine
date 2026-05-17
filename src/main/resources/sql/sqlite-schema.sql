CREATE TABLE IF NOT EXISTS depot (
    depot_id INTEGER PRIMARY KEY AUTOINCREMENT,
    depot_name TEXT NOT NULL UNIQUE,
    location TEXT NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity >= 0)
);

CREATE TABLE IF NOT EXISTS vehicle_model (
    model_id INTEGER PRIMARY KEY AUTOINCREMENT,
    model_name TEXT NOT NULL,
    manufacturer TEXT NOT NULL,
    vehicle_type TEXT NOT NULL,
    fuel_type TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicle (
    vehicle_id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_code TEXT NOT NULL UNIQUE,
    plate_number TEXT NOT NULL UNIQUE,
    vehicle_name TEXT NOT NULL,
    model_id INTEGER NOT NULL,
    depot_id INTEGER NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('Active', 'Under Maintenance', 'Retired')),
    manufacture_year INTEGER NOT NULL,
    passenger_capacity INTEGER NOT NULL CHECK (passenger_capacity >= 0),
    current_mileage REAL NOT NULL CHECK (current_mileage >= 0),
    FOREIGN KEY (model_id) REFERENCES vehicle_model(model_id) ON UPDATE CASCADE,
    FOREIGN KEY (depot_id) REFERENCES depot(depot_id) ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS mechanic (
    mechanic_id INTEGER PRIMARY KEY AUTOINCREMENT,
    mechanic_code TEXT NOT NULL UNIQUE,
    mechanic_name TEXT NOT NULL,
    specialization TEXT NOT NULL,
    phone TEXT,
    depot_id INTEGER NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('Active', 'On Leave', 'Inactive')),
    FOREIGN KEY (depot_id) REFERENCES depot(depot_id) ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS supplier (
    supplier_id INTEGER PRIMARY KEY AUTOINCREMENT,
    supplier_name TEXT NOT NULL UNIQUE,
    phone TEXT,
    email TEXT
);

CREATE TABLE IF NOT EXISTS spare_part (
    part_id INTEGER PRIMARY KEY AUTOINCREMENT,
    part_code TEXT NOT NULL UNIQUE,
    part_name TEXT NOT NULL,
    unit_cost REAL NOT NULL CHECK (unit_cost >= 0),
    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
    supplier_id INTEGER NOT NULL,
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id) ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS maintenance_log (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id INTEGER NOT NULL,
    mechanic_id INTEGER NOT NULL,
    open_date TEXT NOT NULL,
    close_date TEXT,
    description TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('Open', 'In Progress', 'Closed', 'Cancelled')),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id) ON UPDATE CASCADE,
    FOREIGN KEY (mechanic_id) REFERENCES mechanic(mechanic_id) ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS part_usage (
    usage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    log_id INTEGER NOT NULL,
    part_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_cost_at_time REAL NOT NULL CHECK (unit_cost_at_time >= 0),
    FOREIGN KEY (log_id) REFERENCES maintenance_log(log_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (part_id) REFERENCES spare_part(part_id) ON UPDATE CASCADE
);
