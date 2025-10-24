package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatsController {

    private final InventoryRepository inventoryRepository;

    public StatsController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> getStats() {
        long totalProducts = inventoryRepository.count();
        long lowStock = inventoryRepository.countByQuantityLessThanEqual(5);
        long suppliers = inventoryRepository.countDistinctSuppliers();
        return Map.of(
                "totalProducts", totalProducts,
                "lowStock", lowStock,
                "suppliers", suppliers
        );
    }
}
