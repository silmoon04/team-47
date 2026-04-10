-- test data for dev/integration work
-- run instead of 02 + 03, directly after 01_schema.sql
-- includes the main seeded set plus merged edge-case coverage
-- has customer/account edges, low-stock rows, order status variety,
-- null-field cases, reminder/statement rows, and pu online-order states

USE iposca_database;

-- CLEANUP
-- delete in reverse FK order so nothing blocks
-- every rerun starts fresh

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reminders;
TRUNCATE TABLE statements;
TRUNCATE TABLE payments;
TRUNCATE TABLE sale_items;
TRUNCATE TABLE sales;
TRUNCATE TABLE online_order_items;
TRUNCATE TABLE online_orders;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE discount_tiers;
TRUNCATE TABLE customers;
TRUNCATE TABLE stock;
TRUNCATE TABLE products;
TRUNCATE TABLE templates;
TRUNCATE TABLE users;
TRUNCATE TABLE system_config;
DELETE FROM merchant_details WHERE merchant_id = 1;
SET FOREIGN_KEY_CHECKS = 1;

-- need merchant details first (users FK depends on it)
INSERT INTO merchant_details (merchant_id, business_name, address, phone, email, sa_merchant_id, sa_username, sa_password) VALUES
    (1, 'Test Pharmacy', '14 Green Lane, London EC1V 0HB', '0207 000 0001', 'info@testpharmacy.local', 'ACC-0001', 'admin', 'admin123');

-- basic config
INSERT INTO system_config (config_key, config_value) VALUES
    ('vat_rate', '20.00'),
    ('currency_code', 'GBP'),
    ('debt_period_days', '30'),
    ('default_credit_limit', '150.00');

-- fixed columns, added random hash
INSERT INTO users (username, password_hash, full_name, role, merchant_id) VALUES
    ('admin',       '5cf9422de300411c28f198aff3e9efc4d7e3ae08bf73d0ca1310f0d1cee37782',   'System Administrator', 'ADMIN',      1),
    ('manager1',    'de4bb6065808183cd09377f85570f40941c840bef475d7fd7e8fe657cfc789de',  'Shakira Patel',       'MANAGER',    1),
    ('pharmacist1', '9feacbadee4eebd665ab123c06af28ed19c694a249f95d4a7bd4a2ff021840c3',  'Daniel Craig',        'PHARMACIST', 1),
    ('pharmacist2', '9feacbadee4eebd665ab123c06af28ed19c694a249f95d4a7bd4a2ff021840c3',  'Hannah May',          'PHARMACIST', 1);

-- fixed columns and types differences, kept her original products for testing variety
INSERT INTO products (sa_product_id, name, cost_price, markup_rate, vat_rate, category, is_active) VALUES
    ('T-0100', 'Paracetamol 500mg Tablets',  1.20, 0.2500, 0.2000, 'Pain Relief',     TRUE),
    ('T-0101', 'Ibuprofen 200mg Tablets',    1.80, 0.3000, 0.2000, 'Pain Relief',     TRUE),
    ('T-0102', 'Aspirin 300mg Tablets',      1.50, 0.2800, 0.2000, 'Pain Relief',     TRUE),
    ('T-0103', 'Cough Syrup 150ml',          2.75, 0.3500, 0.2000, 'Cold and Flu',    TRUE),
    ('T-0104', 'Vitamin C 1000mg Tablets',   3.20, 0.4000, 0.2000, 'Vitamins',        TRUE),
    ('T-0105', 'Multivitamin Capsules',      4.10, 0.3800, 0.2000, 'Vitamins',        TRUE),
    ('T-0106', 'Antiseptic Cream 30g',       2.10, 0.3200, 0.2000, 'First Aid',       TRUE),
    ('T-0107', 'Elastic Bandage',            1.95, 0.2700, 0.2000, 'First Aid',       TRUE),
    ('T-0108', 'Digital Thermometer',        6.50, 0.2200, 0.2000, 'Medical Devices', TRUE),
    ('T-0109', 'Hand Sanitiser 250ml',       2.40, 0.3300, 0.2000, 'Hygiene',         TRUE),
    ('T-0110', 'Allergy Relief Tablets',     3.60, 0.2900, 0.2000, 'Allergy',         TRUE),
    ('T-0111', 'Eye Drops 10ml',             2.90, 0.3100, 0.2000, 'Eye Care',        TRUE),
    ('T-1013', 'Nasal Spray 20ml',           3.10, 0.3000, 0.2000, 'Cold and Flu',    TRUE),
    ('T-1014', 'Calcium Supplements',        4.50, 0.3600, 0.2000, 'Supplements',     TRUE),
    ('T-1015', 'Face Masks Pack of 10',      2.00, 0.2600, 0.2000, 'Hygiene',         TRUE);

