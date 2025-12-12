-- Add po_number column to purchase_orders table if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'purchase_orders' 
  AND COLUMN_NAME = 'po_number';

SET @query = IF(@col_exists = 0, 
    'ALTER TABLE purchase_orders ADD COLUMN po_number VARCHAR(50) UNIQUE',
    'SELECT "Column po_number already exists" AS info');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Generate po_number for existing records that don't have one
UPDATE purchase_orders 
SET po_number = CONCAT('PO-', LPAD(id, 8, '0')) 
WHERE po_number IS NULL OR po_number = '';

-- Make po_number NOT NULL if it's nullable
SET @query2 = IF(@col_exists = 0, 
    'ALTER TABLE purchase_orders MODIFY COLUMN po_number VARCHAR(50) NOT NULL',
    'SELECT "Column already configured" AS info');
PREPARE stmt2 FROM @query2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;
