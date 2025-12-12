-- Create stock_transactions table if it does not exist

CREATE TABLE IF NOT EXISTS stock_transactions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  type VARCHAR(10) NOT NULL,
  quantity INT NOT NULL,
  notes VARCHAR(500) NULL,
  performed_by VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_stock_txn_product_id (product_id),
  INDEX idx_stock_txn_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
