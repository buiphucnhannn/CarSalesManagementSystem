-- CarSalesMS seed data
USE car_sales_ms;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE warranties;
TRUNCATE TABLE test_drives;
TRUNCATE TABLE installment_plans;
TRUNCATE TABLE invoices;
TRUNCATE TABLE payments;
TRUNCATE TABLE sale_order_details;
TRUNCATE TABLE sale_orders;
TRUNCATE TABLE cars;
TRUNCATE TABLE promotions;
TRUNCATE TABLE accounts;
TRUNCATE TABLE customers;
TRUNCATE TABLE staffs;
TRUNCATE TABLE car_categories;
TRUNCATE TABLE brands;
TRUNCATE TABLE branches;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO branches (id, branch_code, branch_name, address, phone, email, status) VALUES
(1, 'BR-HCM-01', 'HCM District 1 Showroom', '12 Nguyen Hue, District 1, HCM', '02838220001', 'd1@carsalesms.vn', 'ACTIVE'),
(2, 'BR-HCM-07', 'HCM District 7 Showroom', '99 Nguyen Thi Thap, District 7, HCM', '02838220007', 'd7@carsalesms.vn', 'ACTIVE');

INSERT INTO brands (id, brand_code, brand_name, country, description, status) VALUES
(1, 'BRAND-TOYOTA', 'Toyota', 'Japan', 'Reliable passenger cars', 'ACTIVE'),
(2, 'BRAND-MAZDA', 'Mazda', 'Japan', 'Modern design and efficiency', 'ACTIVE'),
(3, 'BRAND-KIA', 'Kia', 'Korea', 'Value-focused line up', 'ACTIVE'),
(4, 'BRAND-HYUNDAI', 'Hyundai', 'Korea', 'Popular sedans and SUVs', 'ACTIVE');

INSERT INTO car_categories (id, category_code, category_name, description, status) VALUES
(1, 'CAT-SEDAN', 'Sedan', 'Family sedan models', 'ACTIVE'),
(2, 'CAT-SUV', 'SUV', 'Compact and mid-size SUVs', 'ACTIVE'),
(3, 'CAT-MPV', 'MPV', 'Multi-purpose vehicles', 'ACTIVE');

INSERT INTO staffs (id, staff_code, full_name, email, phone, role, branch_id, status) VALUES
(1, 'ST-ADMIN-01', 'Nguyen Van Admin', 'admin@carsalesms.vn', '0909000001', 'ADMIN', 1, 'ACTIVE'),
(2, 'ST-STAFF-01', 'Tran Minh Khang', 'khang@carsalesms.vn', '0909000002', 'STAFF', 1, 'ACTIVE'),
(3, 'ST-STAFF-02', 'Le Thu Hang', 'hang@carsalesms.vn', '0909000003', 'STAFF', 2, 'ACTIVE'),
(4, 'ST-ADMIN-02', 'Pham Quoc Bao', 'bao.admin@carsalesms.vn', '0909000004', 'ADMIN', 2, 'ACTIVE'),
(5, 'ST-STAFF-03', 'Vo Thanh Dat', 'dat@carsalesms.vn', '0909000005', 'STAFF', 1, 'ACTIVE'),
(6, 'ST-STAFF-04', 'Nguyen Thi Mai', 'mai@carsalesms.vn', '0909000006', 'STAFF', 2, 'ACTIVE'),
(7, 'ST-STAFF-05', 'Ho Minh Tuan', 'tuan@carsalesms.vn', '0909000007', 'STAFF', 1, 'ACTIVE');

