DROP DATABASE IF EXISTS ipos_ca;
CREATE DATABASE ipos_ca

USE ipos_ca;
-- Users
-- Each ipos-ca user has one role out of these: Pharmacist/ manager/ admin.

CREATE TABLE users (
    user_id NOT NULL,
    username VARCHAR(50),
    password_hash VARCHAR(255),
    role ENUM('PHARMACIST', 'MANAGER', 'ADMIN'),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(30),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_t DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
)
