-- Seed demo products for SmartShelfX
INSERT INTO products (sku, name, current_stock, reorder_level, vendor_email)
VALUES
 ('SKU-1001','Milk 1L',60,30,'vendorA@example.com'),
 ('SKU-1002','Bread',40,20,'vendorB@example.com'),
 ('SKU-1003','Eggs 12',24,18,'vendorA@example.com')
ON DUPLICATE KEY UPDATE name=VALUES(name), current_stock=VALUES(current_stock), reorder_level=VALUES(reorder_level), vendor_email=VALUES(vendor_email);
