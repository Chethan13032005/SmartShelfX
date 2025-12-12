-- Seed demo stock OUT transactions over recent days for forecasting
-- Ensure products exist with IDs by referencing SKU -> ID mapping
-- This uses a simple approach: insert by joining SKU to get product_id

INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 5, 'Demo sale', 'system', NOW() - INTERVAL 7 DAY FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 7, 'Demo sale', 'system', NOW() - INTERVAL 5 DAY FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 6, 'Demo sale', 'system', NOW() - INTERVAL 3 DAY FROM products p WHERE p.sku='SKU-1001';

INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 4, 'Demo sale', 'system', NOW() - INTERVAL 10 DAY FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 6, 'Demo sale', 'system', NOW() - INTERVAL 6 DAY FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 5, 'Demo sale', 'system', NOW() - INTERVAL 2 DAY FROM products p WHERE p.sku='SKU-1002';

INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 3, 'Demo sale', 'system', NOW() - INTERVAL 9 DAY FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 4, 'Demo sale', 'system', NOW() - INTERVAL 4 DAY FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 5, 'Demo sale', 'system', NOW() - INTERVAL 1 DAY FROM products p WHERE p.sku='SKU-1003';
