-- IPOS-CA Database Schema
-- MySQL 8.0+
-- run this first to create all tables

-- TODO: CREATE DATABASE IF NOT EXISTS ipos_ca

-- TODO: users table
--   user_id (PK, auto increment)
--   username (unique, not null)
--   password_hash (not null)
--   full_name
--   role ENUM('PHARMACIST','ADMIN','MANAGER')
--   is_active (boolean, default true)
--   created_at (timestamp)

CREATE TABLE users (
    user_id       int(10) NOT NULL,
    username      varchar(255) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    full_name     varchar(255) NOT NULL,
    role          varchar(255) NOT NULL,
    is_active     varchar(255) DEFAULT 'true',
    created_at    datetime NOT NULL,
    merchant_id   int(10) NOT NULL,
    PRIMARY KEY (user_id));

-- TODO: merchant_details table (singleton, only 1 row)
--   merchant_id (PK, default 1)
--   business_name, address, phone, email
--   sa_merchant_id (for team A integration)
CREATE TABLE merchant_details (
    merchant_id    int(10) DEFAULT 1 NOT NULL,
    business_name  varchar(255) NOT NULL,
    address        varchar(255) NOT NULL,
    phone          varchar(255) NOT NULL,
    email          varchar(255) NOT NULL,
    sa_merchant_id int(10) NOT NULL UNIQUE,
    PRIMARY KEY (merchant_id));

-- TODO: products table
--   product_id (PK)
--   sa_product_id (unique, maps to team A catalogue)
--   name, cost_price, markup_rate, vat_rate, category
--   is_active, last_synced

CREATE TABLE products (
    product_id    int(10) NOT NULL AUTO_INCREMENT,
    sa_product_id int(10) NOT NULL UNIQUE,
    name          varchar(255) NOT NULL,
    cost_price    decimal(10, 2) NOT NULL,
    markup_rate   decimal(10, 2) NOT NULL,
    vat_rate      decimal(10, 2) NOT NULL,
    category      varchar(255) NOT NULL,
    is_active     varchar(255) NOT NULL,
    last_synced   datetime NOT NULL,
    PRIMARY KEY (product_id));

-- TODO: stock table
--   stock_id (PK)
--   product_id (FK -> products, unique)
--   quantity, reorder_level
--   last_updated

CREATE TABLE stock (
    stock_id      int(10) NOT NULL AUTO_INCREMENT,
    product_id    int(10) NOT NULL UNIQUE,
    quantity      int(10),
    reorder_level varchar(255),
    last_updated  datetime NOT NULL,
    PRIMARY KEY (stock_id));

-- TODO: discount_tiers table
--   tier_id (PK)
--   tier_name, min_monthly_spend, discount_rate

CREATE TABLE discount_tiers (
    tier_id           int(10) NOT NULL AUTO_INCREMENT,
    tier_name         varchar(255) NOT NULL,
    min_monthly_spend decimal(10, 2) NOT NULL,
    discount_rate     decimal(10, 2) NOT NULL,
    PRIMARY KEY (tier_id));

-- TODO: customers table
--   customer_id (PK)
--   name, email, phone, address
--   account_status ENUM('NORMAL','SUSPENDED','IN_DEFAULT')
--   credit_limit, outstanding_balance
--   discount_type ENUM('FIXED','FLEXIBLE')
--   fixed_discount_rate, flexible_tier_id (FK -> discount_tiers)
--   debt tracking fields: debt_period_start, reminder dates and statuses
--   created_at, created_by (FK -> users)

CREATE TABLE customers (
    customer_id         int(10) NOT NULL AUTO_INCREMENT,
    name                varchar(255) NOT NULL,
    email               varchar(255) NOT NULL,
    phone               varchar(255),
    address             varchar(255) NOT NULL,
    account_status      varchar(255) NOT NULL,
    credit_limit        decimal(10, 2) NOT NULL,
    outstanding_balance decimal(10, 2) NOT NULL,
    discount_type       varchar(255) NOT NULL,
    fixed_discount_rate decimal(10, 2) NOT NULL,
    flexible_tier_id    int(10) NOT NULL,
    debt_period_start   date NOT NULL,
    reminder_dates      date NOT NULL,
    statuses            varchar(255) NOT NULL,
    created_at          datetime NOT NULL,
    created_by          int(10) NOT NULL,
    merchant_id         int(10) NOT NULL UNIQUE,
    PRIMARY KEY (customer_id));