INSERT INTO accounts (id, username, password_hash, staff_id, status, is_locked, failed_login_attempts, last_login_at) VALUES
-- Tài khoản cũ (SHA-256 hash)
(1, 'admin',    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 1, 'ACTIVE', 0, 0, NULL),
(2, 'staff01',  '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 2, 'ACTIVE', 0, 0, NULL),
(3, 'staff02',  '6bc0a63cb29c92306020c0a6bbc358cc4628db277dc06e253535e126517ad637', 3, 'ACTIVE', 0, 0, NULL),
(4, 'admin2',     'admin2',  4, 'ACTIVE', 0, 0, NULL),
(5, 'nhanvien1',  '1234',    5, 'ACTIVE', 0, 0, NULL),
(6, 'nhanvien2',  '1234',    6, 'ACTIVE', 0, 0, NULL),
(7, 'nhanvien3',  'abcd',    7, 'ACTIVE', 0, 0, NULL);

INSERT INTO customers (id, customer_code, full_name, phone, email, gender, date_of_birth, identity_number, address, note) VALUES
(1, 'CUST-0001', 'Nguyen Van An', '0908111222', 'an@gmail.com', 'MALE', '1992-02-10', '079092001111', 'Thu Duc, HCM', 'Repeat customer'),
(2, 'CUST-0002', 'Tran Thi Bich', '0908333444', 'bich@gmail.com', 'FEMALE', '1995-09-21', '079095552222', 'District 7, HCM', 'Prefers white cars'),
(3, 'CUST-0003', 'Pham Gia Bao', '0908555666', 'bao@gmail.com', 'MALE', '1988-01-05', '079088883333', 'District 1, HCM', 'Business buyer'),
(4, 'CUST-0004', 'Le Minh Chau', '0908777888', 'chau@gmail.com', 'OTHER', '1999-07-12', '079099994444', 'Binh Thanh, HCM', 'Interested in installment plans');

INSERT INTO promotions (id, promotion_code, promotion_name, discount_type, discount_value, start_date, end_date, description, status) VALUES
(1, 'PROMO-Q2-5P', 'Q2 Discount 5 Percent', 'PERCENT', 5.00, '2026-04-01', '2026-06-30', 'Applied to selected sedans', 'ACTIVE'),
(2, 'PROMO-CASH-15M', 'Cash Voucher 15M', 'AMOUNT', 15000000.00, '2026-01-01', '2026-12-31', 'Flat discount for full-payment orders', 'ACTIVE');

INSERT INTO cars (id, car_code, car_name, brand_id, category_id, branch_id, color, seat_count, fuel_type, transmission, origin, manufacture_year, import_price, sale_price, quantity, available_quantity, description, status) VALUES
(1, 'CAR-TOY-VIOS-G', 'Toyota Vios G', 1, 1, 1, 'White', 5, 'Gasoline', 'Automatic', 'Vietnam', 2025, 430000000.00, 515000000.00, 6, 4, 'Popular compact sedan', 'ACTIVE'),
(2, 'CAR-MZD-CX5-DEL', 'Mazda CX-5 Deluxe', 2, 2, 1, 'Red', 5, 'Gasoline', 'Automatic', 'Japan', 2025, 710000000.00, 829000000.00, 4, 3, 'Best-selling SUV line', 'ACTIVE'),
(3, 'CAR-KIA-SELTOS', 'Kia Seltos Premium', 3, 2, 2, 'Black', 5, 'Gasoline', 'Automatic', 'Korea', 2025, 620000000.00, 739000000.00, 5, 5, 'Urban SUV', 'ACTIVE'),
(4, 'CAR-HYU-ACCENT', 'Hyundai Accent AT', 4, 1, 2, 'Silver', 5, 'Gasoline', 'Automatic', 'Vietnam', 2025, 420000000.00, 499000000.00, 7, 6, 'Economy sedan', 'ACTIVE'),
(5, 'CAR-TOY-INNOVA', 'Toyota Innova Cross', 1, 3, 1, 'Gray', 7, 'Hybrid', 'Automatic', 'Indonesia', 2025, 730000000.00, 860000000.00, 3, 2, 'Family MPV', 'ACTIVE'),
(6, 'CAR-MZD-3-LUX', 'Mazda 3 Luxury', 2, 1, 2, 'Blue', 5, 'Gasoline', 'Automatic', 'Thailand', 2024, 560000000.00, 679000000.00, 4, 4, 'Sport sedan feel', 'ACTIVE');

INSERT INTO sale_orders (id, order_code, customer_id, staff_id, promotion_id, order_date, total_amount, discount_amount, final_amount, payment_method, order_status, note) VALUES
(1, 'SO-260301', 1, 2, 1, '2026-03-01 10:15:00', 829000000.00, 41450000.00, 787550000.00, 'BANK_TRANSFER', 'PAID', 'Customer requested urgent delivery'),
(2, 'SO-260312', 2, 3, 2, '2026-03-12 15:20:00', 739000000.00, 15000000.00, 724000000.00, 'INSTALLMENT', 'CONFIRMED', 'Approved installment in 3 terms'),
(3, 'SO-260320', 3, 2, NULL, '2026-03-20 09:40:00', 499000000.00, 0.00, 499000000.00, 'CASH', 'PENDING', 'Awaiting deposit confirmation');

INSERT INTO sale_order_details (id, sale_order_id, car_id, quantity, unit_price, discount_amount, line_total, note) VALUES
(1, 1, 2, 1, 829000000.00, 41450000.00, 787550000.00, 'Applied PROMO-Q2-5P'),
(2, 2, 3, 1, 739000000.00, 15000000.00, 724000000.00, 'Applied PROMO-CASH-15M'),
(3, 3, 4, 1, 499000000.00, 0.00, 499000000.00, 'No promotion');

INSERT INTO payments (id, payment_code, sale_order_id, payment_date, amount, payment_method, payment_status, transaction_reference, note) VALUES
(1, 'PAY-260301-01', 1, '2026-03-01 11:05:00', 300000000.00, 'BANK_TRANSFER', 'COMPLETED', 'VCB-998811', 'Initial transfer'),
(2, 'PAY-260302-02', 1, '2026-03-02 16:30:00', 487550000.00, 'BANK_TRANSFER', 'COMPLETED', 'VCB-998812', 'Final transfer'),
(3, 'PAY-260312-01', 2, '2026-03-12 17:20:00', 224000000.00, 'INSTALLMENT', 'COMPLETED', 'FIN-AGREE-20260312', 'Down payment'),
(4, 'PAY-260323-01', 2, '2026-03-23 10:00:00', 200000000.00, 'INSTALLMENT', 'COMPLETED', 'FIN-TERM1-20260323', 'Term 1');

INSERT INTO invoices (id, invoice_code, sale_order_id, issued_date, invoice_status, tax_amount, total_amount, note) VALUES
(1, 'INV-260301', 1, '2026-03-02 17:00:00', 'ISSUED', 71595455.00, 787550000.00, 'VAT included'),
(2, 'INV-260312', 2, '2026-03-12 18:00:00', 'ISSUED', 65818182.00, 724000000.00, 'Issued for installment order');

INSERT INTO installment_plans (id, sale_order_id, installment_no, due_date, amount, paid_amount, installment_status, note) VALUES
(1, 2, 1, '2026-03-23', 200000000.00, 200000000.00, 'PAID', 'Paid on due date'),
(2, 2, 2, '2026-04-23', 200000000.00, 0.00, 'UNPAID', 'Upcoming'),
(3, 2, 3, '2026-05-23', 100000000.00, 0.00, 'UNPAID', 'Final term');

INSERT INTO test_drives (id, test_drive_code, customer_id, car_id, staff_id, scheduled_time, result, status, note) VALUES
(1, 'TD-260228-01', 4, 2, 2, '2026-02-28 14:00:00', 'Customer likes handling', 'COMPLETED', 'Potential close in Q2'),
(2, 'TD-260321-01', 1, 5, 2, '2026-03-21 09:30:00', NULL, 'SCHEDULED', 'Family test drive request'),
(3, 'TD-260324-01', 3, 4, 3, '2026-03-24 16:00:00', 'No show', 'CANCELLED', 'Customer busy');

INSERT INTO warranties (id, warranty_code, sale_order_detail_id, start_date, end_date, warranty_status, note) VALUES
(1, 'WAR-260301', 1, '2026-03-05', '2029-03-05', 'ACTIVE', '36 months manufacturer warranty'),
(2, 'WAR-260312', 2, '2026-03-15', '2029-03-15', 'ACTIVE', '36 months manufacturer warranty');

INSERT INTO audit_logs (id, staff_id, action, entity_name, entity_id, old_value, new_value, created_at) VALUES
(1, 1, 'CREATE', 'BRANCH', 2, NULL, '{"branchCode":"BR-HCM-07"}', '2026-02-01 08:10:00'),
(2, 2, 'CREATE', 'SALE_ORDER', 1, NULL, '{"orderCode":"SO-260301"}', '2026-03-01 10:16:00'),
(3, 2, 'UPDATE', 'SALE_ORDER', 1, '{"orderStatus":"CONFIRMED"}', '{"orderStatus":"PAID"}', '2026-03-02 16:32:00'),
(4, 3, 'CREATE', 'INSTALLMENT_PLAN', 2, NULL, '{"saleOrder":"SO-260312"}', '2026-03-12 18:05:00'),
(5, 1, 'UPDATE', 'ACCOUNT', 2, '{"failedLoginAttempts":1}', '{"failedLoginAttempts":0}', '2026-03-13 09:40:00');

