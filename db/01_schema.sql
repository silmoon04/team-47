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

-- TODO: merchant_details table (singleton, only 1 row)
--   merchant_id (PK, default 1)
--   business_name, address, phone, email
--   sa_merchant_id (for team A integration)

-- TODO: products table
--   product_id (PK)
--   sa_product_id (unique, maps to team A catalogue)
--   name, cost_price, markup_rate, vat_rate, category
--   is_active, last_synced

-- TODO: stock table
--   stock_id (PK)
--   product_id (FK -> products, unique)
--   quantity, reorder_level
--   last_updated

-- TODO: discount_tiers table
--   tier_id (PK)
--   tier_name, min_monthly_spend, discount_rate

-- TODO: customers table
--   customer_id (PK)
--   name, email, phone, address
--   account_status ENUM('NORMAL','SUSPENDED','IN_DEFAULT')
--   credit_limit, outstanding_balance
--   discount_type ENUM('FIXED','FLEXIBLE')
--   fixed_discount_rate, flexible_tier_id (FK -> discount_tiers)
--   debt tracking fields: debt_period_start, reminder dates and statuses
--   created_at, created_by (FK -> users)

-- TODO: sales table
--   sale_id (PK)
--   customer_id (FK, nullable for walk-in)
--   sold_by (FK -> users)
--   subtotal, discount_amount, vat_amount, total_amount
--   sale_date, is_walk_in

-- TODO: sale_items table
--   sale_item_id (PK)
--   sale_id (FK -> sales)
--   product_id (FK -> products)
--   product_name, quantity, unit_price, discount_rate, line_total

-- TODO: payments table
--   payment_id (PK)
--   sale_id (FK, nullable)
--   customer_id (FK, nullable)
--   payment_type ENUM('CASH','DEBIT_CARD','CREDIT_CARD','ON_CREDIT','ACCOUNT_PAYMENT')
--   amount, card_type, card_first4, card_last4, card_expiry
--   change_given, payment_date

-- TODO: orders table
--   order_id (PK)
--   sa_order_id, merchant_id
--   order_status ENUM('ACCEPTED','PROCESSING','PACKED','IN_TRANSIT','DELIVERED','CANCELLED')
--   total_amount, ordered_at, delivered_at, ordered_by (FK -> users)

-- TODO: order_items table
--   order_item_id (PK)
--   order_id (FK -> orders)
--   product_id (FK -> products)
--   quantity, unit_price

-- TODO: statements table
--   statement_id (PK)
--   customer_id (FK), period_start, period_end
--   opening_balance, total_purchases, total_payments, closing_balance
--   generated_at, generated_by (FK -> users)

-- TODO: reminders table
--   reminder_id (PK)
--   customer_id (FK), reminder_type, amount_owed
--   due_date, sent_at, content

-- TODO: templates table
--   template_id (PK)
--   template_type (STATEMENT, REMINDER_1ST, REMINDER_2ND, RECEIPT)
--   content (with {placeholders}), updated_at, updated_by

-- TODO: system_config table
--   config_key (PK), config_value, updated_at

-- TODO: add indexes on frequently queried columns
-- TODO: add foreign key constraints