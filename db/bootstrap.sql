-- IPOS-CA Full DB Bootstrap
-- Run once after cloning:
--   cd IPOS-CA-47
--   mysql -u root -p < db/bootstrap.sql
-- Sets up CA + SA databases so the full system works out of the box.
-- Then: open in IntelliJ, build, and run Main.java.

-- ============================================================
-- 1. CA DATABASE
-- ============================================================
SOURCE db/01_schema.sql;
SOURCE db/02_reference_data.sql;
SOURCE db/03_demo_data.sql;

-- ============================================================
-- 2. SA DATABASE (Team 46 schema + demo seed data)
-- ============================================================
DROP DATABASE IF EXISTS ipos_sa_db;
CREATE DATABASE IF NOT EXISTS ipos_sa_db;
USE ipos_sa_db;

CREATE TABLE Users (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Role ENUM('Admin','Director of Operations','Merchant','Senior accountant','Accountant','Warehouse employee','Delivery department employee') NOT NULL,
    AccountNo VARCHAR(20) UNIQUE,
    AccountHolderName VARCHAR(100),
    ContactName VARCHAR(100),
    Address VARCHAR(255),
    Phone VARCHAR(20),
    CreditLimit DECIMAL(10,2),
    DiscountPlan ENUM('Fixed','Flexible','Variable'),
    DiscountRate VARCHAR(255),
    AccountStatus ENUM('Normal','Suspended','In_Default') DEFAULT 'Normal',
    OutstandingBalance DECIMAL(10,2) DEFAULT 0.00
);

CREATE TABLE Catalogue (
    ProductID VARCHAR(50) PRIMARY KEY,
    Name VARCHAR(255) NOT NULL,
    Description VARCHAR(255),
    Category VARCHAR(100),
    PackageType VARCHAR(50),
    Unit VARCHAR(50),
    UnitsInPack INT,
    PackageCost DECIMAL(10,2),
    Availability INT DEFAULT 0,
    StockLimit INT DEFAULT 0,
    IsActive BOOLEAN DEFAULT TRUE
);

CREATE TABLE Orders (
    OrderID VARCHAR(50) PRIMARY KEY,
    MerchantID INT,
    OrderDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    TotalAmount DECIMAL(10,2),
    OrderStatus ENUM('ACCEPTED','PROCESSING','READY_TO_DISPATCH','PACKED','DISPATCHED','DELIVERED','CANCELLED') DEFAULT 'ACCEPTED',
    EstimatedDelivery DATETIME,
    DispatchDetails VARCHAR(255),
    FOREIGN KEY (MerchantID) REFERENCES Users(UserID)
);

CREATE TABLE OrderItems (
    OrderItemID INT AUTO_INCREMENT PRIMARY KEY,
    OrderID VARCHAR(50),
    ProductID VARCHAR(50),
    Quantity INT,
    UnitCost DECIMAL(10,2),
    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    FOREIGN KEY (ProductID) REFERENCES Catalogue(ProductID)
);

CREATE TABLE Invoices_Payments (
    InvoiceID INT AUTO_INCREMENT PRIMARY KEY,
    OrderID VARCHAR(50),
    IssueDate DATE,
    DueDate DATE,
    PaymentStatus ENUM('Pending','Paid','Overdue') DEFAULT 'Pending',
    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID)
);

CREATE TABLE Payments (
    PaymentID INT AUTO_INCREMENT PRIMARY KEY,
    MerchantID INT,
    Amount DECIMAL(10,2),
    PaymentDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    PaymentMethod VARCHAR(50),
    FOREIGN KEY (MerchantID) REFERENCES Users(UserID)
);

