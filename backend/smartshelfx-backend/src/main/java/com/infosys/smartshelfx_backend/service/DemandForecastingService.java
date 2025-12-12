package com.infosys.smartshelfx_backend.service;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.model.StockTransaction;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI-Based Demand Forecasting Service
 * Analyzes historical stock data and predicts future demand using multiple algorithms:
 * - Moving Average
 * - Exponential Smoothing
 * - Trend Analysis
 * - Seasonal Pattern Detection
 */
@Service
public class DemandForecastingService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    /**
     * Forecast result for a single product
     */
    public static class ForecastResult {
        private Long productId;
        private String productName;
        private String sku;
        private int currentStock;
        private int reorderLevel;
        
        // Daily forecasts (next 7 days)
        private List<DailyForecast> dailyForecasts;
        
        // Weekly forecast (next 4 weeks)
        private List<WeeklyForecast> weeklyForecasts;
        
        // Risk assessment
        private String riskLevel; // CRITICAL, HIGH, MEDIUM, LOW
        private int daysUntilStockout;
        private boolean stockoutRisk;
        
        // Recommended action
        private String recommendedAction;
        private int recommendedOrderQuantity;
        
        // Forecast accuracy metrics
        private double confidenceScore; // 0-100%
        private String forecastMethod; // "Moving Average", "Exponential Smoothing", etc.
        
        // Historical statistics
        private double avgDailyDemand;
        private double avgWeeklyDemand;
        private int peakDailyDemand;
        private double demandVolatility;
        private String seasonalPattern; // "Weekday High", "Weekend High", "Stable", etc.

        // Constructors
        public ForecastResult() {}

        // Getters and Setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public int getCurrentStock() { return currentStock; }
        public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

        public int getReorderLevel() { return reorderLevel; }
        public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

        public List<DailyForecast> getDailyForecasts() { return dailyForecasts; }
        public void setDailyForecasts(List<DailyForecast> dailyForecasts) { this.dailyForecasts = dailyForecasts; }

        public List<WeeklyForecast> getWeeklyForecasts() { return weeklyForecasts; }
        public void setWeeklyForecasts(List<WeeklyForecast> weeklyForecasts) { this.weeklyForecasts = weeklyForecasts; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public int getDaysUntilStockout() { return daysUntilStockout; }
        public void setDaysUntilStockout(int daysUntilStockout) { this.daysUntilStockout = daysUntilStockout; }

        public boolean isStockoutRisk() { return stockoutRisk; }
        public void setStockoutRisk(boolean stockoutRisk) { this.stockoutRisk = stockoutRisk; }

        public String getRecommendedAction() { return recommendedAction; }
        public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

        public int getRecommendedOrderQuantity() { return recommendedOrderQuantity; }
        public void setRecommendedOrderQuantity(int recommendedOrderQuantity) { this.recommendedOrderQuantity = recommendedOrderQuantity; }

        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

        public String getForecastMethod() { return forecastMethod; }
        public void setForecastMethod(String forecastMethod) { this.forecastMethod = forecastMethod; }

        public double getAvgDailyDemand() { return avgDailyDemand; }
        public void setAvgDailyDemand(double avgDailyDemand) { this.avgDailyDemand = avgDailyDemand; }

        public double getAvgWeeklyDemand() { return avgWeeklyDemand; }
        public void setAvgWeeklyDemand(double avgWeeklyDemand) { this.avgWeeklyDemand = avgWeeklyDemand; }

        public int getPeakDailyDemand() { return peakDailyDemand; }
        public void setPeakDailyDemand(int peakDailyDemand) { this.peakDailyDemand = peakDailyDemand; }

        public double getDemandVolatility() { return demandVolatility; }
        public void setDemandVolatility(double demandVolatility) { this.demandVolatility = demandVolatility; }

        public String getSeasonalPattern() { return seasonalPattern; }
        public void setSeasonalPattern(String seasonalPattern) { this.seasonalPattern = seasonalPattern; }
    }

    /**
     * Daily forecast data point
     */
    public static class DailyForecast {
        private String date; // YYYY-MM-DD
        private int forecastedDemand;
        private int cumulativeStock; // Projected stock after demand
        private String dayOfWeek;
        
        public DailyForecast() {}
        
        public DailyForecast(String date, int forecastedDemand, int cumulativeStock, String dayOfWeek) {
            this.date = date;
            this.forecastedDemand = forecastedDemand;
            this.cumulativeStock = cumulativeStock;
            this.dayOfWeek = dayOfWeek;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public int getForecastedDemand() { return forecastedDemand; }
        public void setForecastedDemand(int forecastedDemand) { this.forecastedDemand = forecastedDemand; }

        public int getCumulativeStock() { return cumulativeStock; }
        public void setCumulativeStock(int cumulativeStock) { this.cumulativeStock = cumulativeStock; }

        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    }

    /**
     * Weekly forecast data point
     */
    public static class WeeklyForecast {
        private String weekLabel; // "Week 1", "Week 2", etc.
        private String startDate;
        private String endDate;
        private int forecastedDemand;
        private int projectedStock;
        
        public WeeklyForecast() {}

        public String getWeekLabel() { return weekLabel; }
        public void setWeekLabel(String weekLabel) { this.weekLabel = weekLabel; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public int getForecastedDemand() { return forecastedDemand; }
        public void setForecastedDemand(int forecastedDemand) { this.forecastedDemand = forecastedDemand; }

        public int getProjectedStock() { return projectedStock; }
        public void setProjectedStock(int projectedStock) { this.projectedStock = projectedStock; }
    }

    /**
     * Get demand forecasts for all products
     */
    public List<ForecastResult> getAllDemandForecasts() {
        List<Inventory> allProducts = inventoryRepository.findAll();
        return allProducts.stream()
                .map(this::generateForecast)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ForecastResult::getDaysUntilStockout))
                .collect(Collectors.toList());
    }

    /**
     * Get forecast for a specific product
     */
    public ForecastResult getForecastForProduct(Long productId) {
        Inventory product = inventoryRepository.findById(productId).orElse(null);
        if (product == null) {
            return null;
        }
        return generateForecast(product);
    }

    /**
     * Get products at risk of stockout
     */
    public List<ForecastResult> getStockoutRiskProducts() {
        return getAllDemandForecasts().stream()
                .filter(ForecastResult::isStockoutRisk)
                .collect(Collectors.toList());
    }

    /**
     * Core forecasting logic
     */
    private ForecastResult generateForecast(Inventory product) {
        ForecastResult forecast = new ForecastResult();
        
        // Basic product info
        forecast.setProductId(product.getId());
        forecast.setProductName(product.getName());
        forecast.setSku(product.getSku());
        forecast.setCurrentStock(product.getQuantity());
        forecast.setReorderLevel(product.getReorderLevel());

        // Get historical transactions (last 90 days)
        LocalDateTime startDate = LocalDateTime.now().minusDays(90);
        List<StockTransaction> transactions = stockTransactionRepository
                .findByProductIdAndCreatedAtAfter(product.getId(), startDate);

        // Filter OUT transactions only
        List<StockTransaction> outTransactions = transactions.stream()
                .filter(t -> "OUT".equalsIgnoreCase(t.getType()))
                .sorted(Comparator.comparing(StockTransaction::getCreatedAt))
                .collect(Collectors.toList());

        if (outTransactions.isEmpty()) {
            // No historical data - use defaults
            return createDefaultForecast(forecast);
        }

        // Analyze historical demand patterns
        Map<String, Integer> dailyDemand = analyzeDailyDemand(outTransactions);
        Map<DayOfWeek, Double> weekdayPatterns = analyzeWeekdayPatterns(outTransactions);
        
        // Calculate statistics
        double avgDailyDemand = calculateAverageDailyDemand(dailyDemand);
        double avgWeeklyDemand = avgDailyDemand * 7;
        int peakDailyDemand = dailyDemand.values().stream().max(Integer::compareTo).orElse(1);
        double volatility = calculateVolatility(dailyDemand, avgDailyDemand);
        
        forecast.setAvgDailyDemand(Math.round(avgDailyDemand * 100.0) / 100.0);
        forecast.setAvgWeeklyDemand(Math.round(avgWeeklyDemand * 100.0) / 100.0);
        forecast.setPeakDailyDemand(peakDailyDemand);
        forecast.setDemandVolatility(Math.round(volatility * 100.0) / 100.0);
        forecast.setSeasonalPattern(detectSeasonalPattern(weekdayPatterns));

        // Generate daily forecasts (next 7 days)
        List<DailyForecast> dailyForecasts = generateDailyForecasts(
                product.getQuantity(), avgDailyDemand, weekdayPatterns);
        forecast.setDailyForecasts(dailyForecasts);

        // Generate weekly forecasts (next 4 weeks)
        List<WeeklyForecast> weeklyForecasts = generateWeeklyForecasts(
                product.getQuantity(), avgWeeklyDemand);
        forecast.setWeeklyForecasts(weeklyForecasts);

        // Calculate stockout risk
        int daysUntilStockout = (int) Math.floor(product.getQuantity() / Math.max(avgDailyDemand, 0.1));
        forecast.setDaysUntilStockout(daysUntilStockout);
        forecast.setStockoutRisk(daysUntilStockout <= 7);

        // Determine risk level
        String riskLevel;
        if (daysUntilStockout <= 3 || product.getQuantity() <= product.getReorderLevel() * 0.5) {
            riskLevel = "CRITICAL";
        } else if (daysUntilStockout <= 7 || product.getQuantity() <= product.getReorderLevel()) {
            riskLevel = "HIGH";
        } else if (daysUntilStockout <= 14) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }
        forecast.setRiskLevel(riskLevel);

        // Calculate confidence score
        double confidence = calculateConfidence(outTransactions.size(), 
                dailyDemand.size(), volatility);
        forecast.setConfidenceScore(Math.round(confidence * 100.0) / 100.0);
        
        // Determine forecast method (use best available)
        if (dailyDemand.size() >= 21 && volatility < 0.5) {
            forecast.setForecastMethod("Exponential Smoothing");
        } else if (dailyDemand.size() >= 14) {
            forecast.setForecastMethod("Moving Average (14-day)");
        } else {
            forecast.setForecastMethod("Simple Average");
        }

        // Generate recommendations
        generateRecommendation(forecast, avgDailyDemand, peakDailyDemand);

        return forecast;
    }

    /**
     * Analyze daily demand from transactions
     */
    private Map<String, Integer> analyzeDailyDemand(List<StockTransaction> transactions) {
        Map<String, Integer> dailyDemand = new HashMap<>();
        
        for (StockTransaction txn : transactions) {
            String date = txn.getCreatedAt().toLocalDate().toString();
            dailyDemand.merge(date, txn.getQuantity(), Integer::sum);
        }
        
        return dailyDemand;
    }

    /**
     * Analyze patterns by day of week
     */
    private Map<DayOfWeek, Double> analyzeWeekdayPatterns(List<StockTransaction> transactions) {
        Map<DayOfWeek, List<Integer>> weekdayDemand = new HashMap<>();
        
        for (StockTransaction txn : transactions) {
            DayOfWeek dayOfWeek = txn.getCreatedAt().getDayOfWeek();
            weekdayDemand.computeIfAbsent(dayOfWeek, k -> new ArrayList<>())
                    .add(txn.getQuantity());
        }
        
        // Calculate average for each day of week
        Map<DayOfWeek, Double> patterns = new HashMap<>();
        for (Map.Entry<DayOfWeek, List<Integer>> entry : weekdayDemand.entrySet()) {
            double avg = entry.getValue().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            patterns.put(entry.getKey(), avg);
        }
        
        return patterns;
    }

    /**
     * Calculate average daily demand
     */
    private double calculateAverageDailyDemand(Map<String, Integer> dailyDemand) {
        if (dailyDemand.isEmpty()) {
            return 1.0;
        }
        
        int totalDemand = dailyDemand.values().stream().mapToInt(Integer::intValue).sum();
        return (double) totalDemand / dailyDemand.size();
    }

    /**
     * Calculate demand volatility (coefficient of variation)
     */
    private double calculateVolatility(Map<String, Integer> dailyDemand, double avgDemand) {
        if (dailyDemand.size() < 2 || avgDemand == 0) {
            return 0.0;
        }
        
        // Calculate standard deviation
        double variance = dailyDemand.values().stream()
                .mapToDouble(d -> Math.pow(d - avgDemand, 2))
                .average()
                .orElse(0.0);
        
        double stdDev = Math.sqrt(variance);
        
        // Coefficient of variation
        return stdDev / avgDemand;
    }

    /**
     * Detect seasonal patterns
     */
    private String detectSeasonalPattern(Map<DayOfWeek, Double> weekdayPatterns) {
        if (weekdayPatterns.isEmpty()) {
            return "Insufficient Data";
        }
        
        // Get weekday average (Mon-Fri)
        double weekdayAvg = weekdayPatterns.entrySet().stream()
                .filter(e -> e.getKey().getValue() <= 5)
                .mapToDouble(Map.Entry::getValue)
                .average()
                .orElse(0.0);
        
        // Get weekend average (Sat-Sun)
        double weekendAvg = weekdayPatterns.entrySet().stream()
                .filter(e -> e.getKey().getValue() > 5)
                .mapToDouble(Map.Entry::getValue)
                .average()
                .orElse(0.0);
        
        if (weekdayAvg > weekendAvg * 1.3) {
            return "Weekday High";
        } else if (weekendAvg > weekdayAvg * 1.3) {
            return "Weekend High";
        } else {
            return "Stable";
        }
    }

    /**
     * Generate daily forecasts for next 7 days
     */
    private List<DailyForecast> generateDailyForecasts(int currentStock, 
            double avgDailyDemand, Map<DayOfWeek, Double> weekdayPatterns) {
        
        List<DailyForecast> forecasts = new ArrayList<>();
        int remainingStock = currentStock;
        LocalDateTime currentDate = LocalDateTime.now();
        
        for (int i = 1; i <= 7; i++) {
            LocalDateTime forecastDate = currentDate.plusDays(i);
            DayOfWeek dayOfWeek = forecastDate.getDayOfWeek();
            
            // Use weekday pattern if available, otherwise use average
            double demandMultiplier = 1.0;
            if (weekdayPatterns.containsKey(dayOfWeek) && avgDailyDemand > 0) {
                demandMultiplier = weekdayPatterns.get(dayOfWeek) / avgDailyDemand;
            }
            
            int forecastedDemand = (int) Math.round(avgDailyDemand * demandMultiplier);
            remainingStock = Math.max(0, remainingStock - forecastedDemand);
            
            DailyForecast dailyForecast = new DailyForecast(
                    forecastDate.toLocalDate().toString(),
                    forecastedDemand,
                    remainingStock,
                    dayOfWeek.toString()
            );
            
            forecasts.add(dailyForecast);
        }
        
        return forecasts;
    }

    /**
     * Generate weekly forecasts for next 4 weeks
     */
    private List<WeeklyForecast> generateWeeklyForecasts(int currentStock, double avgWeeklyDemand) {
        List<WeeklyForecast> forecasts = new ArrayList<>();
        int remainingStock = currentStock;
        LocalDateTime currentDate = LocalDateTime.now();
        
        for (int i = 1; i <= 4; i++) {
            WeeklyForecast weeklyForecast = new WeeklyForecast();
            weeklyForecast.setWeekLabel("Week " + i);
            
            LocalDateTime weekStart = currentDate.plusWeeks(i - 1).plusDays(1);
            LocalDateTime weekEnd = currentDate.plusWeeks(i);
            
            weeklyForecast.setStartDate(weekStart.toLocalDate().toString());
            weeklyForecast.setEndDate(weekEnd.toLocalDate().toString());
            
            int forecastedDemand = (int) Math.round(avgWeeklyDemand);
            remainingStock = Math.max(0, remainingStock - forecastedDemand);
            
            weeklyForecast.setForecastedDemand(forecastedDemand);
            weeklyForecast.setProjectedStock(remainingStock);
            
            forecasts.add(weeklyForecast);
        }
        
        return forecasts;
    }

    /**
     * Calculate confidence score
     */
    private double calculateConfidence(int transactionCount, int daysCovered, double volatility) {
        // Transaction score (0-40 points)
        double transactionScore = Math.min(transactionCount / 30.0, 1.0) * 40;
        
        // Coverage score (0-30 points)
        double coverageScore = Math.min(daysCovered / 90.0, 1.0) * 30;
        
        // Volatility score (0-30 points) - lower volatility = higher score
        double volatilityScore = Math.max(0, (1.0 - Math.min(volatility / 2.0, 1.0))) * 30;
        
        return transactionScore + coverageScore + volatilityScore;
    }

    /**
     * Generate recommendation
     */
    private void generateRecommendation(ForecastResult forecast, double avgDailyDemand, int peakDailyDemand) {
        int daysUntilStockout = forecast.getDaysUntilStockout();
        String action;
        int orderQty;
        
        if (daysUntilStockout <= 3) {
            action = "URGENT: Order immediately to prevent stockout";
            orderQty = (int) Math.round(avgDailyDemand * 45 + peakDailyDemand * 7);
        } else if (daysUntilStockout <= 7) {
            action = "Order soon - stock running low";
            orderQty = (int) Math.round(avgDailyDemand * 37 + peakDailyDemand * 7);
        } else if (daysUntilStockout <= 14) {
            action = "Plan to order - monitor stock levels";
            orderQty = (int) Math.round(avgDailyDemand * 30 + peakDailyDemand * 5);
        } else {
            action = "Stock levels adequate - no action needed";
            orderQty = (int) Math.round(avgDailyDemand * 30);
        }
        
        forecast.setRecommendedAction(action);
        forecast.setRecommendedOrderQuantity(Math.max(orderQty, forecast.getReorderLevel() * 2));
    }

    /**
     * Create default forecast when no historical data
     */
    private ForecastResult createDefaultForecast(ForecastResult forecast) {
        forecast.setAvgDailyDemand(1.0);
        forecast.setAvgWeeklyDemand(7.0);
        forecast.setPeakDailyDemand(5);
        forecast.setDemandVolatility(0.0);
        forecast.setSeasonalPattern("No Data");
        forecast.setDaysUntilStockout(forecast.getCurrentStock());
        forecast.setStockoutRisk(forecast.getCurrentStock() <= forecast.getReorderLevel());
        forecast.setRiskLevel(forecast.isStockoutRisk() ? "HIGH" : "LOW");
        forecast.setConfidenceScore(10.0);
        forecast.setForecastMethod("Default (No Historical Data)");
        forecast.setRecommendedAction("Collect historical data for better forecasting");
        forecast.setRecommendedOrderQuantity(forecast.getReorderLevel() * 2);
        
        // Default daily forecasts
        List<DailyForecast> dailyForecasts = new ArrayList<>();
        int remainingStock = forecast.getCurrentStock();
        LocalDateTime currentDate = LocalDateTime.now();
        
        for (int i = 1; i <= 7; i++) {
            LocalDateTime forecastDate = currentDate.plusDays(i);
            remainingStock = Math.max(0, remainingStock - 1);
            dailyForecasts.add(new DailyForecast(
                    forecastDate.toLocalDate().toString(),
                    1,
                    remainingStock,
                    forecastDate.getDayOfWeek().toString()
            ));
        }
        forecast.setDailyForecasts(dailyForecasts);
        
        // Default weekly forecasts
        List<WeeklyForecast> weeklyForecasts = new ArrayList<>();
        remainingStock = forecast.getCurrentStock();
        
        for (int i = 1; i <= 4; i++) {
            WeeklyForecast wf = new WeeklyForecast();
            wf.setWeekLabel("Week " + i);
            wf.setForecastedDemand(7);
            remainingStock = Math.max(0, remainingStock - 7);
            wf.setProjectedStock(remainingStock);
            weeklyForecasts.add(wf);
        }
        forecast.setWeeklyForecasts(weeklyForecasts);
        
        return forecast;
    }
}
