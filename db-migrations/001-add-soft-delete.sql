-- ============================================================================
-- Civil eCommerce Database Migration
-- Migration ID: V1__Add_Soft_Delete_Support
-- Date: January 27, 2026
-- Purpose: Add soft delete support for User and Order entities
-- Status: REQUIRED for security compliance
-- ============================================================================

-- Step 1: Add soft delete columns to users table
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- Step 2: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_is_deleted ON users(is_deleted);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);

-- Step 3: Add soft delete columns to orders table
ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- Step 4: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_orders_is_deleted ON orders(is_deleted);
CREATE INDEX IF NOT EXISTS idx_orders_deleted_at ON orders(deleted_at);

-- Step 5: Verify migrations
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME IN ('users', 'orders') 
AND COLUMN_NAME IN ('is_deleted', 'deleted_at')
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- Step 6: Verify indexes
SHOW INDEX FROM users WHERE Column_name IN ('is_deleted', 'deleted_at');
SHOW INDEX FROM orders WHERE Column_name IN ('is_deleted', 'deleted_at');

-- Migration complete!
-- All soft delete columns and indexes have been created successfully.