CREATE TABLE PU_Applications (
    ApplicationID VARCHAR(20) NOT NULL PRIMARY KEY,
    ApplicationType VARCHAR(50),
    ApplicantName VARCHAR(100),
    CompanyName VARCHAR(100),
    CompanyHouseReg VARCHAR(50),
    Address VARCHAR(255),
    EmailAddress VARCHAR(100),
    ContactEmail VARCHAR(100),
    Status ENUM('Pending','Approved','Rejected') DEFAULT 'Pending',
    ApplicationStatus ENUM('Pending','Approved','Rejected') DEFAULT 'Pending',
    SubmittedAt DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- SA Users (matching marking criteria spec)
INSERT INTO Users (Username, Password, Role) VALUES
    ('Sysdba',     'London_weighting', 'Admin'),
    ('manager',    'Get_it_done',      'Director of Operations'),
    ('accountant', 'Count_money',      'Senior accountant'),
    ('clerk',      'Paperwork',        'Accountant'),
    ('warehouse1', 'Get_a_beer',       'Warehouse employee'),
    ('warehouse2', 'Lot_smell',        'Warehouse employee'),
    ('delivery',   'Too_dark',         'Delivery department employee');

-- SA Merchants (from marking criteria)
INSERT INTO Users (Username, Password, Role, AccountNo, AccountHolderName, ContactName, Address, Phone, CreditLimit, DiscountPlan, DiscountRate, AccountStatus, OutstandingBalance) VALUES
    ('city',    'northampton', 'Merchant', 'ACC0001', 'CityPharmacy',  'Prof David Rhind', 'Northampton Square, London EC1V 0HB', '0207 040 8000', 10000.00, 'Fixed',    '3',                           'Normal', 0.00),
    ('cosymed', 'bondstreet',  'Merchant', 'ACC0002', 'Cosymed Ltd',   'Mr Alex Wright',   '25, Bond Street, London WC1V 8LS',    '0207 321 8001',  5000.00, 'Variable', '<1000:0,1000-2000:1,2000+:2', 'Normal', 0.00),
    ('hello',   'there',       'Merchant', 'ACC0003', 'HelloPharmacy', 'Mr Bruno Wright',  '12, Bond Street, London WC1V 9NS',    '0207 321 8002',  5000.00, 'Variable', '<1000:0,1000-2000:1,2000+:3', 'Normal', 0.00);

-- SA Catalogue (all 14 items from marking criteria)
INSERT INTO Catalogue (ProductID, Name, Description, Category, PackageType, Unit, UnitsInPack, PackageCost, Availability, StockLimit) VALUES
    ('100 00001', 'Paracetamol',           'OTC painkiller',     'OTC Pain Relief', 'box',    'Caps', 20,  0.10, 10345, 300),
    ('100 00002', 'Aspirin',               'OTC painkiller',     'OTC Pain Relief', 'box',    'Caps', 20,  0.50, 12453, 500),
    ('100 00003', 'Analgin',               'OTC painkiller',     'OTC Pain Relief', 'box',    'Caps', 10,  1.20,  4235, 200),
    ('100 00004', 'Celebrex, caps 100 mg', 'Prescription NSAID', 'Prescription',    'box',    'Caps', 10, 10.00,  3420, 200),
    ('100 00005', 'Celebrex, caps 200 mg', 'Prescription NSAID', 'Prescription',    'box',    'caps', 10, 18.50,  1450, 150),
    ('100 00006', 'Retin-A Tretin, 30 g',  'Skin care',          'Skin Care',       'box',    'caps', 20, 25.00,  2013, 200),
    ('100 00007', 'Lipitor TB, 20 mg',     'Statin',             'Prescription',    'box',    'caps', 30, 15.50,  1562, 200),
    ('100 00008', 'Claritin CR, 60g',      'Antihistamine',      'Allergy',         'box',    'caps', 20, 19.50,  2540, 200),
    ('200 00004', 'Iodine tincture',       'Antiseptic',         'Antiseptic',      'bottle', 'ml',  100,  0.30, 22134, 200),
    ('200 00005', 'Rhynol',                'Nasal spray',        'Nasal',           'bottle', 'ml',  200,  2.50,  1908, 300),
    ('300 00001', 'Ospen',                 'Antibiotic',         'Antibiotic',      'box',    'caps', 20, 10.50,   809, 200),
    ('300 00002', 'Amopen',                'Antibiotic',         'Antibiotic',      'box',    'caps', 30, 15.00,  1340, 300),
    ('400 00001', 'Vitamin C',             'Supplement',         'Supplements',     'box',    'caps', 30,  1.20,  3258, 300),
    ('400 00002', 'Vitamin B12',           'Supplement',         'Supplements',     'box',    'caps', 30,  1.30,  2673, 300);

-- Historical orders (scenarios 1-5 from marking criteria)
INSERT INTO Orders (OrderID, MerchantID, OrderDate, TotalAmount, OrderStatus, EstimatedDelivery, DispatchDetails)
SELECT 'ORD-2026-00001', UserID, '2026-02-20 10:00:00', 508.60, 'DELIVERED', '2026-02-23 15:00:00', 'InfoPharma courier' FROM Users WHERE Username='city';

INSERT INTO OrderItems (OrderID, ProductID, Quantity, UnitCost) VALUES
    ('ORD-2026-00001', '100 00001', 10,  0.10),
    ('ORD-2026-00001', '100 00003', 20,  1.20),
    ('ORD-2026-00001', '200 00004', 20,  0.30),
    ('ORD-2026-00001', '200 00005', 10,  2.50),
    ('ORD-2026-00001', '300 00001', 10, 10.50),
    ('ORD-2026-00001', '300 00002', 20, 15.00),
    ('ORD-2026-00001', '400 00001', 20,  1.20),
    ('ORD-2026-00001', '400 00002', 20,  1.30);

INSERT INTO Orders (OrderID, MerchantID, OrderDate, TotalAmount, OrderStatus, EstimatedDelivery, DispatchDetails)
SELECT 'ORD-2026-00002', UserID, '2026-02-25 10:00:00', 376.00, 'DELIVERED', '2026-02-26 17:00:00', 'DHL' FROM Users WHERE Username='cosymed';

INSERT INTO OrderItems (OrderID, ProductID, Quantity, UnitCost) VALUES
    ('ORD-2026-00002', '100 00001', 10,  0.10),
    ('ORD-2026-00002', '100 00003', 20,  1.20),
    ('ORD-2026-00002', '200 00005', 10,  2.50),
    ('ORD-2026-00002', '300 00002', 20, 15.00),
    ('ORD-2026-00002', '400 00002', 20,  1.30);

INSERT INTO Orders (OrderID, MerchantID, OrderDate, TotalAmount, OrderStatus, EstimatedDelivery, DispatchDetails)
SELECT 'ORD-2026-00003', UserID, '2026-03-10 09:00:00', 430.00, 'DELIVERED', '2026-03-12 11:00:00', 'InfoPharma courier' FROM Users WHERE Username='cosymed';

INSERT INTO OrderItems (OrderID, ProductID, Quantity, UnitCost) VALUES
    ('ORD-2026-00003', '200 00005', 10,  2.50),
    ('ORD-2026-00003', '300 00001', 10, 10.50),
    ('ORD-2026-00003', '300 00002', 20, 15.00);

-- Payments (scenarios 8 & 9)
INSERT INTO Payments (MerchantID, Amount, PaymentDate, PaymentMethod)
SELECT UserID, 508.60, '2026-03-15 10:00:00', 'Bank Transfer' FROM Users WHERE Username='city';

INSERT INTO Payments (MerchantID, Amount, PaymentDate, PaymentMethod)
SELECT UserID, 376.00, '2026-03-15 14:00:00', 'Credit Card' FROM Users WHERE Username='cosymed';

USE iposca_database;
SELECT '✓ CA + SA databases ready. Build project and run Main.java' AS status;
