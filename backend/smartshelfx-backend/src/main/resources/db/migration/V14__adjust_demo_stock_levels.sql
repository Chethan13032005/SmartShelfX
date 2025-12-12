-- Lower current stock below reorder levels to showcase AI restock urgency
UPDATE products SET current_stock = 12, reorder_level = 30 WHERE sku='SKU-1001';
UPDATE products SET current_stock = 9,  reorder_level = 20 WHERE sku='SKU-1002';
UPDATE products SET current_stock = 7,  reorder_level = 18 WHERE sku='SKU-1003';
