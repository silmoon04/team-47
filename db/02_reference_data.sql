-- IPOS-CA reference data

-- run after 01_schema.sql

USE iposca_database;

-- vat was 20%, marking criteria says 0%. credit limit was 150, should be 500
-- kept the names where they made sense, added new ones
INSERT INTO system_config (config_key, config_value) VALUES
                                                         ('vat_rate', '0.00'),
                                                         ('default_markup_rate', '1.00'),
                                                         ('currency_symbol', '£'),
                                                         ('currency_code', 'GBP'),
                                                         ('suspension_day', '15'),
                                                         ('default_credit_limit', '500.00'),
                                                         ('debt_period_days', '30'),
                                                         ('reminder_1st_days', '15'),
                                                         ('reminder_2nd_days', '30');


-- data from marking criteria IPOS-CA section
INSERT INTO merchant_details (merchant_id, business_name, address, phone, email, sa_merchant_id, sa_username, sa_password) VALUES
    (1, 'Cosymed Ltd', '25, Bond Street, London WC1V 8LS', '0207 321 8001', 'info@cosymed.co.uk', 'ACC0002', 'cosymed', 'bondstreet');

-- fixed columns aligning to schema
-- kept the placeholder names ({customer_name}, {balance} etc) and fixed few typos
INSERT INTO templates (template_type, content, updated_by) VALUES
                                                               ('RECEIPT',
                                                                'Cosymed Ltd\n25, Bond Street, London WC1V 8LS\nTel: 0207 321 8001\n\n--- RECEIPT ---\nDate: {sale_date}\nServed by: {staff_name}\n\n{items}\n\nSubtotal: £{subtotal}\nDiscount: -£{discount}\nVAT (0%): £{vat}\nTOTAL: £{total}\n\nPayment: {payment_type}\n{payment_details}\n\nThank you for your custom!',
                                                                NULL),
                                                               ('REMINDER_1ST',
                                                                'Dear {customer_name},\n\nThis is a reminder that your account with Cosymed Ltd has an outstanding balance of £{amount_owed}.\n\nPlease arrange payment promptly to avoid account suspension.\n\nYours sincerely,\nCosymed Ltd\n25, Bond Street, London WC1V 8LS',
                                                                NULL),
                                                               ('REMINDER_2ND',
                                                                'Dear {customer_name},\n\nFINAL NOTICE: Your account balance of £{amount_owed} with Cosymed Ltd is overdue.\n\nYour account will be placed in default if payment is not received within 15 days.\n\nYours sincerely,\nCosymed Ltd\n25, Bond Street, London WC1V 8LS',
                                                                NULL),
                                                               ('STATEMENT',
                                                                'Cosymed Ltd\n25, Bond Street, London WC1V 8LS\n\nAccount Statement for {customer_name}\nPeriod: {period_start} to {period_end}\n\nOpening Balance: £{opening_balance}\nTotal Purchases: £{total_purchases}\nTotal Payments: £{total_payments}\nClosing Balance: £{closing_balance}\n\nPlease ensure payment by the 15th of next month.',
                                                                NULL);

-- fixed the columns with the correct schema ones
-- changed with exact 14 items from the marking criteria
-- retail price = cost * (1 + markup) = cost * 2
INSERT INTO products (sa_product_id, name, package_type, unit_type, units_per_pack, cost_price, markup_rate, vat_rate, category, is_active) VALUES
                                                                                                                                                ('100 00001', 'Paracetamol',           'Box',    'Caps', 20,  0.10, 1.0000, 0.0000, 'OTC Pain Relief', TRUE),
                                                                                                                                                ('100 00002', 'Aspirin',               'Box',    'Caps', 20,  0.50, 1.0000, 0.0000, 'OTC Pain Relief', TRUE),
                                                                                                                                                ('100 00003', 'Analgin',               'Box',    'Caps', 10,  1.20, 1.0000, 0.0000, 'OTC Pain Relief', TRUE),
                                                                                                                                                ('100 00004', 'Celebrex, caps 100 mg', 'Box',    'Caps', 10, 10.00, 1.0000, 0.0000, 'Prescription',    TRUE),
                                                                                                                                                ('100 00005', 'Celebrex, caps 200 mg', 'Box',    'Caps', 10, 18.50, 1.0000, 0.0000, 'Prescription',    TRUE),
                                                                                                                                                ('100 00006', 'Retin-A Tretin, 30 g',  'Box',    'Caps', 20, 25.00, 1.0000, 0.0000, 'Skin Care',       TRUE),
                                                                                                                                                ('100 00007', 'Lipitor TB, 20 mg',     'Box',    'Caps', 30, 15.50, 1.0000, 0.0000, 'Prescription',    TRUE),
                                                                                                                                                ('100 00008', 'Claritin CR, 60g',      'Box',    'Caps', 20, 19.50, 1.0000, 0.0000, 'Allergy',         TRUE),
                                                                                                                                                ('200 00004', 'Iodine tincture',       'Bottle', 'Ml', 100,   0.30, 1.0000, 0.0000, 'Antiseptic',      TRUE),
                                                                                                                                                ('200 00005', 'Rhynol',                'Bottle', 'Ml', 200,   2.50, 1.0000, 0.0000, 'Nasal',           TRUE),
                                                                                                                                                ('300 00001', 'Ospen',                 'Box',    'Caps', 20, 10.50, 1.0000, 0.0000, 'Antibiotic',      TRUE),
                                                                                                                                                ('300 00002', 'Amopen',                'Box',    'Caps', 30, 15.00, 1.0000, 0.0000, 'Antibiotic',      TRUE),
                                                                                                                                                ('400 00001', 'Vitamin C',             'Box',    'Caps', 30,  1.20, 1.0000, 0.0000, 'Supplements',     TRUE),
                                                                                                                                                ('400 00002', 'Vitamin B12',           'Box',    'Caps', 30,  1.30, 1.0000, 0.0000, 'Supplements',     TRUE);

-- replaced the quantities with marking criteria values
-- kept alex's compact format
-- lipitor (7) is AT reorder level, rhynol (10) is BELOW , good demo talking points
INSERT INTO stock (product_id, quantity, reorder_level) VALUES
                                                            (1,  121, 10),   -- Paracetamol
                                                            (2,  201, 15),   -- Aspirin
                                                            (3,   25, 10),   -- Analgin
                                                            (4,   43, 10),   -- Celebrex 100mg
                                                            (5,   35,  5),   -- Celebrex 200mg
                                                            (6,   28, 10),   -- Retin-A
                                                            (7,   10, 10),   -- Lipitor (AT reorder level)
                                                            (8,   21, 10),   -- Claritin
                                                            (9,   35, 10),   -- Iodine tincture
                                                            (10,  14, 15),   -- Rhynol (BELOW reorder level)
                                                            (11,  78, 10),   -- Ospen
                                                            (12,  90, 15),   -- Amopen
                                                            (13,  22, 15),   -- Vitamin C
                                                            (14,  43, 15);   -- Vitamin B12
