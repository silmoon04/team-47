USE ipos_ca;

-- Users (ref by role)
-- basline application
-- password hashes bellow are placeholders for development only
-- will need to replace or use app's password hashing flow

INSERT INTO users(
    username,
    password_hash,
    role,
    first_name,
    last_name,
    email,
    phone,
    is_active
) 
VALUES (
    'admin',
    'REPLACE_WITH_ADMIN_HASH',
    'ADMIN',
    'System',
    'Administrator'
    'admin@infopharma.local',
    '0207000001',
    TRUE
),
(
    'manager1',
    'REPLACE_WITH_MANAGER_HASH',
    'MANGER',
    'Shakira',
    'Patel',
    'manager@infopharma.local',
    '0207000002',
    TRUE
),
(
    'pharmacist1',
    'REPLACE_WITH_PHARMA1_HASH',
    'PHARMACIST',
    'Daniel',
    'Craig',
    'pharmacist1@infopharma.local',
    '0207000003',
    TRUE
),
(
    'pharmacit2',
    'REPLACE_WITH_PHARMA2_HASH'
    'PHARMACIST',
    'Hannah',
    'May',
    'pharmacist2@infopharma.local',
    '02070000004',
    TRUE
);

INSERT INTO products(
    product_id,
    sa_product_id,
    product_name,
    cost_price,
    markup_rate,
    vat_rate,
    category,
    is_active,
    last_synced
)
VALUES
(
    1, 0100,'Paracetamol 500mg Tablets', 
    1.20, 25.00, 20.00, 'Pain Relief', 
    TRUE, '2026-03-21 09:00:00'
),
(
    2, 0101,'Ibuprofen 200mg Tablets',
    1.80, 30.00, 20.00, 'Pain Relief',
    TRUE, '2026-03-21 09:00:00'
),
(
    3, 0102,'Aspirin 300mg Tablets',
    1.50, 28.00, 20.00, 'Pain Relief',
    TRUE,  '2026-03-21 09:00:00'
),
(
    4, 0103,'Cough Syrup 150ml',
    2.75, 35.00, 20.00, 'Cold and Flu',
    TRUE,  '2026-03-21 09:00:00'
),
(
    5, 0104,'Vitamin C 1000mg Tablets',
    3.20, 40.00, 20.00, 'Vitamins',
    TRUE,  '2026-03-21 09:00:00'
),
(
    6, 0105,'Multivitamin Capsules',
    4.10, 38.00, 20.00, 'Vitamins',
    TRUE,  '2026-03-21 09:00:00'),
(
    7, 0106,'Antiseptic Cream 30g',
    2.10, 32.00, 20.00, 'First Aid',
),
(
    8, 0107,'Elastic Bandage',1.95,
    27.00, 20.00, 'First Aid',
),
(
    9, 0108,'Digital Thermometer',
    6.50, 22.00, 20.00, 'Medical Devices',
    TRUE,  '2026-03-21 09:00:00'
),
(
    10, 0109,'Hand Sanitiser 250ml',
    2.40, 33.00, 20.00, 'Hygiene',
    TRUE,  '2026-03-21 09:00:00'
),
(
    11, 0110,'Allergy Relief Tablets',
    3.60, 29.00, 20.00, 'Allergy',
    TRUE,  '2026-03-21 09:00:00'
),
(
    12, 0111, 'Eye Drops 10ml',
    2.90, 31.00, 20.00, 'Eye Care',
    TRUE,  '2026-03-21 09:00:00'
),
(
    13, 1013, 'Nasal Spray 20ml',
    3.10, 30.00, 20.00, 'Cold and Flu',
    TRUE,  '2026-03-21 09:00:00'
),
(
    14, 1014, 'Calcium Supplements',
    4.50, 36.00, 20.00, 'Supplements',
),
(
    15, 1015, 'Face Masks Pack of 10',
    2.00, 26.00, 20.00, 'Hygiene',
    TRUE,  '2026-03-21 09:00:00'
);

INSERT INTO stock(
    stock_id,
    product_id,
    quantity,
    reorder_level,
    last_updated
)
VALUES
(1,  1, 120, 20, '2026-03-21 09:15:00'),
(2,  2, 95,  15, '2026-03-21 09:15:00'),
(3,  3, 80,  15, '2026-03-21 09:15:00'),
(4,  4, 40,  10, '2026-03-21 09:15:00'),
(5,  5, 65,  12, '2026-03-21 09:15:00'),
(6,  6, 55,  10, '2026-03-21 09:15:00'),
(7,  7, 35,  8,  '2026-03-21 09:15:00'),
(8,  8, 50,  10, '2026-03-21 09:15:00'),
(9,  9, 18,  5,  '2026-03-21 09:15:00'),
(10, 10, 70,  15, '2026-03-21 09:15:00'),
(11, 11, 45,  10, '2026-03-21 09:15:00'),
(12, 12, 30,  8,  '2026-03-21 09:15:00'),
(13, 13, 28,  8,  '2026-03-21 09:15:00'),
(14, 14, 22,  6,  '2026-03-21 09:15:00'),
(15, 15, 100, 20, '2026-03-21 09:15:00');

