-- Add image_url column to products table
USE smartshelfx_db;

-- Add the image_url column if it doesn't exist
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Verify the changes
SELECT 'Products table updated with image_url column!' as Status;
DESCRIBE products;
