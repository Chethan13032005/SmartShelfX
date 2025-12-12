CREATE TABLE IF NOT EXISTS forecast_results (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  forecast_qty DOUBLE NOT NULL,
  forecast_date DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_forecast_product_date (product_id, forecast_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
