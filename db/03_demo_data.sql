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

INSERT INTO online_orders (merchant_id, member_id, order_reference, total_price, discount_applied, status, payment_method, transaction_id, delivery_address) VALUES
    (1, 'MEM-DEMO-0001', 'PU-WEB-2026-0001', 42.80, 4.20, 'CONFIRMED', 'ONLINE_CARD', 'TXN-DEMO-0001', '17 Beacon Road, London N1 4AB'),
    (1, NULL,            'PU-WEB-2026-0002', 18.40, 0.00, 'DISPATCHED', 'ONLINE_CARD', 'TXN-DEMO-0002', '88 Market Street, London E1 6NN');

INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 1, 4, 5.00 FROM online_orders WHERE order_reference = 'PU-WEB-2026-0001';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 4, 2, 13.50 FROM online_orders WHERE order_reference = 'PU-WEB-2026-0001';
INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price)
SELECT online_order_id, 10, 2, 9.20 FROM online_orders WHERE order_reference = 'PU-WEB-2026-0002';

-- rest of the scenarios im pretty sure that we need to do live, can double check later
