import express from 'express';
import dotenv from 'dotenv';
import forecastRouter from './routes/forecastRouter.js';
import poRouter from './routes/poRouter.js';
import notificationsRouter from './routes/notificationsRouter.js';

dotenv.config();
const app = express();
app.use(express.json());

app.use('/api/forecast', forecastRouter);
app.use('/api/po', poRouter);
app.use('/api/notifications', notificationsRouter);

const PORT = process.env.PORT || 4000;
app.listen(PORT, () => console.log(`Node backend running on port ${PORT}`));