-- TODO: sales table
--   sale_id (PK)
--   customer_id (FK, nullable for walk-in)
--   sold_by (FK -> users)
--   subtotal, discount_amount, vat_amount, total_amount
--   sale_date, is_walk_in

CREATE TABLE sales (
    sale_id         int(10) NOT NULL AUTO_INCREMENT,
    customer_id     int(10),
    sold_by         int(10) NOT NULL,
    subtotal        decimal(10, 2) NOT NULL,
    discount_amount decimal(10, 2) NOT NULL,
    vat_amount      decimal(10, 2) NOT NULL,
    total_amount    decimal(10, 2) NOT NULL,
    sale_date       date NOT NULL,
    payment_method  varchar(4) NOT NULL,
    PRIMARY KEY (sale_id));

-- TODO: sale_items table
--   sale_item_id (PK)
--   sale_id (FK -> sales)
--   product_id (FK -> products)
--   product_name, quantity, unit_price, discount_rate, line_total

CREATE TABLE sale_items (
    sale_item_id  int(10) NOT NULL AUTO_INCREMENT,
    sale_id       int(10) NOT NULL,
    product_id    int(10) NOT NULL,
    product_name  varchar(255) NOT NULL,
    quantity      int(10) NOT NULL,
    unit_price    decimal(10, 2) NOT NULL,
    discount_rate decimal(10, 2) NOT NULL,
    line_total    decimal(10, 2) NOT NULL,
    PRIMARY KEY (sale_item_id));

-- TODO: payments table
--   payment_id (PK)
--   sale_id (FK, nullable)
--   customer_id (FK, nullable)
--   payment_type ENUM('CASH','DEBIT_CARD','CREDIT_CARD','ON_CREDIT','ACCOUNT_PAYMENT')
--   amount, card_type, card_first4, card_last4, card_expiry
--   change_given, payment_date

CREATE TABLE payments (
    payment_id   int(10) NOT NULL AUTO_INCREMENT,
    sale_id      int(10) UNIQUE,
    customer_id  int(10),
    payment_type varchar(255) NOT NULL,
    amount       decimal(10, 2) NOT NULL,
    card_type    varchar(255) NOT NULL,
    card_first4  int(4) NOT NULL,
    card_last4   int(4) NOT NULL,
    card_expiry  int(6) NOT NULL,
    change_given decimal(10, 2) NOT NULL,
    payment_date datetime NOT NULL,
    PRIMARY KEY (payment_id));

-- TODO: orders table
--   order_id (PK)
--   sa_order_id, merchant_id
--   order_status ENUM('ACCEPTED','PROCESSING','PACKED','IN_TRANSIT','DELIVERED','CANCELLED')
--   total_amount, ordered_at, delivered_at, ordered_by (FK -> users)

CREATE TABLE orders (
    order_id     int(10) NOT NULL AUTO_INCREMENT,
    sa_order_id  int(10) NOT NULL,
    merchant_id  int(10) NOT NULL,
    order_status varchar(255) NOT NULL,
    total_amount decimal(10, 2) NOT NULL,
    ordered_at   datetime NOT NULL,
    delivered_at datetime NOT NULL,
    ordered_by   int(10) NOT NULL,
    PRIMARY KEY (order_id));

-- TODO: order_items table
--   order_item_id (PK)
--   order_id (FK -> orders)
--   product_id (FK -> products)
--   quantity, unit_price

CREATE TABLE ordered_items (
    order_item_id int(10) NOT NULL AUTO_INCREMENT,
    order_id      int(10) NOT NULL,
    product_id    int(10) NOT NULL,
    quantity      int(10) NOT NULL,
    unit_price    decimal(10, 2) NOT NULL,
    PRIMARY KEY (order_item_id));

-- TODO: statements table
--   statement_id (PK)
--   customer_id (FK), period_start, period_end
--   opening_balance, total_purchases, total_payments, closing_balance
--   generated_at, generated_by (FK -> users)

CREATE TABLE statements (
    statement_id    int(10) NOT NULL AUTO_INCREMENT,
    customer_id     int(10) NOT NULL,
    period_start    datetime NOT NULL,
    period_end      datetime NOT NULL,
    opening_balance decimal(10, 2) NOT NULL,
    total_purchases decimal(10, 2) NOT NULL,
    total_payments  decimal(10, 2) NOT NULL,
    closing_balance decimal(10, 2) NOT NULL,
    generated_at    datetime NOT NULL,
    generated_by    int(10) NOT NULL,
    PRIMARY KEY (statement_id));


