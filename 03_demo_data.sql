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
