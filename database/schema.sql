-- ============================================================
-- Dawson's Laundry System - MySQL Schema
-- Run this in MySQL Workbench (or `mysql -u root -p < schema.sql`)
-- ============================================================

CREATE DATABASE IF NOT EXISTS dawsons_laundry
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE dawsons_laundry;

-- ── Roles ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS roles (
    id      INT PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(20) UNIQUE NOT NULL          -- 'ADMIN', 'CASHIER'
);

INSERT INTO roles (name) VALUES ('ADMIN'), ('CASHIER')
    ON DUPLICATE KEY UPDATE name = name;

-- ── Users ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    full_name     VARCHAR(100) NOT NULL,
    username      VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,          -- BCrypt hash
    role_id       INT NOT NULL,
    active        BOOLEAN DEFAULT TRUE,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Default admin: username = admin / password = Admin@123
-- (hash below is a BCrypt hash generated for "Admin@123" — the app will
--  also happily create the first admin for you via /api/auth/signup if
--  you prefer not to rely on this seed row.)
INSERT INTO users (full_name, username, password_hash, role_id, active)
VALUES ('System Admin', 'admin',
        '$2a$10$nI2ZnVgED8DgCYBE1eJ0je1EE41lY8zAMCzpbu.A1cZHbNd/.wunq',
        (SELECT id FROM roles WHERE name = 'ADMIN'), TRUE)
    ON DUPLICATE KEY UPDATE username = username;

-- ── Products ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
    id      INT PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(100) NOT NULL,
    price   DECIMAL(10,2) NOT NULL,
    active  BOOLEAN DEFAULT TRUE                  -- soft-disable, never hard delete
);

INSERT INTO products (name, price) VALUES
    ('Shalwar Kameez Cotton',      260),
    ('Shalwar Kameez Wash n Wear', 250),
    ('Shalwar Kameez Boski',       550),
    ('Shirt',                      130),
    ('Pant',                       200),
    ('Coat',                       750),
    ('Waist Coat',                 700),
    ('Kambal',                     750),
    ('Towel',                      130),
    ('Waist / Sando',               70)
    ON DUPLICATE KEY UPDATE name = name;