-- removed stock_id/last_updated
INSERT INTO stock (product_id, quantity, reorder_level) VALUES
    (1,  120, 20), (2,  95,  15), (3,  80,  15), (4,  40,  10),
    (5,  65,  12), (6,  55,  10), (7,  35,  8),  (8,  50,  10),
    (9,  18,  5),  (10, 70,  15), (11, 45,  10), (12, 30,  8),
    (13, 28,  8),  (14, 22,  6),  (15, 100, 20);

-- fixed more columns
-- NORMAL, SUSPENDED, IN_DEFAULT states variety for testing
INSERT INTO customers (name, email, phone, address, account_status,
    credit_limit, outstanding_balance, discount_type, fixed_discount_rate,
    flexible_tier_id, debt_period_start, created_by, merchant_id) VALUES
    ('Alice Johnson',       'alice.johnson@example.com',    '07111111111',
     '12 King Street, Manchester, M1 2AB',
     'NORMAL',    200.00,  45.50,  'FIXED', 0.0500, NULL, '2026-03-01', 1, 1),
    ('Brian Smith',         'brian.smith@example.com',      '07222222222',
     '45 Oxford Road, Manchester, M13 9PL',
     'SUSPENDED', 150.00, 160.75,  'FLEXIBLE', NULL, NULL, '2026-01-15', 1, 1),
    ('Charlotte Brown',     'charlotte.brown@example.com',  '07333333333',
     '8 Market Lane, Salford, M5 4QT',
     'IN_DEFAULT', 100.00, 210.20, 'FIXED', 0.1000, NULL, '2025-12-01', 1, 1),
    ('David Wilson',        'david.wilson@example.com',     '07444444444',
     '77 Bridge Street, Bolton, BL1 1AA',
     'NORMAL',    300.00,   0.00,  'FLEXIBLE', NULL, NULL, NULL,         1, 1),
    ('Emma Taylor',         'emma.taylor@example.com',      '07555555555',
     '21 High Street, Stockport, SK1 3XE',
     'NORMAL',    250.00,  89.99,  'FIXED', 0.1500, NULL, '2026-03-05', 1, 1);

INSERT INTO discount_tiers (customer_id, tier_name, min_monthly_spend, discount_rate) VALUES
    (2, 'Bronze', 0.00,   0.0500),
    (2, 'Silver', 100.00, 0.1000),
    (2, 'Gold',   250.00, 0.1500);

-- update brian to point to his first tier
UPDATE customers SET flexible_tier_id = 1 WHERE customer_id = 2;
-- david also flexible
UPDATE customers SET flexible_tier_id = 1 WHERE customer_id = 4;

-- fixed column name, added is_walk_in flag, good mix of payment types
INSERT INTO sales (customer_id, sold_by, subtotal, discount_amount, vat_amount,
    total_amount, sale_date, payment_method, is_walk_in) VALUES
    (1,    3, 25.00, 1.25, 4.75, 28.50,  '2026-03-21 10:30:00', 'CASH',            FALSE),
    (2,    3, 42.00, 4.20, 7.56, 45.36,  '2026-03-21 11:00:00', 'DEBIT_CARD',      FALSE),
    (3,    2, 18.50, 0.00, 3.70, 22.20,  '2026-03-21 11:20:00', 'ON_CREDIT',       FALSE),
    (NULL, 3, 12.00, 0.00, 2.40, 14.40,  '2026-03-21 11:45:00', 'CASH',            TRUE),
    (4,    2, 60.00, 6.00, 10.80, 64.80, '2026-03-21 12:15:00', 'CREDIT_CARD',     FALSE),
    (5,    3, 30.00, 4.50, 5.10, 30.60,  '2026-03-21 12:40:00', 'ACCOUNT_PAYMENT', FALSE),
    (1,    2, 15.75, 0.79, 3.00, 17.96,  '2026-03-22 09:10:00', 'DEBIT_CARD',      FALSE),
    (NULL, 3,  8.99, 0.00, 1.80, 10.79,  '2026-03-22 09:35:00', 'CASH',            TRUE);

