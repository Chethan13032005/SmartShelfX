-- ==========================================
-- V5: Drop all stock-related triggers
-- ==========================================
-- This migration removes problematic triggers that interfere with stock operations

-- Drop all possible trigger names
DROP TRIGGER IF EXISTS check_stock_before_transaction;
DROP TRIGGER IF EXISTS validate_stock_transaction;
DROP TRIGGER IF EXISTS check_stock_availability;
DROP TRIGGER IF EXISTS stock_validation_trigger;
DROP TRIGGER IF EXISTS prevent_negative_stock;
DROP TRIGGER IF EXISTS stock_check_trigger;
DROP TRIGGER IF EXISTS before_stock_transaction_insert;
DROP TRIGGER IF EXISTS before_insert_stock_transactions;
DROP TRIGGER IF EXISTS stock_transactions_before_insert;

-- Log completion
SELECT 'V5 Migration: All stock triggers dropped successfully' AS status;