-- TODO: reminders table
--   reminder_id (PK)
--   customer_id (FK), reminder_type, amount_owed
--   due_date, sent_at, content

CREATE TABLE reminders (
    reminder_id   int(10) NOT NULL AUTO_INCREMENT,
    customer_id   int(10) NOT NULL,
    reminder_type varchar(255) NOT NULL,
    amount_owed   decimal(10, 2) NOT NULL,
    due_date      date NOT NULL,
    sent_at       date NOT NULL,
    content       varchar(255) NOT NULL,
    PRIMARY KEY (reminder_id));

-- TODO: templates table
--   template_id (PK)
--   template_type (STATEMENT, REMINDER_1ST, REMINDER_2ND, RECEIPT)
--   content (with {placeholders}), updated_at, updated_by

CREATE TABLE templates (
    template_id   int(10) NOT NULL AUTO_INCREMENT,
    template_type varchar(255) NOT NULL,
    content       varchar(255) NOT NULL,
    updated_at    datetime NOT NULL,
    updated_by    int(10) NOT NULL,
    PRIMARY KEY (template_id));

-- TODO: system_config table
--   config_key (PK), config_value, updated_at

CREATE TABLE system_config (
    config_key   int(10) NOT NULL AUTO_INCREMENT,
    config_value decimal(10, 2) NOT NULL,
    updated_at   datetime NOT NULL,
    PRIMARY KEY (config_key));

-- TODO: add indexes on frequently queried columns
-- TODO: add foreign key constraints

ALTER TABLE users ADD CONSTRAINT FKusers138177 FOREIGN KEY (merchant_id) REFERENCES merchant_details (merchant_id);
ALTER TABLE customers ADD CONSTRAINT FKcustomers848476 FOREIGN KEY (merchant_id) REFERENCES merchant_details (merchant_id);
ALTER TABLE sales ADD CONSTRAINT FKsales360902 FOREIGN KEY (customer_id) REFERENCES customers (customer_id);
ALTER TABLE stock ADD CONSTRAINT FKstock942274 FOREIGN KEY (product_id) REFERENCES products (product_id);
ALTER TABLE customers ADD CONSTRAINT FKcustomers938353 FOREIGN KEY (flexible_tier_id) REFERENCES discount_tiers (tier_id);
ALTER TABLE customers ADD CONSTRAINT FKcustomers280715 FOREIGN KEY (created_by) REFERENCES users (user_id);
ALTER TABLE sale_items ADD CONSTRAINT FKsale_items563122 FOREIGN KEY (sale_id) REFERENCES sales (sale_id);
ALTER TABLE sale_items ADD CONSTRAINT FKsale_items543035 FOREIGN KEY (product_id) REFERENCES products (product_id);
ALTER TABLE payments ADD CONSTRAINT FKpayments77051 FOREIGN KEY (sale_id) REFERENCES sales (sale_id);
ALTER TABLE payments ADD CONSTRAINT FKpayments878891 FOREIGN KEY (customer_id) REFERENCES customers (customer_id);
ALTER TABLE orders ADD CONSTRAINT FKorders211906 FOREIGN KEY (merchant_id) REFERENCES merchant_details (merchant_id);
ALTER TABLE orders ADD CONSTRAINT FKorders860725 FOREIGN KEY (ordered_by) REFERENCES users (user_id);
ALTER TABLE ordered_items ADD CONSTRAINT FKordered_it57231 FOREIGN KEY (order_id) REFERENCES orders (order_id);
ALTER TABLE ordered_items ADD CONSTRAINT FKordered_it474910 FOREIGN KEY (product_id) REFERENCES products (product_id);
ALTER TABLE statements ADD CONSTRAINT FKstatements320788 FOREIGN KEY (customer_id) REFERENCES customers (customer_id);
ALTER TABLE statements ADD CONSTRAINT FKstatements317630 FOREIGN KEY (generated_by) REFERENCES users (user_id);
ALTER TABLE reminders ADD CONSTRAINT FKreminders374063 FOREIGN KEY (customer_id) REFERENCES customers (customer_id);
ALTER TABLE templates ADD CONSTRAINT FKtemplates710557 FOREIGN KEY (updated_by) REFERENCES users (user_id);
ALTER TABLE sales ADD CONSTRAINT FKsales964063 FOREIGN KEY (sold_by) REFERENCES users (user_id);