-- updated product names to match our test products
INSERT INTO sale_items (sale_id, product_id, product_name, quantity, unit_price, discount_rate, line_total) VALUES
    (1, 1,  'Paracetamol 500mg Tablets', 2,  3.50,  0.0500,  6.65),
    (1, 10, 'Hand Sanitiser 250ml',      3,  6.00,  0.0500, 17.10),
    (2, 2,  'Ibuprofen 200mg Tablets',   4,  5.00,  0.1000, 18.00),
    (2, 5,  'Vitamin C 1000mg Tablets',  2, 12.00,  0.1000, 21.60),
    (3, 4,  'Cough Syrup 150ml',         2,  9.25,  0.0000, 18.50),
    (4, 15, 'Face Masks Pack of 10',     2,  6.00,  0.0000, 12.00),
    (5, 9,  'Digital Thermometer',       3, 20.00,  0.1000, 54.00),
    (6, 11, 'Allergy Relief Tablets',    5,  6.00,  0.1500, 25.50),
    (7, 3,  'Aspirin 300mg Tablets',     3,  5.25,  0.0500, 14.96),
    (8, 7,  'Antiseptic Cream 30g',      1,  8.99,  0.0000,  8.99);

-- payments for the sales above
INSERT INTO payments (sale_id, customer_id, payment_type, amount, change_given) VALUES
    (1, 1,    'CASH',            28.50, 1.50),
    (4, NULL, 'CASH',            14.40, 0.60),
    (8, NULL, 'CASH',            10.79, 0.21);

INSERT INTO payments (sale_id, customer_id, payment_type, amount, card_type, card_first4, card_last4, card_expiry) VALUES
    (2, 2, 'DEBIT_CARD',   45.36, 'Visa',       '4532', '7890', '12/28'),
    (5, 4, 'CREDIT_CARD',  64.80, 'Mastercard', '5412', '3456', '06/27'),
    (7, 1, 'DEBIT_CARD',   17.96, 'Visa',       '4532', '7890', '12/28');

INSERT INTO payments (sale_id, customer_id, payment_type, amount) VALUES
    (3, 3, 'ON_CREDIT',        22.20),
    (6, 5, 'ACCOUNT_PAYMENT',  30.60);


-- orders (so order tests have data to work with)
INSERT INTO orders (sa_order_id, merchant_id, order_status, total_amount, ordered_at, delivered_at, ordered_by) VALUES
    ('ORD-TEST-001', 1, 'DELIVERED',  96.00, '2026-03-15 09:00:00', '2026-03-17 14:00:00', 1),
    ('ORD-TEST-002', 1, 'PROCESSING',  73.25, '2026-03-20 10:30:00', NULL,                  2);

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
    (1, 1, 50, 1.20),
    (1, 2, 20, 1.80),
    (2, 4, 15, 2.75),
    (2, 5, 10, 3.20);

INSERT INTO online_orders (merchant_id, member_id, order_reference, total_price, discount_applied, status, payment_method, transaction_id, delivery_address) VALUES
    (1, 'MEM-TEST-001', 'PU-TEST-001', 24.50, 2.50, 'CONFIRMED', 'ONLINE_CARD', 'TXN-TEST-001', '10 Test Close, Manchester M1 1AA'),
    (1, 'MEM-TEST-002', 'PU-TEST-002', 61.20, 0.80, 'PROCESSING', 'ONLINE_CARD', 'TXN-TEST-002', '11 Test Close, Manchester M1 1AB'),
    (1, NULL,           'PU-TEST-003', 15.75, 0.25, 'DELIVERED', 'ONLINE_CARD', 'TXN-TEST-003', '12 Test Close, Manchester M1 1AC');

INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 1, 1, 12.00 FROM online_orders WHERE order_reference = 'PU-TEST-001';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 4, 1, 15.00 FROM online_orders WHERE order_reference = 'PU-TEST-001';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 5, 2, 31.00 FROM online_orders WHERE order_reference = 'PU-TEST-002';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 15, 1, 16.00 FROM online_orders WHERE order_reference = 'PU-TEST-003';

-- templates (so settings panel has data)
INSERT INTO templates (template_type, content) VALUES
    ('FIRST',  'Dear {customer_name},\n\nYour account ({account_no}) has an outstanding balance of £{amount_owed}.\n\nPlease arrange payment.\n\nRegards,\nTest Pharmacy'),
    ('SECOND', 'Dear {customer_name},\n\nFINAL NOTICE: Account ({account_no}) remains unpaid. Balance: £{amount_owed}.\n\nRegards,\nTest Pharmacy');

