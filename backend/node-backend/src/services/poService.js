import db from '../config/db.js';
import { sendEmail } from './utils.js';

export const autoRestock = async (req, res) => {
  try {
    const [rows] = await db.query(`
      SELECT p.id, p.sku, p.vendor_id AS vendorId, p.current_stock AS currentStock, f.forecast_qty AS forecastQty
      FROM products p
      JOIN forecast_results f ON f.product_id = p.id
      WHERE p.current_stock < f.forecast_qty
    `);

    for (const item of rows) {
      const qty = item.forecastQty - item.currentStock;
      // Example stored procedure call; adapt to your DB's sp signature
      await db.query('CALL sp_create_po(?,?,?,?)', [item.id, item.vendorId, qty, req.user ? req.user.id : 0]);

      // Send vendor email (example)
      await sendEmail('vendor@example.com', `Purchase Order for ${item.sku}`, `Please create PO for ${qty} units to replenish stock.`);
    }

    res.json({ autoPOs: rows.length });
  } catch (err) {
    console.error('autoRestock error', err.message || err);
    res.status(500).json({ error: 'Auto restock failed', details: err.message });
  }
};
