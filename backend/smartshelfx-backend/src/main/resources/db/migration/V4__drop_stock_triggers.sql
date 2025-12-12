-- Drop any existing triggers on stock_transactions table that might be preventing inserts
-- These triggers were checking stock levels, but we handle that in the application layer

DROP TRIGGER IF EXISTS before_stock_transaction_insert;
DROP TRIGGER IF EXISTS check_stock_before_transaction;
DROP TRIGGER IF EXISTS validate_stock_transaction;
DROP TRIGGER IF EXISTS stock_validation_trigger;

-- The application (StockTransactionService) now handles:
-- 1. Stock validation before transaction
-- 2. Inventory update BEFORE transaction insert
-- 3. Low stock alerts
-- 4. Notifications

-- No database triggers needed - all logic in application layer
