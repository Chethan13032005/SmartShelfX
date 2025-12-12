package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.service.ForecastEngine;
import com.infosys.smartshelfx_backend.service.DemandForecastingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ForecastEngine forecastEngine;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private DemandForecastingService demandForecastingService;

    public static class TransactionData {
        public Long productId;
        public Double sold;
        public String date;
    }

    public static class ForecastResult {
        public Long productId;
        public Double predicted;
        public String forecastDate;
        public ForecastResult(Long productId, Double predicted, String forecastDate) {
            this.productId = productId; this.predicted = predicted; this.forecastDate = forecastDate;
        }
    }

    @PostMapping("/run")
    public Map<String, Object> runForecast() {
        Map<String, Object> response = new HashMap<>();
        try {
            String sql = "SELECT product_id AS productId, SUM(quantity) AS sold, DATE(created_at) AS date " +
                    "FROM stock_transactions WHERE type='OUT' GROUP BY product_id, DATE(created_at)";

            List<TransactionData> rows = jdbcTemplate.query(sql, (rs, i) -> {
                TransactionData t = new TransactionData();
                t.productId = rs.getLong("productId");
                t.sold = rs.getDouble("sold");
                t.date = rs.getString("date");
                return t;
            });

            Map<Long, List<TransactionData>> grouped = new HashMap<>();
            for (TransactionData t : rows) {
                grouped.computeIfAbsent(t.productId, k -> new ArrayList<>()).add(t);
            }

            List<ForecastResult> results = new ArrayList<>();
            var forecastDate = java.time.LocalDateTime.now().plusDays(7); // use datetime for MySQL DATETIME

            if (!grouped.isEmpty()) {
                for (Map.Entry<Long, List<TransactionData>> e : grouped.entrySet()) {
                    double prediction = forecastEngine.predictNext(e.getValue());
                    ForecastResult r = new ForecastResult(e.getKey(), prediction, forecastDate.toLocalDate().toString());
                    results.add(r);

                    // Persist using Timestamp to match DATETIME
                    jdbcTemplate.update(
                            "INSERT INTO forecast_results (product_id, forecast_qty, forecast_date, created_at) VALUES (?, ?, ?, NOW())",
                            e.getKey(), prediction, java.sql.Timestamp.valueOf(forecastDate)
                    );
                }
                response.put("message", "Forecast complete");
            } else {
                // No sales history – return placeholder forecasts for visible products so UI is not empty
                List<Inventory> products = inventoryRepository.findAll();
                for (Inventory p : products) {
                    results.add(new ForecastResult(p.getId(), 0.0, forecastDate.toLocalDate().toString()));
                }
                response.put("message", "No sales data found; returning placeholder forecasts");
            }

            response.put("message", "Forecast complete");
            response.put("forecast", results);
        } catch (Exception ex) {
            response.put("message", "Forecast failed");
            response.put("error", ex.getMessage());
        }
        return response;
    }

    /**
     * Detailed demand forecasts using historical OUT transactions and analytics
     */
    @GetMapping("/detailed")
    public List<DemandForecastingService.ForecastResult> getDetailedForecasts() {
        try {
            return demandForecastingService.getAllDemandForecasts();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Aggregated predictions for dashboard chart
     * Returns daily aggregated demand across all products for next 7 days
     */
    @GetMapping("/predictions")
    public List<Map<String, Object>> getPredictions() {
        try {
            List<DemandForecastingService.ForecastResult> forecasts = demandForecastingService.getAllDemandForecasts();
            
            if (forecasts.isEmpty()) {
                return Collections.emptyList();
            }

            // Aggregate daily forecasts across all products
            Map<String, Integer> aggregatedDemand = new HashMap<>();
            
            for (DemandForecastingService.ForecastResult forecast : forecasts) {
                if (forecast.getDailyForecasts() != null) {
                    for (DemandForecastingService.DailyForecast daily : forecast.getDailyForecasts()) {
                        aggregatedDemand.merge(daily.getDate(), daily.getForecastedDemand(), Integer::sum);
                    }
                }
            }

            // Convert to chart format with sorted dates
            List<Map<String, Object>> chartData = new ArrayList<>();
            aggregatedDemand.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        Map<String, Object> dataPoint = new HashMap<>();
                        dataPoint.put("date", entry.getKey());
                        dataPoint.put("predictedDemand", entry.getValue());
                        chartData.add(dataPoint);
                    });

            return chartData;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
 