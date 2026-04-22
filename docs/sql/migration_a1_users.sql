-- A1 migration script for users table (MySQL compatibility edition)
-- Target DB: competition_platform
-- Execute:
-- mysql -u root -p competition_platform < docs/sql/migration_a1_users.sql

USE competition_platform;

-- Optional pre-check: duplicate usernames (should be empty before unique index)
SELECT username, COUNT(*) AS cnt
FROM users
WHERE username IS NOT NULL AND TRIM(username) <> ''
GROUP BY username
HAVING cnt > 1;

-- 1) approval_status column (idempotent via information_schema)
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE users ADD COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT ''APPROVED'' AFTER role',
    'SELECT ''approval_status exists'' AS msg'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'approval_status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) display_name column
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE users ADD COLUMN display_name VARCHAR(64) NULL AFTER username',
    'SELECT ''display_name exists'' AS msg'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'display_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) backfill data
UPDATE users
SET username = account_no
WHERE username IS NULL OR TRIM(username) = '';

UPDATE users
SET display_name = username
WHERE display_name IS NULL OR TRIM(display_name) = '';

-- 4) unique index on username
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username)',
    'SELECT ''uk_users_username exists'' AS msg'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND INDEX_NAME = 'uk_users_username'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Post-checks
DESC users;
SHOW INDEX FROM users;
SELECT id, username, display_name, role, approval_status
FROM users
ORDER BY id
LIMIT 20;
