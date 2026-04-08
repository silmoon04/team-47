-- IPOS-CA Database Schema
-- mysql 8.0+

-- if needs to reset, just rerun the files 01, 02, 03 in order

DROP DATABASE IF EXISTS iposca_database;
CREATE DATABASE IF NOT EXISTS iposca_database
    CHARACTER SET utf8mb4 -- apparently this has full unicode support, and is good practice
    COLLATE utf8mb4_unicode_ci; -- this does case-insensitive comparisons which may be useful

-- added this, important
USE iposca_database;


-- stores app settings (vat rate, currency etc) changeable from settings panel
CREATE TABLE system_config (
                               config_key   VARCHAR(50) PRIMARY KEY,
                               config_value VARCHAR(255) NOT NULL,
                               updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- mostly same as original
-- email/phone now nullable (demo users dont all have them)
-- merchant_id gets DEFAULT 1 so inserts dont need to specify it
CREATE TABLE users (
                       user_id       INT NOT NULL AUTO_INCREMENT,
                       username      VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name     VARCHAR(255) NOT NULL,
                       email         VARCHAR(255) NULL DEFAULT '',
                       phone         VARCHAR(30) NULL DEFAULT '',
                       role          ENUM('PHARMACIST','ADMIN','MANAGER') NOT NULL,
                       is_active     BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       merchant_id   INT NOT NULL DEFAULT 1,
                       PRIMARY KEY (user_id)
);

-- DEFAULT 1 + AUTO_INCREMENT together crashes mysql 8 idk why
-- it's the bug that stops the entire schema from running
-- removed AUTO_INCREMENT, added CHECK so only 1 row ever exists
-- sa_merchant_id was INT, now VARCHAR for ids like 'ACC0002'
-- added sa_username/sa_password for SA login (cosymed/bondstreet)
CREATE TABLE merchant_details (
                                  merchant_id    INT NOT NULL DEFAULT 1,
                                  business_name  VARCHAR(255) NOT NULL,
                                  address        VARCHAR(255) NOT NULL,
                                  phone          VARCHAR(255) NOT NULL,
                                  email          VARCHAR(255) NOT NULL,
                                  sa_merchant_id VARCHAR(50) NOT NULL UNIQUE,
                                  sa_username    VARCHAR(50) NULL,
                                  sa_password    VARCHAR(255) NULL,
                                  PRIMARY KEY (merchant_id),
                                  CONSTRAINT chk_singleton CHECK (merchant_id = 1)
);

-- sa_product_id was INT, now VARCHAR for ids like '100 00001'
-- markup/vat precision increased and added few cols from marking criteria
-- category/is_active/last_synced get defaults so inserts dont fail
CREATE TABLE products (
                          product_id     INT NOT NULL AUTO_INCREMENT,
                          sa_product_id  VARCHAR(20) NOT NULL UNIQUE,
                          name           VARCHAR(255) NOT NULL,
                          description    VARCHAR(255) NULL,
                          package_type   VARCHAR(50) NULL DEFAULT 'Box',
                          unit_type      VARCHAR(50) NULL DEFAULT 'Caps',
                          units_per_pack INT NULL DEFAULT 1,
                          cost_price     DECIMAL(10, 2) NOT NULL,
                          markup_rate    DECIMAL(10, 4) NOT NULL DEFAULT 1.0000,
                          vat_rate       DECIMAL(10, 4) NOT NULL DEFAULT 0.0000,
                          category       VARCHAR(255) NULL,
                          is_active      BOOLEAN NOT NULL DEFAULT TRUE,
                          last_synced    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (product_id)
);

-- reorder_level made int
-- added defaults on quantity/last_updated, CHECK to prevent negative stock
CREATE TABLE stock (
                       stock_id      INT NOT NULL AUTO_INCREMENT,
                       product_id    INT NOT NULL UNIQUE,
                       quantity      INT NOT NULL DEFAULT 0,
                       reorder_level INT NOT NULL DEFAULT 10,
                       last_updated  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (stock_id),
                       CONSTRAINT chk_qty_non_negative CHECK (quantity >= 0)
);

-- mostly same as original
-- added customer_id so tiers link to specific customer,
-- discount_rate precision increase, added CHECK on rate range
CREATE TABLE discount_tiers (
                                tier_id           INT NOT NULL AUTO_INCREMENT,
                                customer_id       INT NULL,
                                tier_name         VARCHAR(255) NOT NULL,
                                min_monthly_spend DECIMAL(10, 2) NOT NULL,
                                discount_rate     DECIMAL(10, 4) NOT NULL,
                                PRIMARY KEY (tier_id),
                                CONSTRAINT chk_rate_range CHECK (discount_rate >= 0 AND discount_rate <= 1)
);
        -- direct-DB online orders written by Team C / PU for the demo path
        CREATE TABLE online_orders (
                                       online_order_id  INT NOT NULL AUTO_INCREMENT,
                                       merchant_id      INT NOT NULL DEFAULT 1,
                                       pu_order_ref     VARCHAR(50) NOT NULL UNIQUE,
                                       customer_name    VARCHAR(255) NOT NULL,
                                       customer_email   VARCHAR(255) NULL,
                                       customer_phone   VARCHAR(30) NULL,
                                       delivery_address VARCHAR(255) NOT NULL,
                                       status           ENUM('RECEIVED','PROCESSING','READY','DISPATCHED','DELIVERED','CANCELLED') NOT NULL DEFAULT 'RECEIVED',
                                       total_amount     DECIMAL(10, 2) NOT NULL,
                                       created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       PRIMARY KEY (online_order_id)
        );


-- lots of fixes here:
-- merchant_id had UNIQUE (only 1 customer per merchant??) removed
-- flexible_tier_id was NOT NULL, FIXED customers dont have a tier

        ALTER TABLE online_orders ADD CONSTRAINT fk_online_orders_merchant
            FOREIGN KEY (merchant_id) REFERENCES merchant_details (merchant_id);
-- fixed_discount_rate was NOT NULL, FLEXIBLE customers dont have one
-- debt_period_start was NOT NULL, new customers have no debt
-- reminder_dates (single DATE) split into date_1st/2nd_reminder
-- statuses (varchar csv) split into status_1st/2nd_reminder ENUMs
-- email was NOT NULL, not all customers have email
-- created_by was NOT NULL, admin-created at setup has no user
-- added account_no, contact_name, last_payment_date for marking criteria data
CREATE TABLE customers (
                           customer_id         INT NOT NULL AUTO_INCREMENT,
                           account_no          VARCHAR(20) NULL,
                           name                VARCHAR(255) NOT NULL,
                           contact_name        VARCHAR(255) NULL,
                           email               VARCHAR(255) NULL,
                           phone               VARCHAR(255) NULL,
                           address             VARCHAR(255) NOT NULL,
                           account_status      ENUM('NORMAL','SUSPENDED','IN_DEFAULT') NOT NULL DEFAULT 'NORMAL',
                           credit_limit        DECIMAL(10, 2) NOT NULL DEFAULT 500.00,
                           outstanding_balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                           discount_type       ENUM('FIXED','FLEXIBLE') NOT NULL DEFAULT 'FIXED',
                           fixed_discount_rate DECIMAL(10, 4) NULL DEFAULT 0.0000,
                           flexible_tier_id    INT NULL,
                           debt_period_start   DATE NULL,
                           last_payment_date   DATE NULL,
                           date_1st_reminder   DATE NULL,
                           status_1st_reminder ENUM('NOT_DUE','DUE','SENT') DEFAULT 'NOT_DUE',
                           date_2nd_reminder   DATE NULL,
                           status_2nd_reminder ENUM('NOT_DUE','DUE','SENT') DEFAULT 'NOT_DUE',
                           created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           created_by          INT NULL,
                           merchant_id         INT NOT NULL DEFAULT 1,
                           PRIMARY KEY (customer_id)
);

-- payment_method was VARCHAR(4), 'DEBIT_CARD' is 10 chars
-- sale_date was DATE, now DATETIME bc time precision
-- added is_walk_in flag, defaults on discount/vat amounts
CREATE TABLE sales (
                       sale_id         INT NOT NULL AUTO_INCREMENT,
                       customer_id     INT NULL,
                       sold_by         INT NOT NULL,
                       subtotal        DECIMAL(10, 2) NOT NULL,
                       discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                       vat_amount      DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                       total_amount    DECIMAL(10, 2) NOT NULL,
                       sale_date       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       payment_method  VARCHAR(20) NULL,
                       is_walk_in      BOOLEAN NOT NULL DEFAULT TRUE,
                       PRIMARY KEY (sale_id)
);

-- same structure as original, just better precision on discount_rate
-- added CHECK on quantity
CREATE TABLE sale_items (
                            sale_item_id  INT NOT NULL AUTO_INCREMENT,
                            sale_id       INT NOT NULL,
                            product_id    INT NOT NULL,
                            product_name  VARCHAR(255) NOT NULL,
                            quantity      INT NOT NULL,
                            unit_price    DECIMAL(10, 2) NOT NULL,
                            discount_rate DECIMAL(10, 4) NOT NULL DEFAULT 0.0000,
                            line_total    DECIMAL(10, 2) NOT NULL,
                            PRIMARY KEY (sale_item_id),
                            CONSTRAINT chk_sale_qty CHECK (quantity > 0)
);

-- payment_type and card_type were SWAPPED in original
-- payment_type had VARCHAR, card_type had the ENUM , backwards
-- card fields were INT NOT NULL , loses leading zeros, cash has no card
-- now CHAR/VARCHAR and nullable
-- change_given gets DEFAULT 0 (card payments have no change)
-- remove UNIQUE on sale_id (split payments possible)
CREATE TABLE payments (
                          payment_id   INT NOT NULL AUTO_INCREMENT,
                          sale_id      INT NULL,
                          customer_id  INT NULL,
                          payment_type ENUM('CASH','DEBIT_CARD','CREDIT_CARD','ON_CREDIT','ACCOUNT_PAYMENT') NOT NULL,
                          amount       DECIMAL(10, 2) NOT NULL,
                          card_type    VARCHAR(20) NULL,
                          card_first4  CHAR(4) NULL,
                          card_last4   CHAR(4) NULL,
                          card_expiry  VARCHAR(7) NULL,
                          change_given DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                          payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (payment_id)
);

-- delivered_at was NOT NULL, undelivered orders have no date
-- sa_order_id was INT, now VARCHAR for string ids
-- added DISPATCHED to enum (original had IN_TRANSIT instead), DEFAULT on status
CREATE TABLE orders (
                        order_id     INT NOT NULL AUTO_INCREMENT,
                        sa_order_id  VARCHAR(50) NULL,
                        merchant_id  INT NOT NULL DEFAULT 1,
                        order_status ENUM('ACCEPTED','PROCESSING','PACKED','DISPATCHED','DELIVERED','CANCELLED') NOT NULL DEFAULT 'ACCEPTED',
                        total_amount DECIMAL(10, 2) NOT NULL,
                        ordered_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        delivered_at DATETIME NULL,
                        ordered_by   INT NOT NULL,
                        PRIMARY KEY (order_id)
);

-- TODO: add an online_orders table (or extend orders cleanly) for the demo PU direct-DB path,
-- including delivery address and fulfilment status fields for online orders written by Team C.

-- renamed from ordered_items to match java model OrderItem
-- added CHECK on quantity
CREATE TABLE order_items (
                             order_item_id INT NOT NULL AUTO_INCREMENT,
                             order_id      INT NOT NULL,
                             product_id    INT NOT NULL,
                             quantity      INT NOT NULL,
                             unit_price    DECIMAL(10, 2) NOT NULL,
                             PRIMARY KEY (order_item_id),
                             CONSTRAINT chk_order_qty CHECK (quantity > 0)
);

-- period_start/end were DATETIME, should be DATE (date ranges not timestamps)
-- added defaults on totals and generated_at
CREATE TABLE statements (
                            statement_id    INT NOT NULL AUTO_INCREMENT,
                            customer_id     INT NOT NULL,
                            period_start    DATE NOT NULL,
                            period_end      DATE NOT NULL,
                            opening_balance DECIMAL(10, 2) NOT NULL,
                            total_purchases DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                            total_payments  DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                            closing_balance DECIMAL(10, 2) NOT NULL,
                            generated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            generated_by    INT NOT NULL,
                            PRIMARY KEY (statement_id)
);

-- sent_at was NOT NULL, unsent reminders have no date
-- reminder_type was VARCHAR, now ENUM
-- content was VARCHAR(255), reminders are long text
CREATE TABLE reminders (
                           reminder_id   INT NOT NULL AUTO_INCREMENT,
                           customer_id   INT NOT NULL,
                           reminder_type ENUM('FIRST','SECOND') NOT NULL,
                           amount_owed   DECIMAL(10, 2) NOT NULL,
                           due_date      DATE NULL,
                           sent_at       DATE NULL,
                           content       TEXT NULL,
                           PRIMARY KEY (reminder_id)
);

-- content was VARCHAR(255), templates are multi-line text
-- updated_by was NOT NULL, system templates have no user
CREATE TABLE templates (
                           template_id   INT NOT NULL AUTO_INCREMENT,
                           template_type VARCHAR(255) NOT NULL,
                           content       TEXT NOT NULL,
                           updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           updated_by    INT NULL,
                           PRIMARY KEY (template_id)
);

-- foreign keys as separate ALTER TABLEs bc tables reference each other
-- if inline, referenced table might not exist yet. standard practice
-- ON DELETE CASCADE basically deletes sale auto-deletes its items/payments (2 marks in demo)

ALTER TABLE users ADD CONSTRAINT fk_users_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchant_details (merchant_id);

ALTER TABLE stock ADD CONSTRAINT fk_stock_product
    FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE;

ALTER TABLE discount_tiers ADD CONSTRAINT fk_tiers_customer
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE CASCADE;

ALTER TABLE customers ADD CONSTRAINT fk_customers_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchant_details (merchant_id);
ALTER TABLE customers ADD CONSTRAINT fk_customers_created_by
    FOREIGN KEY (created_by) REFERENCES users (user_id);

ALTER TABLE sales ADD CONSTRAINT fk_sales_customer
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id);
ALTER TABLE sales ADD CONSTRAINT fk_sales_sold_by
    FOREIGN KEY (sold_by) REFERENCES users (user_id);

