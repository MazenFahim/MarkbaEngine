INSERT OR IGNORE INTO depot (depot_id, depot_name, location, capacity) VALUES
(1, 'Central Depot', 'Downtown', 80),
(2, 'North Workshop', 'North District', 40),
(3, 'Electric Service Hub', 'East Industrial Zone', 35),
(4, 'South Depot', 'South Ring Road', 50);

INSERT OR IGNORE INTO vehicle_model (model_id, model_name, manufacturer, vehicle_type, fuel_type) VALUES
(1, 'CityLine Bus 60', 'MetroMotors', 'Bus', 'Diesel'),
(2, 'Electric Shuttle E5', 'GreenDrive', 'Shuttle', 'Electric'),
(3, 'Cargo Van V14', 'UrbanAuto', 'Van', 'Gasoline'),
(4, 'Hybrid Pickup H2', 'RoadWorks', 'Pickup', 'Hybrid'),
(5, 'CNG Bus C55', 'MetroMotors', 'Bus', 'CNG');

INSERT OR IGNORE INTO vehicle (vehicle_id, vehicle_code, plate_number, vehicle_name, model_id, depot_id, status, manufacture_year, passenger_capacity, current_mileage) VALUES
(1, 'V-001', 'CAI-1001', 'City Line Bus 01', 1, 1, 'Active', 2019, 60, 125430.5),
(2, 'V-002', 'CAI-1002', 'City Line Bus 02', 1, 1, 'Under Maintenance', 2020, 60, 98700.0),
(3, 'V-003', 'GIZ-2001', 'Staff Van North 01', 3, 2, 'Active', 2021, 14, 34500.0),
(4, 'V-004', 'GIZ-2002', 'North Minibus 01', 1, 2, 'Active', 2018, 22, 210300.0),
(5, 'V-005', 'CAI-3001', 'Electric Shuttle 01', 2, 3, 'Active', 2023, 8, 8900.0),
(6, 'V-006', 'HEL-4001', 'South Pickup 01', 4, 4, 'Retired', 2015, 4, 410000.0),
(7, 'V-007', 'HEL-4002', 'South CNG Bus 01', 5, 4, 'Active', 2022, 55, 57000.0);

INSERT OR IGNORE INTO mechanic (mechanic_id, mechanic_code, mechanic_name, specialization, phone, depot_id, status) VALUES
(1, 'M-001', 'Omar Hassan', 'Engines', '01010000001', 1, 'Active'),
(2, 'M-002', 'Mona Adel', 'Electric Systems', '01010000002', 3, 'Active'),
(3, 'M-003', 'Karim Nabil', 'Brakes', '01010000003', 2, 'Active'),
(4, 'M-004', 'Sara Youssef', 'Electronics', '01010000004', 1, 'On Leave'),
(5, 'M-005', 'Ahmed Tarek', 'General Maintenance', '01010000005', 4, 'Active');

INSERT OR IGNORE INTO supplier (supplier_id, supplier_name, phone, email) VALUES
(1, 'Metro Parts Co.', '0222222001', 'sales@metroparts.example'),
(2, 'GreenDrive Supply', '0222222002', 'parts@greendrive.example'),
(3, 'RoadWorks Components', '0222222003', 'orders@roadworks.example');

INSERT OR IGNORE INTO spare_part (part_id, part_code, part_name, unit_cost, stock_quantity, supplier_id) VALUES
(1, 'SP-001', 'Oil Filter', 180.00, 90, 1),
(2, 'SP-002', 'Brake Pads Set', 950.00, 35, 1),
(3, 'SP-003', 'Battery Module', 7500.00, 12, 2),
(4, 'SP-004', 'Spark Plug', 120.00, 150, 3),
(5, 'SP-005', 'Tire 22 inch', 3200.00, 28, 1);

INSERT OR IGNORE INTO maintenance_log (log_id, vehicle_id, mechanic_id, open_date, close_date, description, status) VALUES
(1, 2, 1, '2026-04-10', '2026-04-11', 'Engine overheating and oil leak inspection.', 'Closed'),
(2, 5, 2, '2026-04-18', '2026-04-18', 'Battery diagnostics and control unit update.', 'Closed'),
(3, 3, 3, '2026-04-25', NULL, 'Brake noise reported during morning route.', 'In Progress'),
(4, 1, 4, '2026-03-30', '2026-04-01', 'Dashboard sensor warning investigation.', 'Closed'),
(5, 7, 5, '2026-04-28', NULL, 'Routine inspection after 57,000 km.', 'Open');

INSERT OR IGNORE INTO part_usage (usage_id, log_id, part_id, quantity, unit_cost_at_time) VALUES
(1, 1, 1, 2, 180.00),
(2, 2, 3, 1, 7500.00),
(3, 3, 2, 1, 950.00),
(4, 4, 4, 6, 120.00),
(5, 5, 5, 2, 3200.00);
