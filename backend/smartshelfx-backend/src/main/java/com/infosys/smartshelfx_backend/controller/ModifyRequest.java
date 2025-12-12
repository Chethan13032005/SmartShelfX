package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.model.User;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.repository.UserRepository;
import com.infosys.smartshelfx_backend.service.AIRestockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restock")
public class ModifyRequest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private AIRestockService aiRestockService;

    @Autowired
    private UserRepository userRepository;

    // Suggestion DTO
    public static class Suggestion {
        public Long productId;
        public String productName;
        public String sku;
        public int currentStock;
        public int reorderLevel;
        public int recommendedOrderQuantity;
        public String riskLevel;
        public String urgency;
        public Long vendorId;
        public String vendorName;
        public String vendorEmail;
        public Double consumptionRate;
        public Double confidence;
    }

    // FIXED: renamed inner DTO to avoid duplicate class name
    public static class ModifyPayload {
        public List<ModifiedItem> items;

        public static class ModifiedItem {
            public Long productId;
            public Integer quantity;
            public Long vendorId;
            public Boolean remove;
        }
    }

    @GetMapping("/suggestions")
    public List<Suggestion> suggestions() {
        List<Inventory> products = inventoryRepository.findAll();
        List<Suggestion> list = new ArrayList<>();
        
        for (Inventory p : products) {
            Suggestion s = new Suggestion();
            s.productId = p.getId();
            s.productName = p.getName() != null ? p.getName() : p.getProductName();
            s.sku = p.getSku();
            s.currentStock = p.getQuantity();
            s.reorderLevel = Optional.ofNullable(p.getReorderLevel()).orElse(0);
            list.add(s);
        }

        Map<Long, Double> forecastMap = new HashMap<>();
        if (tableExists("forecast_results")) {
            try {
                String forecastSql = "SELECT product_id, MAX(forecast_qty) AS qty FROM forecast_results GROUP BY product_id";
                var entries = jdbcTemplate.query(forecastSql, 
                    (rs, i) -> Map.entry(rs.getLong("product_id"), rs.getDouble("qty")));
                for (var e : entries) forecastMap.put(e.getKey(), e.getValue());
            } catch (Exception ignore) { }
        }

        for (Suggestion s : list) {
            double forecast = forecastMap.getOrDefault(s.productId, 0.0);
            int deficit = (int) Math.max(0, Math.round(forecast) - s.currentStock);
            int belowReorder = s.reorderLevel > 0 && s.currentStock < s.reorderLevel 
                                ? (s.reorderLevel - s.currentStock) : 0;

            s.recommendedOrderQuantity = Math.max(deficit, belowReorder);
            s.riskLevel = s.currentStock <= Math.max(1, s.reorderLevel / 2)
                    ? "CRITICAL"
                    : (s.currentStock <= s.reorderLevel ? "HIGH" : (deficit > 0 ? "MEDIUM" : "LOW"));
        }

        list.sort(
            Comparator.comparing((Suggestion s) -> riskRank(s.riskLevel))
                    .thenComparing((Suggestion s) -> -s.recommendedOrderQuantity)
        );

        return list;
    }

    private boolean tableExists(String table) {
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                    Integer.class, table
            );
            return cnt != null && cnt > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private int riskRank(String risk) {
        switch (risk) {
            case "CRITICAL": return 0;
            case "HIGH": return 1;
            case "MEDIUM": return 2;
            default: return 3;
        }
    }
}
