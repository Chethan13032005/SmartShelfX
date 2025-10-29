mysql -u root -p smartshelfx_db < 'D:\Internship\Infosys_SmartShelfX AI Based Inventory Forecast and Auto Restock\backend\smartshelfx-backend\update_schema.sql'mysql -u root -p smartshelfx_db < 'D:\Internship\Infosys_SmartShelfX AI Based Inventory Forecast and Auto Restock\backend\smartshelfx-backend\update_schema.sql'-- Use the smartshelfx_db database
USE smartshelfx_db;

-- First, let's see what columns exist
-- Run this to check: DESCRIBE users;

-- Update the users table to have all required columns
-- Add missing columns if they don't exist
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS first_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS last_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS company VARCHAR(255),
ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20),
ADD COLUMN IF NOT EXISTS warehouse_location VARCHAR(255),
ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE;

-- If full_name exists and is NOT NULL but first_name/last_name are empty,
-- let's populate them from full_name
UPDATE users 
SET first_name = SUBSTRING_INDEX(full_name, ' ', 1),
    last_name = SUBSTRING_INDEX(full_name, ' ', -1)
WHERE full_name IS NOT NULL 
  AND (first_name IS NULL OR first_name = '');

-- Now make full_name nullable since we'll auto-generate it
ALTER TABLE users 
MODIFY COLUMN full_name VARCHAR(255) NULL;

-- Update any null full_name values from first_name and last_name
UPDATE users 
SET full_name = CONCAT(COALESCE(first_name, ''), ' ', COALESCE(last_name, ''))
WHERE full_name IS NULL;

-- If you want to make first_name required (NOT NULL), uncomment this:
-- ALTER TABLE users MODIFY COLUMN first_name VARCHAR(255) NOT NULL;

-- Verify the changes
SELECT 'Users table updated successfully!' as Status;
DESCRIBE users;
