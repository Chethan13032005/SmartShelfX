import express from 'express';
import { autoRestock } from '../services/poService.js';

const router = express.Router();

router.post('/auto', autoRestock);

export default router;
