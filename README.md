# SmartShelfX - AI-Based Inventory Forecasting & Auto Restock

A comprehensive inventory management system with AI-powered demand forecasting, automated restock recommendations, and multi-service architecture.

## 🎯 Features

- **AI Demand Forecasting** - Predict future demand using historical data with 6 ML algorithms
- **Auto-Restock System** - Automated purchase order creation based on forecasts
- **Multi-Backend Architecture** - Spring Boot (main) + Node.js orchestrator + Java ML microservice
- **Role-Based Access** - Admin, Manager, Vendor dashboards with JWT authentication
- **Real-Time Notifications** - Stock alerts, PO updates, expiry warnings
- **React Frontend** - Modern UI with warm, nature-inspired design (no "AI purple")

## 📁 Project Structure

```
SmartShelfX/
├── backend/
│   ├── smartshelfx-backend/       # Main Spring Boot API (port 8082)
│   ├── node-backend/               # Node.js orchestrator (port 4000)
│   └── java-forecast-service/     # Java ML microservice (port 8080)
├── frontend/
│   └── smartshelfx-frontend/      # React app (port 3000)
├── docs/                          # Documentation
└── RUN_THIS_IN_MYSQL_WORKBENCH.sql # Database setup
```

## 🚀 Quick Start

### 1. Database Setup
```sql
-- Run in MySQL Workbench
source RUN_THIS_IN_MYSQL_WORKBENCH.sql
```

### 2. Backend (Spring Boot - Main API)
```powershell
cd backend\smartshelfx-backend
.\mvnw.cmd spring-boot:run
```
Runs on http://localhost:8082

### 3. Java Forecast Service (Optional ML Service)
```powershell
cd backend\java-forecast-service
.\mvnw.cmd spring-boot:run
```
Runs on http://localhost:8080

### 4. Node Backend (Optional Orchestrator)
```powershell
cd backend\node-backend
npm install
cp .env.example .env
npm run dev
```
Runs on http://localhost:4000

### 5. Frontend (React)
```powershell
cd frontend\smartshelfx-frontend
npm install
npm start
```
Runs on http://localhost:3000

## 🔑 Default Login

- **Admin**: admin@smartshelf.com / Admin@123
- **Manager**: manager@smartshelf.com / Manager@123
- **Vendor**: vendor@smartshelf.com / Vendor@123

## 📊 Key Endpoints

### Main Spring Boot API (8082)
- `POST /api/auth/login` - Authentication
- `GET /api/forecast/all` - All demand forecasts
- `POST /api/forecast/run` - Trigger forecast analysis
- `GET /api/po/suggested` - Auto-restock suggestions
- `GET /api/notifications` - User notifications

### Node Orchestrator (4000)
- `POST /api/forecast/run` - Run ML forecast via Java service
- `POST /api/po/auto` - Create auto POs
- `GET /api/notifications` - Notifications feed

### Java Forecast Service (8080)
- `POST /forecast/predict` - ML demand prediction

## 🎨 UI Features

- **Dashboard**: Role-based analytics and KPIs
- **Demand Forecasting**: 
  - 7-day daily forecasts + 4-week projections
  - Risk classification (Critical/High/Medium/Low)
  - Interactive charts with warm color scheme
  - Confidence scoring (0-100%)
- **Auto-Restock**:
  - AI-driven PO suggestions
  - Vendor integration
  - Email notifications
- **Stock Management**: Real-time IN/OUT tracking
- **Purchase Orders**: Full lifecycle management

## 🛠️ Technologies

**Backend:**
- Spring Boot 3.1.5 (Java 17)
- Node.js + Express
- MySQL 8.0
- JWT Authentication
- Weka ML (Java forecasting)

**Frontend:**
- React 18
- Chart.js (visualizations)
- Tailwind CSS (warm/nature-inspired design)
- Axios (API calls)

## 📖 Documentation

- [START_HERE.md](START_HERE.md) - Main guide
- [DEMAND_FORECASTING_GUIDE.md](DEMAND_FORECASTING_GUIDE.md) - Forecasting details
- [backend/INTEGRATION_GUIDE.md](backend/INTEGRATION_GUIDE.md) - Multi-service setup
- [docs/USER_ROLES.md](docs/USER_ROLES.md) - Role permissions

## 🔧 Configuration

### Backend (.env for node-backend)
```env
DB_HOST=127.0.0.1
DB_USER=root
DB_PASS=yourpassword
DB_NAME=smartshelfx
FORECAST_SERVICE_URL=http://localhost:8080/forecast/predict
```

### Frontend (.env.development)
```env
REACT_APP_API_URL=http://localhost:8082/api
```

## 📝 Development

**Start all services:**
```powershell
# Terminal 1 - Main backend
cd backend\smartshelfx-backend
.\mvnw.cmd spring-boot:run

# Terminal 2 - Frontend
cd frontend\smartshelfx-frontend
npm start
```

**Optional microservices:**
```powershell
# Terminal 3 - Node orchestrator
cd backend\node-backend
npm run dev

# Terminal 4 - Java forecast
cd backend\java-forecast-service
.\mvnw.cmd spring-boot:run
```

## 🎯 Forecasting Algorithms

1. **Moving Average** (14-day window)
2. **Exponential Smoothing** (for stable patterns)
3. **Trend Analysis** (first-half vs second-half)
4. **Seasonal Detection** (weekday patterns)
5. **Volatility Calculation** (coefficient of variation)
6. **Confidence Scoring** (multi-factor: data quality, coverage, stability)

## 🚨 Troubleshooting

**Backend won't start:**
- Ensure MySQL is running
- Check port 8082 is free
- Verify DB credentials in `application.properties`

**Frontend compile errors:**
- Run `npm install` in frontend folder
- Clear node_modules: `Remove-Item node_modules -Recurse -Force ; npm install`

**Forecast not working:**
- Ensure you have historical stock_transactions data
- Check Java forecast service is running (if using microservices)
- Verify `FORECAST_SERVICE_URL` in node backend .env

## 📜 License

See [LICENSE](LICENSE) file.

## 👥 Team

Infosys SmartShelfX Team - AI-Based Inventory Management
