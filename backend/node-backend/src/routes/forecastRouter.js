import express from 'express';
import { runForecast } from '../services/forecastService.js';

const router = express.Router();

router.post('/run', runForecast);

export default router;
