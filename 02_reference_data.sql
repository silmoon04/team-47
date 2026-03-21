USE ipos_ca;

-- Merchant details

INSERT INTO merchant_details(
    pharmacy_name,
    logo_path,
    address_line_1,
    address_line_2,
    city,
    county,
    postcode,
    country,
    email,
    phone,
    vat_rate,
    currency_code,
    receipt_footer,
    updated_by_user_id
) 
VALUES(
    'InfoPharma Pharmacy',
    NULL,
    '14 Green Lane',
    NULL,
    'London',
    'Greater London',
    'EC1V 0HB',
    'United Kingdom',
    'info@infopharma.local',
    '016100000'
    20.00,
    'GBP',
    'Thank you for shopping with InfoPharma.',
    (SELECT user_id FROM users WHERE username = 'admin')
);

-- Templates
-- for recipts/ statements and reminders
-- template_code is unique and is good for having a stable key and database lookup

INSERT INTO templates(
    template_code,
    template_type,
    template_name,
    subject_line,
    body_text,
    is_active,
    version_no,
    updated_by_user_id
)
VALUES (
    'RECEIPT_DEFAULT_V1',
    'RECEIPT',
    'Default Receipt Template',
    NULL,
    'Receipt No: {sale_number}
    Date: {sales_datetime}
    Customer: {customer_name}
    Serverd By:{served_by}
    
    Items:
    {bought_items}
    
    Subtotal: {subtotal_amount}
    Discount: {discount_amount}
    VAT: {vat_amount}
    Total: {total_amount}
    Paid: {amount_paid}',
    TRUE,
    1,
    (SELECT user_id FROM users WHERE username = 'admin')
),
(
    'STATEMENT_DEFAULT-V1',
    'STATEMENT',
    'Default Monthly Statement Template',
    'Your Monthly Statement',
    'Dear {customer_name},

    Please find your account statement bellow:
    
    Statement Number: {statement_number}
    Period: {statement_period_start} to {statement_period_end}
    Opening Balance: {opening_balance}
    New debt: {new_debt_amount}
    Payments received: {payments_amount}
    Closing balance: {closing_balance}
    Payment Due Date: {due_date}

    Regards,
    {merchant_name}'
    TRUE,
    1,
    (SELECT user_id FROM users WHERE username = 'admin')
),
(
    'REMINDER_1_DEFAULT_V1',
    'REMINDER_1',
    'Default First Reminder Template',
    'Payment Reminder',
    'Dear {customer_name},
    
    Our records show that your account balance of {balance} remains unpaid.
    This is your first reminder. 
    Please make payment by {payment_due_date}.
    
    Regards,
    {merchant_name}.',
    TRUE,
    1,
    (SELECT user_id FROM users WHERE username = 'admin')
),
(
    'REMINDER_2_DEFAULT_V1',
    'REMINDER_2',
    'Default Second Reminder Template',
    'Urgent Payment Reminder',
    'Dear {customer_name},
    
    Our records show that your account balance of {balance} remains unapaid.
    This is your second reminder.
    Please make payment immediatley by {payment_date} to avoid default proceedings.
    
    Regards,
    {merchant_name}.',
    TRUE,
    1,
    (SELECT user_id FROM users WHERE username = 'admin')
);

INSERT INTO system_config (config_key, config_value) 
VALUES
('vat_rate', '20.00'),
('currency_code', 'GBP'),
('debt_period_days', '30'),
('first_reminder_after_days', '30'),
('second_reminder_after_days', '60'),
('suspension_after_days', '60'),
('default_after_days', '90'),
('default_credit_limit', '150.00'),
('default_reorder_level', '10');

INSERT INTO discount_tiers(
    discount_plan_code,
    discount_plan_name,
    discount_type,
    min_monthly_spend,
    max_monthly_spend,
    discount_rate,
    is_active
)
VALUES
('LOYALTY_BRONZE', 'Bronze Tier', 'FLEXIBLE', 0.00, 99.99,5.00, TRUE),
('LOYALTY_SILVER', 'Silver Tier', 'FLEXIBLE', 100,00, 250.00, 10.00, TRUE),
('LOYALTY_GOLD', 'Gold Tier', 'FLEXIBLE', 250.00, 500.00, 15.00, TRUE),
('LOYALTY_PLATINUM', 'Platinum', 'FLEXIBLE', 500.00, 750.00, 20.00, TRUE);