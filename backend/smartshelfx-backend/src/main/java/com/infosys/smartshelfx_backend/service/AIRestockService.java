package com.infosys.smartshelfx_backend.service;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.model.StockTransaction;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI-Based Predictive Restock Service
 * Analyzes historical consumption patterns to predict optimal restock quantities and timing
 */
@Service
public class AIRestockService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    /**
     * AI Prediction Model for a single product
     */
    public static class RestockPrediction {
        private Long productId;
        private String productName;
        private String sku;
        private Integer currentStock;
        private Integer reorderLevel;
        private Integer recommendedQuantity;
        private Double consumptionRate; // units per day
        private Integer daysUntilStockout;
        private String urgency; // CRITICAL, HIGH, MEDIUM, LOW
        private Double confidence; // 0-100%
        private Map<String, Object> analytics;

        // Getters and Setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public Integer getCurrentStock() { return currentStock; }
        public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

        public Integer getReorderLevel() { return reorderLevel; }
        public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

        public Integer getRecommendedQuantity() { return recommendedQuantity; }
        public void setRecommendedQuantity(Integer recommendedQuantity) { this.recommendedQuantity = recommendedQuantity; }

        public Double getConsumptionRate() { return consumptionRate; }
        public void setConsumptionRate(Double consumptionRate) { this.consumptionRate = consumptionRate; }

        public Integer getDaysUntilStockout() { return daysUntilStockout; }
        public void setDaysUntilStockout(Integer daysUntilStockout) { this.daysUntilStockout = daysUntilStockout; }

        public String getUrgency() { return urgency; }
        public void setUrgency(String urgency) { this.urgency = urgency; }

        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }

        public Map<String, Object> getAnalytics() { return analytics; }
        public void setAnalytics(Map<String, Object> analytics) { this.analytics = analytics; }
    }

    /**
     * Get AI-powered restock recommendations for all low-stock products
     */
    public List<RestockPrediction> getAIRestockRecommendations() {
        try {
            List<Inventory> lowStockProducts = inventoryRepository.findLowStockProducts();
            
            if (lowStockProducts.isEmpty()) {
                // If no low stock products found with <= check, also check for products below 50% of reorder level
                List<Inventory> allProducts = inventoryRepository.findAll();
                lowStockProducts = allProducts.stream()
                    .filter(p -> {
                        int reorder = p.getReorderLevel() != null ? p.getReorderLevel() : 10;
                        return p.getQuantity() <= reorder;
                    })
                    .toList();
            }
            
            return lowStockProducts.stream()
                    .map(this::analyzePredictiveRestock)
                    .sorted((a, b) -> {
                        // Sort by urgency: CRITICAL > HIGH > MEDIUM > LOW
                        Map<String, Integer> urgencyMap = Map.of(
                            "CRITICAL", 4,
                            "HIGH", 3,
                            "MEDIUM", 2,
                            "LOW", 1
                        );
                        return urgencyMap.getOrDefault(b.getUrgency(), 0)
                                .compareTo(urgencyMap.getOrDefault(a.getUrgency(), 0));
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Log error and return empty list as fallback
            System.err.println("Error in getAIRestockRecommendations: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * AI Analysis for a single product using historical data
     */
    public RestockPrediction analyzePredictiveRestock(Inventory product) {
        RestockPrediction prediction = new RestockPrediction();
        prediction.setProductId(product.getId());
        prediction.setProductName(Optional.ofNullable(product.getName()).orElse(""));
        prediction.setSku(Optional.ofNullable(product.getSku()).orElse(""));
        prediction.setCurrentStock(product.getQuantity());
        int safeReorderLevel = Optional.ofNullable(product.getReorderLevel()).orElse(10);
        prediction.setReorderLevel(safeReorderLevel);

        // Get historical transactions for this product (last 90 days)
        LocalDateTime startDate = LocalDateTime.now().minusDays(90);
        List<StockTransaction> transactions = stockTransactionRepository
                .findByProductIdAndCreatedAtAfter(product.getId(), startDate);

        // Analyze consumption patterns
        Map<String, Object> analytics = analyzeConsumptionPattern(transactions, product);
        prediction.setAnalytics(analytics);

        // Calculate consumption rate (units per day)
        Double consumptionRate = (Double) analytics.get("averageDailyConsumption");
        prediction.setConsumptionRate(consumptionRate);

        // Calculate days until stockout
        Integer daysUntilStockout = calculateDaysUntilStockout(product.getQuantity(), consumptionRate);
        prediction.setDaysUntilStockout(daysUntilStockout);

        // Determine urgency level
        String urgency = determineUrgency(daysUntilStockout, product.getQuantity(), safeReorderLevel);
        prediction.setUrgency(urgency);

        // Calculate recommended quantity using AI algorithm
        Integer recommendedQty = calculateAIRecommendedQuantity(
                consumptionRate, 
            safeReorderLevel,
                (Integer) analytics.get("peakDailyConsumption"),
                (Double) analytics.get("trendFactor")
        );
        prediction.setRecommendedQuantity(recommendedQty);

        // Calculate confidence level
        Double confidence = calculatePredictionConfidence(transactions, analytics);
        prediction.setConfidence(confidence);

        return prediction;
    }

    /**
     * Analyze consumption patterns from historical data
     */
    private Map<String, Object> analyzeConsumptionPattern(List<StockTransaction> transactions, Inventory product) {
        Map<String, Object> analytics = new HashMap<>();

        // Filter only OUT transactions
        List<StockTransaction> outTransactions = transactions.stream()
                .filter(t -> "OUT".equalsIgnoreCase(t.getType()))
                .sorted(Comparator.comparing(StockTransaction::getCreatedAt))
                .collect(Collectors.toList());

        if (outTransactions.isEmpty()) {
            // No historical data - use defaults
            analytics.put("averageDailyConsumption", 1.0);
            analytics.put("peakDailyConsumption", 5);
            analytics.put("totalConsumption", 0);
            analytics.put("transactionCount", 0);
            analytics.put("trendFactor", 1.0); // stable
            analytics.put("volatility", 0.0);
            analytics.put("dataQuality", "LOW");
            return analytics;
        }

        // Calculate total consumption
        int totalConsumption = outTransactions.stream()
                .mapToInt(StockTransaction::getQuantity)
                .sum();

        // Calculate time span
        LocalDateTime firstTransaction = outTransactions.get(0).getCreatedAt();
        LocalDateTime lastTransaction = outTransactions.get(outTransactions.size() - 1).getCreatedAt();
        long daysBetween = ChronoUnit.DAYS.between(firstTransaction, lastTransaction);
        if (daysBetween == 0) daysBetween = 1; // Avoid division by zero

        // Average daily consumption
        double avgDailyConsumption = (double) totalConsumption / daysBetween;

        // Group by day and find peak consumption
        Map<String, Integer> dailyConsumption = new HashMap<>();
        for (StockTransaction txn : outTransactions) {
            String date = txn.getCreatedAt().toLocalDate().toString();
            dailyConsumption.merge(date, txn.getQuantity(), Integer::sum);
        }
        
        int peakDailyConsumption = dailyConsumption.values().stream()
                .max(Integer::compareTo)
                .orElse((int) Math.ceil(avgDailyConsumption * 2));

        // Calculate trend (are we consuming more over time?)
        double trendFactor = calculateTrendFactor(outTransactions);

        // Calculate volatility (how much does consumption vary?)
        double volatility = calculateVolatility(dailyConsumption.values(), avgDailyConsumption);

        // Data quality assessment
        String dataQuality = assessDataQuality(outTransactions.size(), daysBetween);

        analytics.put("averageDailyConsumption", Math.max(0.1, avgDailyConsumption));
        analytics.put("peakDailyConsumption", peakDailyConsumption);
        analytics.put("totalConsumption", totalConsumption);
        analytics.put("transactionCount", outTransactions.size());
        analytics.put("trendFactor", trendFactor);
        analytics.put("volatility", volatility);
        analytics.put("daysCovered", daysBetween);
        analytics.put("dataQuality", dataQuality);

        return analytics;
    }

    /**
     * Calculate trend factor (1.0 = stable, >1.0 = increasing, <1.0 = decreasing)
     */
    private double calculateTrendFactor(List<StockTransaction> transactions) {
        if (transactions.size() < 3) return 1.0;

        // Split into two halves and compare average consumption
        int midPoint = transactions.size() / 2;
        List<StockTransaction> firstHalf = transactions.subList(0, midPoint);
        List<StockTransaction> secondHalf = transactions.subList(midPoint, transactions.size());

        double firstHalfAvg = firstHalf.stream()
                .mapToInt(StockTransaction::getQuantity)
                .average()
                .orElse(1.0);

        double secondHalfAvg = secondHalf.stream()
                .mapToInt(StockTransaction::getQuantity)
                .average()
                .orElse(1.0);

        if (firstHalfAvg == 0) return 1.0;
        
        double trend = secondHalfAvg / firstHalfAvg;
        // Cap trend between 0.5 and 2.0
        return Math.max(0.5, Math.min(2.0, trend));
    }

    /**
     * Calculate volatility (standard deviation / mean)
     */
    private double calculateVolatility(Collection<Integer> values, double mean) {
        if (values.size() < 2) return 0.0;

        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);
        return mean > 0 ? stdDev / mean : 0.0;
    }

    /**
     * Assess quality of historical data
     */
    private String assessDataQuality(int transactionCount, long daysCovered) {
        if (transactionCount >= 20 && daysCovered >= 30) return "HIGH";
        if (transactionCount >= 10 && daysCovered >= 14) return "MEDIUM";
        return "LOW";
    }

    /**
     * Calculate days until stockout based on consumption rate
     */
    private Integer calculateDaysUntilStockout(int currentStock, double consumptionRate) {
        if (consumptionRate <= 0) return 999; // No consumption pattern
        return (int) Math.floor(currentStock / consumptionRate);
    }

    /**
     * Determine urgency level based on days until stockout
     */
    private String determineUrgency(int daysUntilStockout, int currentStock, int reorderLevel) {
        if (daysUntilStockout <= 3 || currentStock < reorderLevel / 2) return "CRITICAL";
        if (daysUntilStockout <= 7 || currentStock < reorderLevel) return "HIGH";
        if (daysUntilStockout <= 14) return "MEDIUM";
        return "LOW";
    }

    /**
     * AI Algorithm: Calculate optimal restock quantity
     * Considers: consumption rate, trend, peak demand, safety stock
     */
    private Integer calculateAIRecommendedQuantity(
            double avgDailyConsumption,
            int reorderLevel,
            int peakDailyConsumption,
            double trendFactor
    ) {
        // Base calculation: 30 days supply
        int leadTimeDays = 7; // Assume 7 days lead time for delivery
        int reviewPeriodDays = 30; // 30 days until next review
        int safetyStockDays = 7; // 7 days safety buffer

        // Calculate base quantity for normal consumption
        double baseQuantity = avgDailyConsumption * (leadTimeDays + reviewPeriodDays);

        // Add safety stock for peak demand
        double safetyStock = peakDailyConsumption * safetyStockDays;

        // Adjust for trend (if consumption is increasing, order more)
        double trendAdjustment = baseQuantity * (trendFactor - 1.0) * 0.5;

        // Total recommended quantity
        double totalQuantity = baseQuantity + safetyStock + trendAdjustment;

        // Ensure minimum of 2x reorder level
        int minQuantity = reorderLevel * 2;
        
        // Round up to nearest 5 for easier handling
        int recommendedQty = (int) Math.ceil(totalQuantity / 5.0) * 5;

        return Math.max(recommendedQty, minQuantity);
    }

    /**
     * Calculate confidence level in prediction (0-100%)
     */
    private Double calculatePredictionConfidence(List<StockTransaction> transactions, Map<String, Object> analytics) {
        double confidence = 50.0; // Base confidence

        // More transactions = higher confidence (null-safe)
        Object txnObj = analytics.get("transactionCount");
        int txnCount = txnObj instanceof Number ? ((Number) txnObj).intValue() : transactions.size();
        if (txnCount >= 30) confidence += 20.0;
        else if (txnCount >= 15) confidence += 10.0;
        else if (txnCount >= 5) confidence += 5.0;
        else confidence -= 10.0;

        // More days covered = higher confidence (null-safe)
        Object daysObj = analytics.get("daysCovered");
        long daysCovered = daysObj instanceof Number ? ((Number) daysObj).longValue() : 0L;
        if (daysCovered >= 60) confidence += 15.0;
        else if (daysCovered >= 30) confidence += 10.0;
        else if (daysCovered >= 14) confidence += 5.0;
        else confidence -= 10.0;

        // Lower volatility = higher confidence (null-safe, assume moderate)
        Object volObj = analytics.get("volatility");
        double volatility = volObj instanceof Number ? ((Number) volObj).doubleValue() : 0.5;
        if (volatility < 0.3) confidence += 10.0;
        else if (volatility > 0.7) confidence -= 10.0;

        // Data quality (null-safe)
        String dataQuality = analytics.get("dataQuality") instanceof String ? (String) analytics.get("dataQuality") : "MEDIUM";
        if ("HIGH".equalsIgnoreCase(dataQuality)) confidence += 5.0;
        else if ("LOW".equalsIgnoreCase(dataQuality)) confidence -= 15.0;

        return Math.max(0.0, Math.min(100.0, confidence));
    }
}

