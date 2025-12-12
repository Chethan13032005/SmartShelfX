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
@RequestMapping("/api/ai-restock")
public class RestockController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private AIRestockService aiRestockService;

    @Autowired
    private UserRepository userRepository;

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
        public String aiAnalysis;
    }

    public static class ModifyRequest {
        public List<ModifiedItem> items;

        public static class ModifiedItem {
            public Long productId;
            public Integer quantity;
            public Long vendorId;
            public Boolean remove;
        }
    }

    /**
     * GET /api/restock/suggestions
     * Returns AI-powered restock suggestions with vendor information
     */
    @GetMapping("/suggestions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> suggestions(Authentication authentication) {
        try {
            // Get AI predictions
            List<AIRestockService.RestockPrediction> predictions = aiRestockService.getAIRestockRecommendations();
            
            // Get all vendors for assignment (case-insensitive to handle ROLE naming)
            List<User> vendors = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase("VENDOR"))
                    .collect(Collectors.toList());
            boolean hasVendors = !vendors.isEmpty();

            // Convert AI predictions to suggestions with vendor assignment
            List<Suggestion> suggestions = predictions.stream().map(pred -> {
                Suggestion s = new Suggestion();
                s.productId = pred.getProductId();
                s.productName = pred.getProductName();
                s.sku = pred.getSku();
                s.currentStock = pred.getCurrentStock();
                s.reorderLevel = pred.getReorderLevel();
                s.recommendedOrderQuantity = pred.getRecommendedQuantity();
                s.urgency = pred.getUrgency();
                s.riskLevel = pred.getUrgency(); // Keep for backwards compatibility
                s.consumptionRate = pred.getConsumptionRate();
                s.confidence = pred.getConfidence();
                
                // Generate AI analysis based on stock levels and business rules
                s.aiAnalysis = generateAIAnalysis(pred);
                
                // Assign vendor (round-robin). If none exist, mark unassigned but do not block suggestions.
                User assignedVendor = hasVendors
                    ? vendors.get(Math.abs(pred.getProductId().hashCode()) % vendors.size())
                    : null;
                s.vendorId = assignedVendor != null ? assignedVendor.getId() : null;
                s.vendorName = assignedVendor != null ? assignedVendor.getFullName() : "Unassigned";
                s.vendorEmail = assignedVendor != null ? assignedVendor.getEmail() : null;
                
                return s;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "suggestions", suggestions,
                "totalItems", suggestions.size(),
                "criticalCount", suggestions.stream().filter(s -> "CRITICAL".equals(s.urgency)).count(),
                "highCount", suggestions.stream().filter(s -> "HIGH".equals(s.urgency)).count()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch restock suggestions: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/restock/modify
     * Allows admin to modify restock suggestions (quantity, vendor, or remove items)
     */
    @PutMapping("/modify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> modifySuggestions(@RequestBody ModifyRequest request, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByEmail(username).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }

            String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
            if (!(role.equals("manager") || role.equals("admin"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only Managers or Admins can modify restock suggestions"));
            }

            // Validate and process modifications
            List<Map<String, Object>> processedItems = new ArrayList<>();
            for (ModifyRequest.ModifiedItem item : request.items) {
                if (Boolean.TRUE.equals(item.remove)) {
                    continue; // Skip removed items
                }

                // Validate product exists
                Inventory product = inventoryRepository.findById(item.productId).orElse(null);
                if (product == null) {
                    continue; // Skip invalid products
                }

                // Validate vendor if specified
                User vendor = null;
                if (item.vendorId != null) {
                    vendor = userRepository.findById(item.vendorId).orElse(null);
                    if (vendor == null || !"Vendor".equalsIgnoreCase(vendor.getRole())) {
                        continue; // Skip if vendor invalid
                    }
                }

                Map<String, Object> processedItem = new HashMap<>();
                processedItem.put("productId", item.productId);
                processedItem.put("productName", product.getName());
                processedItem.put("quantity", item.quantity != null ? item.quantity : 0);
                processedItem.put("vendorId", vendor != null ? vendor.getId() : null);
                processedItem.put("vendorName", vendor != null ? vendor.getFullName() : null);
                processedItems.add(processedItem);
            }

            return ResponseEntity.ok(Map.of(
                "message", "Suggestions modified successfully",
                "modifiedItems", processedItems,
                "count", processedItems.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to modify suggestions: " + e.getMessage()));
        }
    }

    private boolean tableExists(String table) {
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                    Integer.class,
                    table
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

    /**
     * Generate AI analysis explanation based on stock levels and business rules
     */
    private String generateAIAnalysis(AIRestockService.RestockPrediction pred) {
        int currentStock = pred.getCurrentStock();
        int reorderLevel = pred.getReorderLevel();
        double consumptionRate = pred.getConsumptionRate();
        int daysUntilStockout = pred.getDaysUntilStockout();
        
        StringBuilder analysis = new StringBuilder();
        
        // Stock level assessment
        if (currentStock == 0) {
            analysis.append("⚠️ OUT OF STOCK - Immediate action required. ");
        } else if (currentStock < reorderLevel / 2) {
            analysis.append("🔴 CRITICAL SHORTAGE - Stock is below 50% of reorder threshold. ");
        } else if (currentStock < reorderLevel) {
            analysis.append("🟠 BELOW REORDER THRESHOLD - Stock has fallen below the minimum safe level. ");
        } else if (currentStock <= reorderLevel * 1.2) {
            analysis.append("🟡 APPROACHING THRESHOLD - Stock is within 20% of reorder level. ");
        }
        
        // Days until stockout analysis
        if (daysUntilStockout <= 3) {
            analysis.append(String.format("Stock will deplete in %d days at current consumption rate. ", daysUntilStockout));
        } else if (daysUntilStockout <= 7) {
            analysis.append(String.format("Stock will last approximately %d days. ", daysUntilStockout));
        } else if (daysUntilStockout <= 14) {
            analysis.append(String.format("Estimated %d days of stock remaining. ", daysUntilStockout));
        }
        
        // Consumption pattern insights
        if (pred.getAnalytics() != null) {
            Map<String, Object> analytics = pred.getAnalytics();
            Double trendFactor = (Double) analytics.get("trendFactor");
            
            if (trendFactor != null) {
                if (trendFactor > 1.2) {
                    analysis.append("📈 Demand is increasing significantly (+").append(String.format("%.0f%%", (trendFactor - 1) * 100)).append("). ");
                } else if (trendFactor > 1.1) {
                    analysis.append("📈 Demand is trending upward. ");
                } else if (trendFactor < 0.8) {
                    analysis.append("📉 Demand is decreasing. ");
                }
            }
            
            String dataQuality = (String) analytics.get("dataQuality");
            if ("LOW".equals(dataQuality)) {
                analysis.append("⚠️ Limited historical data - prediction confidence may be lower. ");
            } else if ("HIGH".equals(dataQuality)) {
                analysis.append("✓ Based on comprehensive historical data. ");
            }
        }
        
        // Recommendation reasoning
        int recommendedQty = pred.getRecommendedQuantity();
        if (recommendedQty > reorderLevel * 3) {
            analysis.append(String.format("AI recommends %d units to ensure adequate buffer for peak demand and lead time.", recommendedQty));
        } else if (recommendedQty > reorderLevel * 2) {
            analysis.append(String.format("AI recommends %d units to cover projected demand and safety stock.", recommendedQty));
        } else {
            analysis.append(String.format("AI recommends %d units to restore stock to safe levels.", recommendedQty));
        }
        
        return analysis.toString().trim();
    }
}
