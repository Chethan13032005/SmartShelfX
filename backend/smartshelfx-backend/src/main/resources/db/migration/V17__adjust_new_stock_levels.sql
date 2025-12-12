-- Lower stocks for new SKUs to demonstrate urgency
UPDATE products SET current_stock = 10 WHERE sku='SKU-2001';
UPDATE products SET current_stock = 12 WHERE sku='SKU-2002';
