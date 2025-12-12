import express from 'express';
import { getNotifications, markRead } from '../services/notificationsService.js';

const router = express.Router();

router.get('/', getNotifications);
router.post('/:id/read', markRead);

export default router;