ALTER TABLE sale_items ADD CONSTRAINT fk_sale_items_sale
    FOREIGN KEY (sale_id) REFERENCES sales (sale_id) ON DELETE CASCADE;
ALTER TABLE sale_items ADD CONSTRAINT fk_sale_items_product
    FOREIGN KEY (product_id) REFERENCES products (product_id);

ALTER TABLE payments ADD CONSTRAINT fk_payments_sale
    FOREIGN KEY (sale_id) REFERENCES sales (sale_id) ON DELETE CASCADE;
ALTER TABLE payments ADD CONSTRAINT fk_payments_customer
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id);

ALTER TABLE orders ADD CONSTRAINT fk_orders_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchant_details (merchant_id);
ALTER TABLE orders ADD CONSTRAINT fk_orders_ordered_by
    FOREIGN KEY (ordered_by) REFERENCES users (user_id);

ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE;
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product
    FOREIGN KEY (product_id) REFERENCES products (product_id);

ALTER TABLE statements ADD CONSTRAINT fk_statements_customer
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id);
ALTER TABLE statements ADD CONSTRAINT fk_statements_generated_by
    FOREIGN KEY (generated_by) REFERENCES users (user_id);

ALTER TABLE reminders ADD CONSTRAINT fk_reminders_customer
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE CASCADE;

ALTER TABLE templates ADD CONSTRAINT fk_templates_updated_by
    FOREIGN KEY (updated_by) REFERENCES users (user_id);

-- indexes are sorted lookup structures so queries on these columns are fast (idk if it will matter ngl but i think its cool)
-- without them every query does full table scan
CREATE INDEX idx_customer_status ON customers(account_status);
CREATE INDEX idx_customer_balance ON customers(outstanding_balance);
CREATE INDEX idx_sale_date ON sales(sale_date);
CREATE INDEX idx_sale_customer ON sales(customer_id);
CREATE INDEX idx_order_status ON orders(order_status);
CREATE INDEX idx_payment_customer ON payments(customer_id);
CREATE INDEX idx_payment_date ON payments(payment_date);
CREATE INDEX idx_stock_product ON stock(product_id);
CREATE INDEX idx_product_sa_id ON products(sa_product_id);
CREATE INDEX idx_online_order_status ON online_orders(status);
