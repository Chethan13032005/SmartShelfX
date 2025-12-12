package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.service.RecommendationService;
import com.infosys.smartshelfx_backend.service.AIRestockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private AIRestockService aiRestockService;

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Get simple low-stock product list (original feature)
     */
    @GetMapping("/restock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Inventory>> getRestockRecommendations() {
        List<Inventory> recommendations = recommendationService.getRestockSuggestions();
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get AI-powered predictive restock recommendations
     * Analyzes historical consumption patterns and provides intelligent predictions
     */
    @GetMapping("/ai-restock")
    public ResponseEntity<List<AIRestockService.RestockPrediction>> getAIRestockRecommendations() {
        try {
            List<AIRestockService.RestockPrediction> predictions = aiRestockService.getAIRestockRecommendations();
            return ResponseEntity.ok(predictions);
        } catch (Exception ex) {
            // Be resilient in UI: return an empty list instead of 500
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get AI prediction for a specific product
     */
    @GetMapping("/ai-restock/{productId}")
    public ResponseEntity<?> getProductAIPrediction(@PathVariable Long productId) {
        try {
            Inventory product = inventoryRepository.findById(productId).orElse(null);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            AIRestockService.RestockPrediction prediction = aiRestockService.analyzePredictiveRestock(product);
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating prediction: " + e.getMessage());
        }
    }
}