INSERT INTO customers(
    customer_id,
    name,
    email,
    phone,
    address,
    account_status,
    credit_limit,
    outstanding_balance,
    discount_type,
    fixed_discount_rate,
    flexible_tier_id,
    debt_period_start,
    reminder_dates,
    statuses,
    created_at,
    created_by,
    merchant_id
)
VALUES
(
    1,
    'Alice Johnson',
    'alice.johnson@example.com',
    '07111111111',
    '12 King Street, Manchester, M1 2AB',
    'NORMAL',
    200.00,
    45.50,
    'FIXED',
    5.00,
    NULL,
    '2026-03-01',
    NULL,
    'ACTIVE',
    '2026-03-21 10:00:00',
    1,
    1
),
(
    2,
    'Brian Smith',
    'brian.smith@example.com',
    '07222222222',
    '45 Oxford Road, Manchester, M13 9PL',
    'SUSPENDED',
    150.00,
    160.75,
    'FLEXIBLE',
    NULL,
    2,
    '2026-01-15',
    '2026-02-15,2026-03-01',
    'FIRST_REMINDER_SENT,SECOND_REMINDER_SENT',
    '2026-03-21 10:05:00',
    1,
    1
),
(
    3,
    'Charlotte Brown',
    'charlotte.brown@example.com',
    '07333333333',
    '8 Market Lane, Salford, M5 4QT',
    'IN_DEFAULT',
    100.00,
    210.20,
    'FIXED',
    10.00,
    NULL,
    '2025-12-01',
    '2026-01-01,2026-02-01',
    'FIRST_REMINDER_SENT,DEFAULTED',
    '2026-03-21 10:10:00',
    2,
    1
),
(
    4,
    'David Wilson',
    'david.wilson@example.com',
    '07444444444',
    '77 Bridge Street, Bolton, BL1 1AA',
    'NORMAL',
    300.00,
    0.00,
    'FLEXIBLE',
    NULL,
    1,
    NULL,
    NULL,
    'ACTIVE',
    '2026-03-21 10:15:00',
    2,
    1
),
(
    5,
    'Emma Taylor',
    'emma.taylor@example.com',
    '07555555555',
    '21 High Street, Stockport, SK1 3XE',
    'NORMAL',
    250.00,
    89.99,
    'FIXED',
    15.00,
    NULL,
    '2026-03-05',
    '2026-04-05',
    'PAYMENT_DUE',
    '2026-03-21 10:20:00',
    3,
    1
);

INSERT INTO sales(
    sale_id,
    customer_id,
    sold_by,
    subtotal,
    discount_amount,
    vat_amount,
    total_amount,
    sales_date,
    payment_method
)
VALUES
(
    1, 1, 3, 25.00, 1.25, 4.75, 28.50, 
    '2026-03-21 10:30:00', 'CASH'
),
(
    2, 2, 3, 42.00, 4.20, 7.56, 45.36, 
    '2026-03-21 11:00:00', 'DEBIT_CARD'
),
(
    3, 3, 2, 18.50, 0.00, 3.70, 22.20, 
    '2026-03-21 11:20:00', 'ON_CREDIT'
),
(
    4, NULL, 3, 12.00, 0.00, 2.40, 14.40, 
    '2026-03-21 11:45:00', 'CASH'
),
(
    5, 4, 2, 60.00, 6.00, 10.80, 64.80, 
    '2026-03-21 12:15:00', 'CREDIT_CARD'
),
(
    6, 5, 3, 30.00, 4.50, 5.10, 30.60, 
    '2026-03-21 12:40:00', 'ACCOUNT_PAYMENT'
),
(
    7, 1, 2, 15.75, 0.79, 3.00, 17.96, 
    '2026-03-22 09:10:00', 'DEBIT_CARD'
),
(
    8, NULL, 3, 8.99, 0.00, 1.80, 10.79, 
    '2026-03-22 09:35:00', 'CASH'
);

INSERT INTO sale_items(
    sale_item_id,
    sale_id,
    product_id,
    product_name,
    quantity,
    unit_price,
    discount_rate,
    line_total
)
VALUES
(1, 1, 1,  'Paracetamol 500mg Tablets', 2,  3.50,  5.00,  6.65),
(2, 1, 10, 'Hand Sanitiser 250ml',      3,  6.00,  5.00, 17.10),
(3, 2, 2,  'Ibuprofen 200mg Tablets',   4,  5.00, 10.00, 18.00),
(4, 2, 5,  'Vitamin C 1000mg Tablets',  2, 12.00, 10.00, 21.60),
(5, 3, 4,  'Cough Syrup 150ml',         2,  9.25,  0.00, 18.50),
(6, 4, 15, 'Face Masks Pack of 10',     2,  6.00,  0.00, 12.00),
(7, 5, 9,  'Digital Thermometer',       3, 20.00, 10.00, 54.00),
(8, 6, 11, 'Allergy Relief Tablets',    5,  6.00, 15.00, 25.50),
(9, 7, 3,  'Aspirin 300mg Tablets',     3,  5.25,  5.00, 14.96),
(10, 8, 7, 'Antiseptic Cream 30g',      1,  8.99,  0.00,  8.99);