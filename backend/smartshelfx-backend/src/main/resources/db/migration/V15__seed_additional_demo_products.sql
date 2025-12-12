-- Add two additional demo products (idempotent)
INSERT INTO products (sku, name, current_stock, reorder_level, vendor_email)
VALUES
 ('SKU-2001','Yogurt 500g',35,25,'vendorC@example.com'),
 ('SKU-2002','Apples 1kg',50,30,'vendorD@example.com')
ON DUPLICATE KEY UPDATE name=VALUES(name), current_stock=VALUES(current_stock), reorder_level=VALUES(reorder_level), vendor_email=VALUES(vendor_email);