-- ── Bills ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bills (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    receipt_no      INT UNIQUE NOT NULL,
    customer_name   VARCHAR(100) NOT NULL,
    customer_phone  VARCHAR(30),
    create_date     DATE NOT NULL,
    delivery_date   DATE NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    status          ENUM('PENDING','PAID','VOIDED','RETURNED') DEFAULT 'PENDING',
    created_by      INT NOT NULL,
    updated_by      INT,
    void_reason     VARCHAR(255),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- ── Bill Items ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bill_items (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    bill_id     INT NOT NULL,
    product_id  INT,
    name        VARCHAR(100) NOT NULL,             -- snapshot at time of billing
    quantity    INT NOT NULL,
    price       DECIMAL(10,2) NOT NULL,             -- snapshot at time of billing
    subtotal    DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ── Audit Log — every action, by whom, when ───────────────
CREATE TABLE IF NOT EXISTS audit_log (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT NOT NULL,
    action      VARCHAR(50) NOT NULL,     -- CREATE_BILL, MARK_PAID, EDIT_BILL, VOID_BILL, ADD_PRODUCT, EDIT_PRODUCT, DISABLE_PRODUCT, CREATE_USER, LOGIN
    entity_type VARCHAR(50) NOT NULL,     -- BILL, PRODUCT, USER
    entity_id   INT,
    details     TEXT,
    timestamp   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_bills_status ON bills(status);
CREATE INDEX idx_bills_create_date ON bills(create_date);
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);


-- Use your database
USE dawsons_laundry;

-- Fix 1: Change details column from TEXT to TINYTEXT to match @Lob expectation
ALTER TABLE audit_log MODIFY COLUMN details TINYTEXT;

-- Fix 2: Ensure status enum matches exactly what Hibernate expects
ALTER TABLE bills MODIFY COLUMN status ENUM('PENDING','PAID','VOIDED','RETURNED') DEFAULT 'PENDING';

-- Fix 3: Verify all decimal columns have correct precision
ALTER TABLE bills MODIFY COLUMN total_amount DECIMAL(10,2) NOT NULL;
ALTER TABLE bill_items MODIFY COLUMN price DECIMAL(10,2) NOT NULL;
ALTER TABLE bill_items MODIFY COLUMN subtotal DECIMAL(10,2) NOT NULL;
ALTER TABLE products MODIFY COLUMN price DECIMAL(10,2) NOT NULL;

-- Fix 4: Make sure all NOT NULL constraints match entities
ALTER TABLE bills MODIFY COLUMN receipt_no INT UNIQUE NOT NULL;
ALTER TABLE bills MODIFY COLUMN customer_name VARCHAR(100) NOT NULL;
ALTER TABLE bills MODIFY COLUMN create_date DATE NOT NULL;
ALTER TABLE bills MODIFY COLUMN delivery_date DATE NOT NULL;
ALTER TABLE bills MODIFY COLUMN created_by INT NOT NULL;

-- Verify the changes
DESCRIBE audit_log;
DESCRIBE bills;

-- Use your database
USE dawsons_laundry;

-- Add is_custom column to products table
ALTER TABLE products ADD COLUMN is_custom BOOLEAN DEFAULT FALSE;

-- Add is_custom column to bill_items table
ALTER TABLE bill_items ADD COLUMN is_custom BOOLEAN DEFAULT FALSE;

-- Insert a custom item placeholder
INSERT INTO products (name, price, active, is_custom) 
VALUES ('➕ Custom Item', 0, TRUE, TRUE);

-- Verify the changes
SELECT * FROM products WHERE is_custom = TRUE;
DESCRIBE bill_items;

-- ============================================================
-- Customer Management
-- ============================================================

CREATE TABLE IF NOT EXISTS customers (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(30),
    address         VARCHAR(200),
    total_orders    INT DEFAULT 0,
    total_spent     DECIMAL(10,2) DEFAULT 0.00,
    last_order_date DATETIME,
    notes           VARCHAR(500),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Add customer_id to bills table
ALTER TABLE bills ADD COLUMN customer_id INT;
ALTER TABLE bills ADD FOREIGN KEY (customer_id) REFERENCES customers(id);

-- Index for searching
CREATE INDEX idx_customers_name ON customers(name);
CREATE INDEX idx_customers_phone ON customers(phone);

-- Use your database
USE dawsons_laundry;

-- Update status enum to include new values
ALTER TABLE bills MODIFY COLUMN status ENUM('PENDING','PROCESSING','READY','PAID','VOIDED','RETURNED') DEFAULT 'PENDING';

-- Add tracking timestamps
ALTER TABLE bills ADD COLUMN IF NOT EXISTS status_updated_at DATETIME;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS processing_started_at DATETIME;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS ready_at DATETIME;

-- Update existing bills - set PROCESSING and READY based on status
UPDATE bills SET status = 'PENDING' WHERE status = 'PENDING';
UPDATE bills SET status = 'PAID' WHERE status = 'PAID';
UPDATE bills SET status = 'VOIDED' WHERE status = 'VOIDED';
UPDATE bills SET status = 'RETURNED' WHERE status = 'RETURNED';

-- Set timestamps for existing bills
UPDATE bills SET status_updated_at = created_at WHERE status_updated_at IS NULL;
UPDATE bills SET processing_started_at = created_at WHERE status IN ('PROCESSING', 'READY', 'PAID') AND processing_started_at IS NULL;
UPDATE bills SET ready_at = created_at WHERE status IN ('READY', 'PAID') AND ready_at IS NULL;

-- Verify the changes
DESCRIBE bills;