-- CarSalesMS schema (MySQL 8+)
CREATE DATABASE IF NOT EXISTS car_sales_ms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE car_sales_ms;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS warranties;
DROP TABLE IF EXISTS test_drives;
DROP TABLE IF EXISTS installment_plans;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS sale_order_details;
DROP TABLE IF EXISTS sale_orders;
DROP TABLE IF EXISTS cars;
DROP TABLE IF EXISTS promotions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS staffs;
DROP TABLE IF EXISTS car_categories;
DROP TABLE IF EXISTS brands;
DROP TABLE IF EXISTS branches;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE branches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    branch_code VARCHAR(50) NOT NULL UNIQUE,
    branch_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(20),
    email VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE brands (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    brand_code VARCHAR(50) NOT NULL UNIQUE,
    brand_name VARCHAR(255) NOT NULL UNIQUE,
    country VARCHAR(100),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE car_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_code VARCHAR(50) NOT NULL UNIQUE,
    category_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE staffs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    staff_code VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    branch_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);

CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    staff_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_staff FOREIGN KEY (staff_id) REFERENCES staffs(id)
);

CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_code VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    gender VARCHAR(10),
    date_of_birth DATE,
    identity_number VARCHAR(50),
    address VARCHAR(500),
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE promotions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    promotion_code VARCHAR(50) NOT NULL UNIQUE,
    promotion_name VARCHAR(255) NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(18,2) NOT NULL DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE cars (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    car_code VARCHAR(50) NOT NULL UNIQUE,
    car_name VARCHAR(255) NOT NULL,
    brand_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    color VARCHAR(50),
    seat_count INT,
    fuel_type VARCHAR(50),
    transmission VARCHAR(50),
    origin VARCHAR(100),
    manufacture_year INT,
    import_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    sale_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL DEFAULT 0,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_car_brand FOREIGN KEY (brand_id) REFERENCES brands(id),
    CONSTRAINT fk_car_category FOREIGN KEY (category_id) REFERENCES car_categories(id),
    CONSTRAINT fk_car_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);

CREATE TABLE sale_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    promotion_id BIGINT NULL,
    order_date DATETIME NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(30) NOT NULL,
    order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_order_staff FOREIGN KEY (staff_id) REFERENCES staffs(id),
    CONSTRAINT fk_order_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id)
);

CREATE TABLE sale_order_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_order_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    line_total DECIMAL(18,2) NOT NULL DEFAULT 0,
    note TEXT,
    CONSTRAINT fk_order_detail_order FOREIGN KEY (sale_order_id) REFERENCES sale_orders(id),
    CONSTRAINT fk_order_detail_car FOREIGN KEY (car_id) REFERENCES cars(id)
);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_code VARCHAR(50) NOT NULL UNIQUE,
    sale_order_id BIGINT NOT NULL,
    payment_date DATETIME NOT NULL,
    amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    transaction_reference VARCHAR(100),
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY (sale_order_id) REFERENCES sale_orders(id)
);

CREATE TABLE invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_code VARCHAR(50) NOT NULL UNIQUE,
    sale_order_id BIGINT NOT NULL UNIQUE,
    issued_date DATETIME NOT NULL,
    invoice_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    tax_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoice_order FOREIGN KEY (sale_order_id) REFERENCES sale_orders(id)
);

CREATE TABLE installment_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_order_id BIGINT NOT NULL,
    installment_no INT NOT NULL,
    due_date DATE NOT NULL,
    amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    installment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_installment_order FOREIGN KEY (sale_order_id) REFERENCES sale_orders(id),
    CONSTRAINT uq_installment_plans_order_no UNIQUE (sale_order_id, installment_no)
);

CREATE TABLE test_drives (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_drive_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    scheduled_time DATETIME NOT NULL,
    result VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_test_drive_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_test_drive_car FOREIGN KEY (car_id) REFERENCES cars(id),
    CONSTRAINT fk_test_drive_staff FOREIGN KEY (staff_id) REFERENCES staffs(id)
);

CREATE TABLE warranties (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warranty_code VARCHAR(50) NOT NULL UNIQUE,
    sale_order_detail_id BIGINT NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    warranty_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_warranty_order_detail FOREIGN KEY (sale_order_detail_id) REFERENCES sale_order_details(id)
);

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    staff_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_log_staff FOREIGN KEY (staff_id) REFERENCES staffs(id)
);

