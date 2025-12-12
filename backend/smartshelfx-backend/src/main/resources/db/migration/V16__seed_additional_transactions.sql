-- Seed 30-day OUT transactions for new SKUs
-- Yogurt 500g (SKU-2001): weekday steady, small fluctuations
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 30 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 28 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 26 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 24 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 5, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 22 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 20 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 18 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 3, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 16 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 5, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 14 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 12 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 6, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 10 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 8 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 5, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 6 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 4 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 6, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM products p WHERE p.sku='SKU-2001';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 5, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM products p WHERE p.sku='SKU-2001';

-- Apples 1kg (SKU-2002): weekend spikes
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 30 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 5, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 27 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 7, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 24 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 4, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 22 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 8, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 20 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 5, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 18 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 9, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 16 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 6, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 14 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 10, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 12 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 7, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 10 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 8, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 8 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 11, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 6 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 6, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 4 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 10, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM products p WHERE p.sku='SKU-2002';
INSERT INTO stock_transactions (product_id, type, quantity, notes, performed_by, created_at) SELECT p.id, 'OUT', 9, 'Demo sale', 'system', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM products p WHERE p.sku='SKU-2002';