-- merged edge-case layer (used to live in 05_edge_case_scenarios.sql)
-- keep this in 04 so one test-db rebuild gives the full coverage set

-- login and role edges
INSERT INTO users (username, password_hash, full_name, email, phone, role, is_active, merchant_id) VALUES
    ('edge_admin', '301186de8a02763fef5a28c9f5ff82d921c987ac8d08cc36e34a4fb686b72bdd', 'Edge Admin', '', '', 'ADMIN', TRUE, 1),
    ('edge_manager', 'c7223d47958d9f31b09eaf954d74ea45b8eaf88d63122d72a1732d9fea43176a', 'Edge Manager', '', '', 'MANAGER', TRUE, 1),
    ('edge_pharmacist', 'b7abdac3ac749dcd392169eceb54e084ba49ab6e7bf45532608447d3ad44f8f4', 'Edge Pharmacist', '', '', 'PHARMACIST', TRUE, 1),
    ('edge_inactive', '70f6aaec84ed3db3f58d78bfad878bc67b04c10a6aa6c6d9a6c52621dd8fb2cd', 'Edge Inactive', '', '', 'PHARMACIST', FALSE, 1);

-- inactive product edge for active-only queries
INSERT INTO products (sa_product_id, name, cost_price, markup_rate, vat_rate, category, is_active) VALUES
    ('T-1099', 'Legacy Test Product', 1.10, 0.2000, 0.2000, 'Legacy', FALSE);

INSERT INTO stock (product_id, quantity, reorder_level)
SELECT product_id, 7, 5 FROM products WHERE sa_product_id = 'T-1099';

-- stock edges
UPDATE stock SET quantity = 5, reorder_level = 20 WHERE product_id = 1;
UPDATE stock SET quantity = 0, reorder_level = 10 WHERE product_id = 4;
UPDATE stock SET quantity = 15, reorder_level = 15 WHERE product_id = 2;
UPDATE stock SET quantity = 50, reorder_level = 5 WHERE product_id = 9;

-- customer and debt edges
INSERT INTO customers (
    account_no, name, contact_name, email, phone, address, account_status,
    credit_limit, outstanding_balance, discount_type, fixed_discount_rate,
    flexible_tier_id, debt_period_start, last_payment_date, created_by, merchant_id
) VALUES
    ('EDGE0001', 'Cycle Start Customer', 'Cycle Start Customer', 'edge1@example.com', '07700000001', '1 Edge Road, Manchester M1 1AA', 'NORMAL', 150.00, 149.00, 'FIXED', 0.0200, NULL, '2026-03-09', '2026-03-10', 1, 1),
    ('EDGE0002', 'Partial Payment Customer', 'Partial Payment Customer', 'edge2@example.com', '07700000002', '2 Edge Road, Manchester M1 1AB', 'SUSPENDED', 300.00, 175.50, 'FLEXIBLE', NULL, NULL, '2026-02-15', '2026-03-01', 1, 1),
    ('EDGE0003', 'Recovered Customer', 'Recovered Customer', 'edge3@example.com', '07700000003', '3 Edge Road, Manchester M1 1AC', 'NORMAL', 250.00, 0.00, 'FIXED', 0.0500, NULL, NULL, '2026-04-01', 1, 1),
    ('EDGE0004', 'Default Edge Customer', 'Default Edge Customer', 'edge4@example.com', '07700000004', '4 Edge Road, Manchester M1 1AD', 'IN_DEFAULT', 100.00, 95.00, 'FIXED', 0.0000, NULL, '2026-02-01', '2026-02-05', 1, 1),
    ('EDGE0005', 'No Contact Customer', NULL, NULL, NULL, '5 Edge Road, Manchester M1 1AE', 'SUSPENDED', 80.00, 79.50, 'FIXED', 0.0000, NULL, '2026-02-20', '2026-03-05', 1, 1);

INSERT INTO discount_tiers (customer_id, tier_name, min_monthly_spend, discount_rate)
SELECT customer_id, 'Edge Bronze', 0.00, 0.0000 FROM customers WHERE account_no = 'EDGE0002';
INSERT INTO discount_tiers (customer_id, tier_name, min_monthly_spend, discount_rate)
SELECT customer_id, 'Edge Silver', 100.00, 0.0200 FROM customers WHERE account_no = 'EDGE0002';
INSERT INTO discount_tiers (customer_id, tier_name, min_monthly_spend, discount_rate)
SELECT customer_id, 'Edge Gold', 250.00, 0.0400 FROM customers WHERE account_no = 'EDGE0002';

