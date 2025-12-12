-- Seed 30 days of realistic OUT transactions for demo SKUs
-- Milk 1L (SKU-1001): weekday higher consumption, slight upward trend
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 30 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at)
SELECT p.id, 'OUT', 5, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 28 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 6, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 26 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 5, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 25 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 7, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 23 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 6, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 21 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 8, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 19 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 7, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 17 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 9, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 15 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 8, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 13 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 10, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 11 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 9, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 9 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 11, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 7 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 12, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 5 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 13, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 3 DAY) FROM products p WHERE p.sku='SKU-1001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 14, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM products p WHERE p.sku='SKU-1001';

-- Bread (SKU-1002): weekend higher consumption
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 30 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 27 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 6, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 24 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 22 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 7, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 20 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 5, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 18 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 8, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 16 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 14 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 9, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 12 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 6, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 10 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 7, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 8 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 10, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 6 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 5, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 4 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 9, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM products p WHERE p.sku='SKU-1002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 7, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM products p WHERE p.sku='SKU-1002';

-- Eggs 12 (SKU-1003): stable consumption
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 2, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 30 DAY) FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 27 DAY) FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 2, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 24 DAY) FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 21 DAY) FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 2, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 18 DAY) FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 15 DAY) FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 2, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 12 DAY) FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 9 DAY) FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 2, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 6 DAY) FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 3 DAY) FROM products p WHERE p.sku='SKU-1003';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 2, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM products p WHERE p.sku='SKU-1003';
