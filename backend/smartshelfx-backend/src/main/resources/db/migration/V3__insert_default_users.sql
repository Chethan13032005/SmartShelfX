-- Insert default users for testing (passwords are 'password123' hashed with BCrypt)
-- BCrypt hash for 'password123': $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Admin User
INSERT INTO users (email, password, first_name, last_name, full_name, name, role, company, phone_number, warehouse_location, created_at, enabled)
VALUES (
    'admin@smartshelfx.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin',
    'User',
    'Admin User',
    'Admin User',
    'Admin',
    'SmartShelfX HQ',
    '+1-555-0100',
    'Main Warehouse',
    CURRENT_TIMESTAMP,
    1
) ON DUPLICATE KEY UPDATE email=email;

-- Manager User
INSERT INTO users (email, password, first_name, last_name, full_name, name, role, company, phone_number, warehouse_location, created_at, enabled)
VALUES (
    'manager@smartshelfx.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Manager',
    'Smith',
    'Manager Smith',
    'Manager Smith',
    'Manager',
    'SmartShelfX Store',
    '+1-555-0101',
    'Store Warehouse',
    CURRENT_TIMESTAMP,
    1
) ON DUPLICATE KEY UPDATE email=email;

-- Vendor User
INSERT INTO users (email, password, first_name, last_name, full_name, name, role, company, phone_number, warehouse_location, created_at, enabled)
VALUES (
    'vendor@smartshelfx.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Vendor',
    'Johnson',
    'Vendor Johnson',
    'Vendor Johnson',
    'Vendor',
    'Supply Co.',
    '+1-555-0102',
    'Vendor Warehouse',
    CURRENT_TIMESTAMP,
    1
) ON DUPLICATE KEY UPDATE email=email;