UPDATE customers
SET flexible_tier_id = (
    SELECT MIN(tier_id) FROM discount_tiers WHERE customer_id = customers.customer_id
)
WHERE account_no = 'EDGE0002';

UPDATE customers
SET date_1st_reminder = '2026-04-03', status_1st_reminder = 'SENT'
WHERE account_no = 'EDGE0001';

UPDATE customers
SET date_1st_reminder = '2026-03-18', status_1st_reminder = 'SENT'
WHERE account_no = 'EDGE0002';

UPDATE customers
SET date_1st_reminder = '2026-03-22', status_1st_reminder = 'SENT',
    date_2nd_reminder = '2026-04-05', status_2nd_reminder = 'DUE'
WHERE account_no = 'EDGE0004';

UPDATE customers
SET date_1st_reminder = '2026-04-06', status_1st_reminder = 'DUE'
WHERE account_no = 'EDGE0005';

-- extra sales and payments
INSERT INTO sales (customer_id, sold_by, subtotal, discount_amount, vat_amount, total_amount, sale_date, payment_method, is_walk_in)
SELECT customer_id, 3, 9.99, 0.20, 0.00, 9.79, '2026-04-02 10:15:00', 'ON_CREDIT', FALSE
FROM customers WHERE account_no = 'EDGE0001';

INSERT INTO sale_items (sale_id, product_id, product_name, quantity, unit_price, discount_rate, line_total)
SELECT MAX(sale_id), 1, 'Paracetamol 500mg Tablets', 1, 9.99, 0.0200, 9.79 FROM sales;

INSERT INTO payments (sale_id, customer_id, payment_type, amount)
SELECT (SELECT MAX(sale_id) FROM sales), customer_id, 'ON_CREDIT', 9.79 FROM customers WHERE account_no = 'EDGE0001';

INSERT INTO sales (customer_id, sold_by, subtotal, discount_amount, vat_amount, total_amount, sale_date, payment_method, is_walk_in)
VALUES (NULL, 3, 0.99, 0.00, 0.00, 0.99, '2026-04-02 11:20:00', 'CREDIT_CARD', TRUE);

INSERT INTO sale_items (sale_id, product_id, product_name, quantity, unit_price, discount_rate, line_total)
SELECT MAX(sale_id), 15, 'Face Masks Pack of 10', 1, 0.99, 0.0000, 0.99 FROM sales;

INSERT INTO payments (sale_id, customer_id, payment_type, amount, card_type, card_first4, card_last4, card_expiry)
SELECT MAX(sale_id), NULL, 'CREDIT_CARD', 0.99, 'Visa', '0000', '1111', '12/26' FROM sales;

INSERT INTO payments (sale_id, customer_id, payment_type, amount)
SELECT NULL, customer_id, 'ACCOUNT_PAYMENT', 25.00 FROM customers WHERE account_no = 'EDGE0002';

UPDATE customers
SET outstanding_balance = 150.50, last_payment_date = '2026-04-02'
WHERE account_no = 'EDGE0002';

-- order status edges
INSERT INTO orders (sa_order_id, merchant_id, order_status, total_amount, ordered_at, delivered_at, ordered_by) VALUES
    ('SA-EDGE-001', 1, 'ACCEPTED', 25.00, '2026-04-01 09:00:00', NULL, 1),
    ('SA-EDGE-002', 1, 'PACKED', 41.25, '2026-04-01 10:00:00', NULL, 1),
    ('SA-EDGE-003', 1, 'DISPATCHED', 63.50, '2026-03-20 12:00:00', NULL, 2),
    ('SA-EDGE-004', 1, 'CANCELLED', 12.00, '2026-04-01 13:00:00', NULL, 2);

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT order_id, 1, 10, 2.50 FROM orders WHERE sa_order_id = 'SA-EDGE-001';
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT order_id, 4, 15, 2.75 FROM orders WHERE sa_order_id = 'SA-EDGE-002';
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT order_id, 5, 10, 6.35 FROM orders WHERE sa_order_id = 'SA-EDGE-003';
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT order_id, 15, 4, 3.00 FROM orders WHERE sa_order_id = 'SA-EDGE-004';

