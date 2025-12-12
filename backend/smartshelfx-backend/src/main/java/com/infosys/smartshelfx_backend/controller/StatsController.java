package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.PurchaseOrder;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.repository.PurchaseOrderRepository;
import com.infosys.smartshelfx_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Admin Dashboard Stats
     */
        @GetMapping("/admin")
        public ResponseEntity<?> getAdminStats(Authentication authentication) {

        long totalProducts = inventoryRepository.count();
        long totalVendors = userRepository.countByRole("Vendor");
        long totalManagers = userRepository.countByRole("Manager");
        
        // Active POs: PENDING, APPROVED, ACCEPTED, DISPATCHED
        long activePOs = purchaseOrderRepository.findByStatusOrderByCreatedAtDesc("PENDING").size()
                + purchaseOrderRepository.findByStatusOrderByCreatedAtDesc("APPROVED").size()
                + purchaseOrderRepository.findByStatusOrderByCreatedAtDesc("ACCEPTED").size()
                + purchaseOrderRepository.findByStatusOrderByCreatedAtDesc("DISPATCHED").size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", totalProducts);
        stats.put("totalVendors", totalVendors);
        stats.put("totalManagers", totalManagers);
        stats.put("activePurchaseOrders", activePOs);
        stats.put("totalUsers", userRepository.count());

        return ResponseEntity.ok(stats);
    }

    /**
     * Manager Dashboard Stats
     */
        @GetMapping("/manager")
        public ResponseEntity<?> getManagerStats(Authentication authentication) {
                String username = authentication != null ? authentication.getName() : null;

        long totalSKUs = inventoryRepository.count();
        
        // Low stock items (quantity <= 10)
        long lowStockItems = inventoryRepository.findAll().stream()
                .filter(inv -> inv.getQuantity() <= 10)
                .count();

        // Pending POs created by current user
        long pendingPOs = (username != null ? purchaseOrderRepository.findByCreatedByOrderByCreatedAtDesc(username) : Collections.<PurchaseOrder>emptyList()).stream()
                .filter(po -> po.getStatus().equals("PENDING"))
                .count();

        // Stock value (sum of price * quantity)
        BigDecimal stockValue = inventoryRepository.findAll().stream()
                .map(inv -> inv.getPrice().multiply(BigDecimal.valueOf(inv.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSKUs", totalSKUs);
        stats.put("lowStockItems", lowStockItems);
        stats.put("pendingPurchaseOrders", pendingPOs);
        stats.put("stockValue", stockValue);

        return ResponseEntity.ok(stats);
    }

    /**
     * Vendor Dashboard Stats
     */
        @GetMapping("/vendor")
        public ResponseEntity<?> getVendorStats(Authentication authentication) {
                String username = authentication != null ? authentication.getName() : null;
                var vendorPOs = (username != null ? purchaseOrderRepository.findByVendorEmailOrderByCreatedAtDesc(username) : Collections.<PurchaseOrder>emptyList());

        long pendingOrders = vendorPOs.stream()
                .filter(po -> po.getStatus().equals("PENDING") || po.getStatus().equals("APPROVED"))
                .count();

        long dispatchedOrders = vendorPOs.stream()
                .filter(po -> po.getStatus().equals("DISPATCHED"))
                .count();

        long completedOrders = vendorPOs.stream()
                .filter(po -> po.getStatus().equals("COMPLETED"))
                .count();

        // Simple on-time percentage calculation
        // For now, just percentage of completed orders
        double ontimePercentage = vendorPOs.isEmpty() ? 0 : 
                (completedOrders * 100.0 / vendorPOs.size());

        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingOrders", pendingOrders);
        stats.put("dispatchedOrders", dispatchedOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("ontimePercentage", Math.round(ontimePercentage));
        stats.put("totalOrders", vendorPOs.size());

        return ResponseEntity.ok(stats);
    }
}
