import db from '../config/db.js';

export const getNotifications = async (req, res) => {
  try {
    const userId = req.user ? req.user.id : req.query.userId || 0;
    const [rows] = await db.query('SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC', [userId]);
    res.json(rows);
  } catch (err) {
    console.error('getNotifications', err.message || err);
    res.status(500).json({ error: 'Failed to load notifications' });
  }
};

export const markRead = async (req, res) => {
  try {
    const id = req.params.id;
    await db.query('UPDATE notifications SET is_read = 1 WHERE id = ?', [id]);
    res.json({ message: 'Notification cleared' });
  } catch (err) {
    console.error('markRead', err.message || err);
    res.status(500).json({ error: 'Failed to mark read' });
  }
};