-- online orders from the PU path
INSERT INTO online_orders (merchant_id, member_id, order_reference, total_price, discount_applied, status, payment_method, transaction_id, delivery_address) VALUES
    (1, 'MEM-EDGE-0001', 'PU-EDGE-0001', 31.20, 1.80, 'RECEIVED',   'ONLINE_CARD', 'TXN-EDGE-0001', '1 Web Lane, Leeds LS1 1AA'),
    (1, 'MEM-EDGE-0002', 'PU-EDGE-0002', 54.10, 0.90, 'READY',      'ONLINE_CARD', 'TXN-EDGE-0002', '2 Web Lane, Leeds LS1 1AB'),
    (1, 'MEM-EDGE-0003', 'PU-EDGE-0003', 18.95, 1.05, 'DISPATCHED', 'ONLINE_CARD', 'TXN-EDGE-0003', '3 Web Lane, Leeds LS1 1AC'),
    (1, 'MEM-EDGE-0004', 'PU-EDGE-0004', 72.30, 2.70, 'DELIVERED',  'ONLINE_CARD', 'TXN-EDGE-0004', '4 Web Lane, Leeds LS1 1AD'),
    (1, 'MEM-EDGE-0005', 'PU-EDGE-0005', 42.90, 2.10, 'CANCELLED',  NULL,          NULL,            '5 Web Lane, Leeds LS1 1AE'),
    (1, NULL,            'PU-EDGE-0006', 11.40, 0.00, 'PROCESSING', 'ONLINE_CARD', 'TXN-EDGE-0006', '6 Web Lane, Leeds LS1 1AF');

INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 1, 2, 16.50 FROM online_orders WHERE order_reference = 'PU-EDGE-0001';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 5, 1, 55.00 FROM online_orders WHERE order_reference = 'PU-EDGE-0002';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 15, 2, 10.00 FROM online_orders WHERE order_reference = 'PU-EDGE-0003';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 9, 3, 25.00 FROM online_orders WHERE order_reference = 'PU-EDGE-0004';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 10, 5, 9.00 FROM online_orders WHERE order_reference = 'PU-EDGE-0005';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 7, 1, 11.40 FROM online_orders WHERE order_reference = 'PU-EDGE-0006';

-- statements and reminders for debt-cycle/doc testing
INSERT INTO statements (customer_id, period_start, period_end, opening_balance, total_purchases, total_payments, closing_balance, generated_by)
SELECT customer_id, '2026-03-01', '2026-03-31', 149.00, 9.79, 0.00, 158.79, 1
FROM customers WHERE account_no = 'EDGE0001';

INSERT INTO statements (customer_id, period_start, period_end, opening_balance, total_purchases, total_payments, closing_balance, generated_by)
SELECT customer_id, '2026-02-01', '2026-02-28', 175.50, 0.00, 25.00, 150.50, 1
FROM customers WHERE account_no = 'EDGE0002';

INSERT INTO reminders (customer_id, reminder_type, amount_owed, due_date, sent_at, content)
SELECT customer_id, 'FIRST', 149.00, '2026-04-10', '2026-04-03', 'first reminder for edge0001 balance.'
FROM customers WHERE account_no = 'EDGE0001';

INSERT INTO reminders (customer_id, reminder_type, amount_owed, due_date, sent_at, content)
SELECT customer_id, 'FIRST', 150.50, '2026-04-08', '2026-03-18', 'first reminder for edge0002 after partial payment.'
FROM customers WHERE account_no = 'EDGE0002';

INSERT INTO reminders (customer_id, reminder_type, amount_owed, due_date, sent_at, content)
SELECT customer_id, 'SECOND', 95.00, '2026-04-12', NULL, 'second reminder queued for default edge customer.'
FROM customers WHERE account_no = 'EDGE0004';

-- extra templates for doc/export flows
INSERT INTO templates (template_type, content, updated_by) VALUES
    ('EDGE_RECEIPT', 'Receipt for {customer_name}\nAddress: {address}\nItems:\n{items}\nTotal: £{total}', 1),
    ('EDGE_REMINDER', 'Dear {contact_name},\n\nYour balance is £{amount_owed}.\nPlease pay by {due_date}.', 1),
    ('EDGE_STATEMENT', 'Statement for {customer_name}\nPeriod: {period_start} - {period_end}\nClosing balance: £{closing_balance}', 1);
