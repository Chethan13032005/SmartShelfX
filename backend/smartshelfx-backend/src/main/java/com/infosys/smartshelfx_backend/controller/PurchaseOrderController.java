package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.model.PurchaseOrder;
import com.infosys.smartshelfx_backend.model.User;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.repository.PurchaseOrderRepository;
import com.infosys.smartshelfx_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * CREATE Purchase Order (Manager only)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPurchaseOrder(@RequestBody Map<String, Object> payload, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (!(role.equals("manager") || role.equals("admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Managers can create Purchase Orders"));
        }

        try {
            Long vendorId = Long.valueOf(payload.get("vendorId").toString());
            Long productId = Long.valueOf(payload.get("productId").toString());
            Integer quantity = Integer.valueOf(payload.get("quantity").toString());
            
            // Fetch vendor details
            User vendor = userRepository.findById(vendorId).orElse(null);
            if (vendor == null || !vendor.getRole().equalsIgnoreCase("Vendor")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid vendor ID"));
            }

            // Fetch product details
            Inventory product = inventoryRepository.findById(productId).orElse(null);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Product not found"));
            }

            PurchaseOrder po = new PurchaseOrder();
            po.setVendorId(vendorId);
            po.setVendorEmail(vendor.getEmail());
            po.setProductId(productId);
            po.setProductName(product.getName());
            po.setQuantity(quantity);
            po.setStatus("PENDING");
            po.setCreatedBy(username);
            
            if (payload.containsKey("deliveryDate")) {
                po.setDeliveryDate(LocalDateTime.parse(payload.get("deliveryDate").toString()));
            }
            if (payload.containsKey("notes")) {
                po.setNotes(payload.get("notes").toString());
            }

            PurchaseOrder savedPO = purchaseOrderRepository.save(po);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Purchase Order created successfully",
                            "purchaseOrder", savedPO
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create Purchase Order: " + e.getMessage()));
        }
    }

    /**
     * GET All Purchase Orders (role-filtered)
     * - Admin: sees all
     * - Manager: sees only created by them
     * - Vendor: sees only where vendorEmail matches
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllPurchaseOrders(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        List<PurchaseOrder> orders = new ArrayList<>();

        String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (role.equals("admin")) {
            // Admin sees all
            orders = purchaseOrderRepository.findAll();
        } else if (role.equals("manager")) {
            // Manager sees only their created POs
            orders = purchaseOrderRepository.findByCreatedByOrderByCreatedAtDesc(username);
        } else if (role.equals("vendor")) {
            // Vendor sees only POs assigned to them
            orders = purchaseOrderRepository.findByVendorEmailOrderByCreatedAtDesc(username);
        }

        return ResponseEntity.ok(orders);
    }

    /**
     * APPROVE Purchase Order (Admin only)
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> approvePurchaseOrder(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (!role.equals("admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admins can approve Purchase Orders"));
        }

        PurchaseOrder po = purchaseOrderRepository.findById(id).orElse(null);
        if (po == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Purchase Order not found"));
        }

        if (!po.getStatus().equals("PENDING")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Only PENDING orders can be approved"));
        }

        po.setStatus("APPROVED");
        po.setApprovedBy(username);
        purchaseOrderRepository.save(po);

        return ResponseEntity.ok(Map.of(
                "message", "Purchase Order approved successfully",
                "purchaseOrder", po
        ));
    }

    /**
     * REJECT Purchase Order (Admin only)
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> rejectPurchaseOrder(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (!role.equals("admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admins can reject Purchase Orders"));
        }

        PurchaseOrder po = purchaseOrderRepository.findById(id).orElse(null);
        if (po == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Purchase Order not found"));
        }

        if (!po.getStatus().equals("PENDING")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Only PENDING orders can be rejected"));
        }

        po.setStatus("REJECTED");
        po.setApprovedBy(username);
        purchaseOrderRepository.save(po);

        return ResponseEntity.ok(Map.of(
                "message", "Purchase Order rejected",
                "purchaseOrder", po
        ));
    }

    /**
     * ACCEPT Purchase Order (Vendor only, for their assigned POs)
     */
    @PutMapping("/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> acceptPurchaseOrder(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        if (!user.getRole().equalsIgnoreCase("Vendor")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Vendors can accept Purchase Orders"));
        }

        PurchaseOrder po = purchaseOrderRepository.findById(id).orElse(null);
        if (po == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Purchase Order not found"));
        }

        if (!po.getVendorEmail().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only accept Purchase Orders assigned to you"));
        }

        if (!po.getStatus().equals("APPROVED")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Only APPROVED orders can be accepted"));
        }

        po.setStatus("ACCEPTED");
        purchaseOrderRepository.save(po);

        return ResponseEntity.ok(Map.of(
                "message", "Purchase Order accepted successfully",
                "purchaseOrder", po
        ));
    }

    /**
     * DISPATCH Purchase Order (Vendor only, for their assigned POs)
     */
    @PutMapping("/{id}/dispatch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> dispatchPurchaseOrder(@PathVariable Long id, 
                                                    @RequestBody Map<String, String> payload,
                                                    Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        if (!user.getRole().equalsIgnoreCase("Vendor")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Vendors can dispatch Purchase Orders"));
        }

        PurchaseOrder po = purchaseOrderRepository.findById(id).orElse(null);
        if (po == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Purchase Order not found"));
        }

        if (!po.getVendorEmail().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only dispatch Purchase Orders assigned to you"));
        }

        if (!po.getStatus().equals("ACCEPTED")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Only ACCEPTED orders can be dispatched"));
        }

        po.setStatus("DISPATCHED");
        if (payload.containsKey("trackingInfo")) {
            po.setDispatchTracking(payload.get("trackingInfo"));
        }
        purchaseOrderRepository.save(po);

        return ResponseEntity.ok(Map.of(
                "message", "Purchase Order dispatched successfully",
                "purchaseOrder", po
        ));
    }

    /**
     * COMPLETE Purchase Order (Manager/Admin only)
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> completePurchaseOrder(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (!(role.equals("manager") || role.equals("admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Managers or Admins can complete Purchase Orders"));
        }

        PurchaseOrder po = purchaseOrderRepository.findById(id).orElse(null);
        if (po == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Purchase Order not found"));
        }

        if (!po.getStatus().equals("DISPATCHED")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Only DISPATCHED orders can be marked complete"));
        }

        po.setStatus("COMPLETED");
        purchaseOrderRepository.save(po);

        return ResponseEntity.ok(Map.of(
                "message", "Purchase Order marked as completed",
                "purchaseOrder", po
        ));
    }
}
