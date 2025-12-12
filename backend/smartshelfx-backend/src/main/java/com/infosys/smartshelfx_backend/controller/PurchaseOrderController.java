package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.model.PurchaseOrder;
import com.infosys.smartshelfx_backend.model.User;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.repository.PurchaseOrderRepository;
import com.infosys.smartshelfx_backend.repository.UserRepository;
import com.infosys.smartshelfx_backend.service.PurchaseOrderService;
import com.infosys.smartshelfx_backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private EmailService emailService;

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

    @PostMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createBatchPurchaseOrders(@RequestBody Map<String, Object> payload, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }

        String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (!(role.equals("manager") || role.equals("admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only Managers or Admins can create Purchase Orders"));
        }

        try {
            // Support both vendorId and vendorEmail for flexibility
            Long vendorId = null;
            String vendorEmail = null;
            
            if (payload.containsKey("vendorId") && payload.get("vendorId") != null) {
                vendorId = Long.valueOf(payload.get("vendorId").toString());
            } else if (payload.containsKey("vendorEmail") && payload.get("vendorEmail") != null) {
                vendorEmail = payload.get("vendorEmail").toString();
                // Look up vendor by email
                User vendor = userRepository.findByEmail(vendorEmail).orElse(null);
                if (vendor == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Vendor not found with email: " + vendorEmail));
                }
                if (vendor.getRole() == null || !vendor.getRole().trim().equalsIgnoreCase("Vendor")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "User is not a valid vendor"));
                }
                vendorId = vendor.getId();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Either vendorId or vendorEmail must be provided"));
            }
            
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            if (items == null || items.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Items list is required and cannot be empty"));
            }

            List<PurchaseOrder> createdOrders = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Long productId = Long.valueOf(item.get("productId").toString());
                Integer quantity = Integer.valueOf(item.get("quantity").toString());
                String notes = "Part of batch order";
                PurchaseOrder po = purchaseOrderService.createPurchaseOrder(productId, vendorId, quantity, notes, username);
                createdOrders.add(po);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Batch purchase orders created successfully",
                    "count", createdOrders.size(),
                    "orders", createdOrders
            ));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid number format: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create batch Purchase Orders: " + e.getMessage()));
        }
    }

    /**
     * Auto-generate POs for low stock (Manager only)
     */
    @PostMapping("/auto-generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> autoGeneratePOs(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (!(role.equals("manager") || role.equals("admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Managers can auto-generate Purchase Orders"));
        }

        try {
            List<PurchaseOrder> pos = purchaseOrderService.autoGeneratePOsForLowStock(username);
            return ResponseEntity.ok(Map.of(
                    "message", "Purchase orders generated successfully",
                    "count", pos.size(),
                    "orders", pos
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to auto-generate Purchase Orders: " + e.getMessage()));
        }
    }

    /**
     * POST /api/purchase-orders/create
     * Create Purchase Orders from AI restock suggestions (edited by admin)
     * Groups items by vendor and creates separate POs
     * Sets status = "Pending Approval" and sends email notifications
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createFromRestockSuggestions(@RequestBody Map<String, Object> payload, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (!(role.equals("manager") || role.equals("admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Managers or Admins can create Purchase Orders"));
        }

        try {
            // Parse the modified suggestions from admin
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            
            if (items == null || items.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No items provided"));
            }

            // Group items by vendor
            Map<Long, List<Map<String, Object>>> itemsByVendor = new HashMap<>();
            for (Map<String, Object> item : items) {
                Long vendorId = item.get("vendorId") != null 
                    ? Long.valueOf(item.get("vendorId").toString()) 
                    : null;
                
                if (vendorId == null) {
                    continue; // Skip items without vendor assignment
                }
                
                itemsByVendor.computeIfAbsent(vendorId, k -> new ArrayList<>()).add(item);
            }

            if (itemsByVendor.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No valid items with vendor assignments"));
            }

            // Create POs grouped by vendor
            List<PurchaseOrder> createdPOs = new ArrayList<>();
            Map<String, List<String>> vendorPOMap = new HashMap<>(); // vendor email -> PO numbers

            for (Map.Entry<Long, List<Map<String, Object>>> entry : itemsByVendor.entrySet()) {
                Long vendorId = entry.getKey();
                List<Map<String, Object>> vendorItems = entry.getValue();
                
                // Get vendor details
                User vendor = userRepository.findById(vendorId).orElse(null);
                if (vendor == null || !"Vendor".equalsIgnoreCase(vendor.getRole())) {
                    continue; // Skip invalid vendors
                }

                // Create PO for each item (or group them in one PO per vendor)
                for (Map<String, Object> item : vendorItems) {
                    Long productId = Long.valueOf(item.get("productId").toString());
                    Integer quantity = Integer.valueOf(item.get("quantity").toString());
                    
                    // Get product details
                    Inventory product = inventoryRepository.findById(productId).orElse(null);
                    if (product == null || quantity <= 0) {
                        continue; // Skip invalid products or zero quantity
                    }

                    // Create Purchase Order
                    PurchaseOrder po = new PurchaseOrder();
                    po.setVendorId(vendorId);
                    po.setVendorEmail(vendor.getEmail());
                    po.setProductId(productId);
                    po.setProductName(product.getName());
                    po.setQuantity(quantity);
                    po.setStatus("Pending Approval"); // Set status as required
                    po.setCreatedBy(username);
                    po.setNotes("Auto-generated from AI restock recommendations");
                    
                    PurchaseOrder saved = purchaseOrderRepository.save(po);
                    createdPOs.add(saved);
                    
                    // Track PO numbers per vendor for email notification
                    vendorPOMap.computeIfAbsent(vendor.getEmail(), k -> new ArrayList<>())
                              .add(saved.getPoNumber());
                }
            }

            if (createdPOs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No valid purchase orders could be created"));
            }

            // Send email notifications to each vendor
            for (Map.Entry<String, List<String>> entry : vendorPOMap.entrySet()) {
                String vendorEmail = entry.getKey();
                List<String> poNumbers = entry.getValue();
                User vendor = userRepository.findByEmail(vendorEmail).orElse(null);
                
                if (vendor != null && !poNumbers.isEmpty()) {
                    try {
                        if (poNumbers.size() == 1) {
                            // Single PO notification
                            emailService.sendPONotificationEmail(
                                vendorEmail, 
                                vendor.getFullName(), 
                                poNumbers.get(0), 
                                1, 
                                "Pending Approval"
                            );
                        } else {
                            // Multiple POs notification
                            emailService.sendGroupedPONotificationEmail(
                                vendorEmail, 
                                vendor.getFullName(), 
                                poNumbers, 
                                poNumbers.size()
                            );
                        }
                    } catch (Exception emailEx) {
                        // Log but don't fail the entire operation
                        System.err.println("Failed to send email to " + vendorEmail + ": " + emailEx.getMessage());
                    }
                }
            }

            // Group POs by vendor for response
            Map<String, List<PurchaseOrder>> posByVendor = createdPOs.stream()
                .collect(Collectors.groupingBy(PurchaseOrder::getVendorEmail));

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Purchase orders created successfully from AI suggestions",
                "totalOrders", createdPOs.size(),
                "vendorsCount", posByVendor.size(),
                "ordersByVendor", posByVendor,
                "orders", createdPOs
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create purchase orders: " + e.getMessage()));
        }
    }

    /**
     * GET All Purchase Orders (role-filtered)
     * - Admin: sees all
     * - Manager: sees all
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
        System.out.println("===== PO GET REQUEST =====");
        System.out.println("User: " + username);
        System.out.println("Role: " + role);
        
        if (role.equals("admin") || role.equals("manager")) {
            // Admin and Manager see all POs (including AI-generated and manual)
            orders = purchaseOrderRepository.findAll();
            System.out.println("Fetching ALL orders - Count: " + orders.size());
        } else if (role.equals("vendor")) {
            // Vendor sees only POs assigned to them
            orders = purchaseOrderRepository.findByVendorEmailOrderByCreatedAtDesc(username);
            System.out.println("Fetching vendor orders - Count: " + orders.size());
        }
        System.out.println("=========================");

        return ResponseEntity.ok(orders);
    }

    /**
     * APPROVE Purchase Order (Admin or Manager)
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
        if (!(role.equals("admin") || role.equals("manager"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admins or Managers can approve Purchase Orders"));
        }

        try {
            PurchaseOrder po = purchaseOrderService.approvePurchaseOrder(id, username);
            return ResponseEntity.ok(Map.of(
                    "message", "Purchase Order approved successfully",
                    "purchaseOrder", po
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * REJECT Purchase Order (Admin, Manager, or Vendor)
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> rejectPurchaseOrder(@PathVariable Long id, 
                                                 @RequestBody Map<String, String> payload,
                                                 Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
        if (!(role.equals("admin") || role.equals("manager") || role.equals("vendor"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admins, Managers, or Vendors can reject Purchase Orders"));
        }

        try {
            String reason = payload.getOrDefault("reason", "No reason provided");
            PurchaseOrder po = purchaseOrderService.rejectPurchaseOrder(id, reason);
            return ResponseEntity.ok(Map.of(
                    "message", "Purchase Order rejected",
                    "purchaseOrder", po
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DISPATCH Purchase Order (Vendor only, for their assigned POs)
     */
    @PutMapping("/{id}/dispatch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> dispatchPurchaseOrder(@PathVariable Long id, 
                                                    @RequestBody(required = false) Map<String, String> payload,
                                                    @RequestParam(name = "trackingNumber", required = false) String trackingNumberParam,
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

        try {
            String trackingNumber = "";
            if (payload != null && payload.get("trackingInfo") != null) {
                trackingNumber = payload.get("trackingInfo");
            } else if (payload != null && payload.get("trackingNumber") != null) {
                trackingNumber = payload.get("trackingNumber");
            } else if (trackingNumberParam != null) {
                trackingNumber = trackingNumberParam;
            }
            PurchaseOrder updated = purchaseOrderService.dispatchOrder(id, trackingNumber);
            return ResponseEntity.ok(Map.of(
                    "message", "Purchase Order dispatched successfully",
                    "purchaseOrder", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ACCEPT Purchase Order (Vendor only, for their assigned POs)
     */
    @PutMapping("/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> acceptPurchaseOrder(@PathVariable Long id,
                                                 @RequestBody(required = false) Map<String, String> payload,
                                                 @RequestParam(name = "deliveryDate", required = false) String deliveryDateParam,
                                                 Authentication authentication) {
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

        try {
            String dateStr = null;
            if (payload != null && payload.get("deliveryDate") != null) {
                dateStr = payload.get("deliveryDate");
            } else if (deliveryDateParam != null) {
                dateStr = deliveryDateParam;
            }

            java.time.LocalDateTime deliveryDate = null;
            if (dateStr != null && !dateStr.isBlank()) {
                deliveryDate = java.time.LocalDateTime.parse(dateStr);
            }

            PurchaseOrder updated = purchaseOrderService.acceptPurchaseOrder(id, deliveryDate);
            return ResponseEntity.ok(Map.of(
                    "message", "Purchase Order accepted successfully",
                    "purchaseOrder", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
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

        try {
            PurchaseOrder po = purchaseOrderService.completePurchaseOrder(id, username);
            return ResponseEntity.ok(Map.of(
                    "message", "Purchase Order marked as completed. Stock has been updated.",
                    "purchaseOrder", po
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
