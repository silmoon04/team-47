-- IPOS-CA demo data , fixed version
-- run after 02_reference_data.sql

-- marking criteria says start with only sysdba login
-- products/stock/merchant already in reference data
-- historical orders (scenarios 2 & 4) pre-seeded here

USE iposca_database;
-- fixed a few things from the previous version
--  db name, user columns, missing commas, typos,
--  syntax errors on products 7/8/14
--  but what i kept is sale structure pattern (mix of payment types), sale_items format

-- only sysdba is actually pre-seeded, we need to create others live during demo
INSERT INTO users (username, password_hash, full_name, role, merchant_id) VALUES
    ('sysdba', '48c5a1d217fe85082464d2ca1e90a16d15464fabe20f8610d79b63aa58797b9b', 'System Administrator', 'ADMIN', 1);

-- pre-seed customers from marking criteria
-- Eva Bauyer: ACC0001, credit £500, Fixed 3%
-- Glynne Morrison: ACC0002, credit £500, Variable discount
INSERT INTO customers (account_no, name, contact_name, address, phone, credit_limit, discount_type, fixed_discount_rate, merchant_id) VALUES
    ('ACC0001', 'Ms Eva Bauyer',       'Ms Eva Bauyer',       '1, Liverpool street, London EC2V 8NS', '0207 321 8001', 500.00, 'FIXED',    0.0300, 1),
    ('ACC0002', 'Mr Glynne Morrison',  'Ms Glynne Morisson',  '1, Liverpool street, London EC2V 8NS', '0207 321 8001', 500.00, 'FLEXIBLE', NULL,   1);

-- Glynne Morrison's variable discount tiers (from marking criteria)
INSERT INTO discount_tiers (customer_id, tier_name, min_monthly_spend, discount_rate) VALUES
    (2, 'No Discount',  0.00,   0.0000),
    (2, 'Bronze',       100.00, 0.0100),
    (2, 'Silver',       300.00, 0.0200);

-- added historical orders
-- scenario 2: 25 feb, cosymed ordered from SA, £376, delivered 26 feb
INSERT INTO orders (sa_order_id, merchant_id, order_status, total_amount, ordered_at, delivered_at, ordered_by) VALUES
    ('SA-ORD-001', 1, 'DELIVERED', 376.00, '2026-02-25 10:00:00', '2026-02-26 17:00:00', 1);

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
                                                                         (1, 1,  10,  0.10),   -- Paracetamol x10 = £1.00
                                                                         (1, 3,  20,  1.20),   -- Analgin x20 = £24.00
                                                                         (1, 10, 10,  2.50),   -- Rhynol x10 = £25.00
                                                                         (1, 12, 20, 15.00),   -- Amopen x20 = £300.00
                                                                         (1, 14, 20,  1.30);   -- Vitamin B12 x20 = £26.00  total = £376

-- scenario 4: 10 mar, cosymed ordered from SA, £430, delivered 12 mar
INSERT INTO orders (sa_order_id, merchant_id, order_status, total_amount, ordered_at, delivered_at, ordered_by) VALUES
    ('SA-ORD-002', 1, 'DELIVERED', 430.00, '2026-03-10 09:00:00', '2026-03-12 11:00:00', 1);

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
                                                                         (2, 10, 10, 2.50),    -- Rhynol x10 = £25.00
                                                                         (2, 11, 10, 10.50),   -- Ospen x10 = £105.00
                                                                         (2, 12, 20, 15.00);   -- Amopen x20 = £300.00  total = £430

-- online orders from PU (scenarios 19 & 20)
-- scenario 19: Peter Popov placed order 20 Mar via PU — Aspirin 1, Retin-A 1 (March Promotion)
-- scenario 20: PU0001 placed order 8 Apr — Ospen 1 (10th order = 10% loyalty discount)
INSERT INTO online_orders (merchant_id, member_id, order_reference, total_price, discount_applied, status, payment_method, transaction_id, delivery_address, created_at) VALUES
    (1, 'peter.popov@example.com', 'ORD-20260320-001', 40.95, 5.05, 'RECEIVED', 'AmEx', 'TXN-PP-20260320', '42 Chislehurst Road, BR7 5NS', '2026-03-20 14:30:00'),
    (1, 'cool@example.com',        'ORD-20260408-001', 18.90, 2.10, 'RECEIVED', 'Mastercard', 'TXN-PU0001-20260408', '15 Bromley Road, London SE6 2TS', '2026-04-08 11:15:00');

-- Peter Popov order items: Aspirin (5% off) + Retin-A (20% off via March Promotion)
-- Aspirin retail £1.00, 5% off = £0.95; Retin-A retail £50.00, 20% off = £40.00; total = £40.95
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 2, 1, 0.95 FROM online_orders WHERE order_reference = 'ORD-20260320-001';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 6, 1, 40.00 FROM online_orders WHERE order_reference = 'ORD-20260320-001';

-- PU0001 order items: Ospen 1 box (retail £21.00, 10% loyalty = £18.90)
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 11, 1, 18.90 FROM online_orders WHERE order_reference = 'ORD-20260408-001';

-- rest of the scenarios im pretty sure that we need to do live, can double check later
