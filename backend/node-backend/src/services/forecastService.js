import axios from 'axios';
import db from '../config/db.js';

const FORECAST_URL = process.env.FORECAST_SERVICE_URL || 'http://localhost:8080/forecast/predict';

export const runForecast = async (req, res) => {
  try {
    // Pull aggregated daily sales from DB
    const [rows] = await db.query(`
      SELECT product_id AS productId, SUM(quantity) AS sold, DATE(created_at) AS date
      FROM stock_transactions
      WHERE type='OUT'
      GROUP BY product_id, DATE(created_at)
    `);

    // Send to Java forecasting service
    const response = await axios.post(FORECAST_URL, rows, { timeout: 30000 });
    const data = response.data;

    // Persist forecast results (upsert approach)
    for (const r of data) {
      // Ensure values are present and types are sane
      const productId = r.productId;
      const predicted = Number(r.predicted || 0);
      // Use ISO date string for MySQL DATE/TIMESTAMP compatibility
      const forecastDate = r.forecastDate || new Date().toISOString().slice(0, 19).replace('T', ' ');

      await db.query(
        `INSERT INTO forecast_results (product_id, forecast_qty, forecast_date, created_at) VALUES (?, ?, ?, NOW())`,
        [productId, predicted, forecastDate]
      );
    }

    res.json({ message: 'Forecast complete', forecast: data });
  } catch (err) {
    console.error('runForecast error', err.message || err);
    res.status(500).json({ error: 'Forecast failed', details: err.message });
  }
};
