package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.model.StockTransaction;
import com.infosys.smartshelfx_backend.model.User;
import com.infosys.smartshelfx_backend.model.PurchaseOrder;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.repository.PurchaseOrderRepository;
import com.infosys.smartshelfx_backend.repository.StockTransactionRepository;
import com.infosys.smartshelfx_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {
    private final StockTransactionRepository transactionRepo;
    private final InventoryRepository inventoryRepo;
    private final UserRepository userRepo;
    private final PurchaseOrderRepository purchaseOrderRepo;

    public StockController(StockTransactionRepository transactionRepo, 
                          InventoryRepository inventoryRepo,
                          UserRepository userRepo,
                          PurchaseOrderRepository purchaseOrderRepo) {
        this.transactionRepo = transactionRepo;
        this.inventoryRepo = inventoryRepo;
        this.userRepo = userRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
    }

    /**
     * POST /api/stock/in
     * Access: Manager, Admin only
     * Record Stock-IN (receiving new inventory)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/in")
	@Transactional
    public ResponseEntity<?> recordStockIn(@RequestBody Map<String, Object> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";

        // Check role
        User user = userRepo.findByEmail(username).orElse(null);
        String role = user != null && user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (user == null || !(role.equals("admin") || role.equals("manager"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only Admin and Manager can record stock transactions");
        }

        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String notes = request.get("notes") != null ? request.get("notes").toString() : "";

            // Find product and update quantity
            Inventory product = inventoryRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setQuantity(product.getQuantity() + quantity);
            product.setUpdatedBy(username);
            product.setUpdatedAt(LocalDateTime.now());
            inventoryRepo.save(product);

            // Create transaction record
            StockTransaction transaction = new StockTransaction();
            transaction.setProductId(productId);
            transaction.setType("IN");
            transaction.setQuantity(quantity);
            transaction.setNotes(notes);
            transaction.setPerformedBy(username);
            transactionRepo.save(transaction);

            return ResponseEntity.ok(Map.of(
                "message", "Stock-IN recorded successfully",
                "transaction", transaction,
                "newQuantity", product.getQuantity()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error recording stock-in: " + e.getMessage());
        }
    }

    /**
     * POST /api/stock/out
     * Access: Manager, Admin only
     * Record Stock-OUT (sales or dispatch)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/out")
	@Transactional
    public ResponseEntity<?> recordStockOut(@RequestBody Map<String, Object> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";

        // Check role
        User user = userRepo.findByEmail(username).orElse(null);
        String role = user != null && user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (user == null || !(role.equals("admin") || role.equals("manager"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only Admin and Manager can record stock transactions");
        }

        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String notes = request.get("notes") != null ? request.get("notes").toString() : "";

            // Find product and update quantity
            Inventory product = inventoryRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getQuantity() < quantity) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Insufficient stock. Available: " + product.getQuantity());
            }

            product.setQuantity(product.getQuantity() - quantity);
            product.setUpdatedBy(username);
            product.setUpdatedAt(LocalDateTime.now());
            inventoryRepo.save(product);

            // Check if stock is below reorder level and issue a warning
            if (product.getQuantity() <= product.getReorderLevel()) {
                System.out.println("LOW STOCK WARNING: Product '" + product.getName() + "' (ID: " + product.getId() + ") has fallen below the reorder level. Current quantity: " + product.getQuantity());
                autoGeneratePurchaseOrder(product, username);
            }

            // Create transaction record
            StockTransaction transaction = new StockTransaction();
            transaction.setProductId(productId);
            transaction.setType("OUT");
            transaction.setQuantity(quantity);
            transaction.setNotes(notes);
            transaction.setPerformedBy(username);
            transactionRepo.save(transaction);

            return ResponseEntity.ok(Map.of(
                "message", "Stock-OUT recorded successfully",
                "transaction", transaction,
                "newQuantity", product.getQuantity()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error recording stock-out: " + e.getMessage());
        }
    }

    /**
     * GET /api/stock/transactions
     * Access: Manager, Admin
     * Get all stock transactions
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";

        User user = userRepo.findByEmail(username).orElse(null);
        String role = user != null && user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (user == null || !(role.equals("admin") || role.equals("manager"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only Admin and Manager can view stock transactions");
        }

        return ResponseEntity.ok(transactionRepo.findAll());
    }

    /**
     * GET /api/stock/recent
     * Access: Manager, Admin
     * Get recent 10 stock transactions for dashboard
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentTransactions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";

        User user = userRepo.findByEmail(username).orElse(null);
        String role = user != null && user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (user == null || !(role.equals("admin") || role.equals("manager"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied");
        }

        return ResponseEntity.ok(transactionRepo.findTop10ByOrderByCreatedAtDesc());
    }

    private void autoGeneratePurchaseOrder(Inventory product, String performedBy) {
        if (product.getVendor() == null || product.getVendor().isEmpty()) {
            System.out.println("ERROR: Cannot auto-generate PO for product '" + product.getName() + "' (ID: " + product.getId() + "). No vendor specified.");
            return;
        }

        userRepo.findByEmail(product.getVendor()).ifPresentOrElse(vendor -> {
            PurchaseOrder po = new PurchaseOrder();
            po.setVendorId(vendor.getId());
            po.setVendorEmail(vendor.getEmail());
            po.setProductId(product.getId());
            po.setProductName(product.getName());
            po.setQuantity(product.getReorderQuantity());
            po.setStatus("PENDING");
            po.setCreatedBy(performedBy);
            purchaseOrderRepo.save(po);
            System.out.println("Auto-generated Purchase Order for product '" + product.getName() + "' (ID: " + product.getId() + ").");
        }, () -> {
            System.out.println("ERROR: Cannot auto-generate PO for product '" + product.getName() + "' (ID: " + product.getId() + "). Vendor not found: " + product.getVendor());
        });
    }
}
