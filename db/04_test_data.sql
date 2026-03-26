-- extra test data (alex's original data, fixed to match schema)

-- for dev/testing only (not demo day)
-- run INSTEAD of 02 + 03, directly after 01_schema.sql
-- has variety of customer states, payment types, walk-in sales etc,
-- data was done by alex and readapted to match the new fixed schema

USE iposca_database;

-- need merchant details first (users FK depends on it)
INSERT INTO merchant_details (merchant_id, business_name, address, phone, email, sa_merchant_id) VALUES
    (1, 'Test Pharmacy', '14 Green Lane, London EC1V 0HB', '0207 000 0001', 'info@testpharmacy.local', 'TEST001');

-- basic config
INSERT INTO system_config (config_key, config_value) VALUES
    ('vat_rate', '20.00'),
    ('currency_code', 'GBP'),
    ('debt_period_days', '30'),
    ('default_credit_limit', '150.00');

-- fixed columns, added random hash
INSERT INTO users (username, password_hash, full_name, role, merchant_id) VALUES
    ('admin',       'ipos_sys',   'System Administrator', 'ADMIN',      1),
    ('manager1',    'ipos_mg',  'Shakira Patel',       'MANAGER',    1),
    ('pharmacist1', 'ipos_pharma',  'Daniel Craig',        'PHARMACIST', 1),
    ('pharmacist2', 'ipos_pharma',  'Hannah May',          'PHARMACIST', 1);

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
