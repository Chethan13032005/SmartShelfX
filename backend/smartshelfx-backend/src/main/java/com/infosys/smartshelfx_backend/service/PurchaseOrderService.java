package com.infosys.smartshelfx_backend.service;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.model.PurchaseOrder;
import com.infosys.smartshelfx_backend.model.User;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.repository.PurchaseOrderRepository;
import com.infosys.smartshelfx_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StockTransactionService stockTransactionService;

    // Create Purchase Order (Manager)
    @Transactional
    public PurchaseOrder createPurchaseOrder(Long productId, Long vendorId, Integer quantity, String notes, String createdBy) {
        // Validate product
        Inventory product = inventoryRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        // Validate vendor
        User vendor = userRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

        if (!"Vendor".equals(vendor.getRole())) {
            throw new RuntimeException("User is not a vendor");
        }

        // Create PO
        PurchaseOrder po = new PurchaseOrder();
        po.setVendorId(vendorId);
        po.setVendorEmail(vendor.getEmail());
        po.setProductId(productId);
        po.setProductName(product.getName());
        po.setQuantity(quantity);
        po.setStatus("PENDING");
        po.setCreatedBy(createdBy);
        po.setNotes(notes);

        PurchaseOrder saved = purchaseOrderRepository.save(po);

        // Notify Admin and Vendor
        String message = String.format("🛒 New Purchase Order #%d created for %d units of '%s'", 
                                      saved.getId(), quantity, product.getName());
        notificationService.createRoleNotification("PO_STATUS", message, "Admin", "NORMAL");
        notificationService.createNotification("PO_STATUS", message, vendor.getEmail(), null, 
                                              "NORMAL", "PURCHASE_ORDER", saved.getId());

        return saved;
    }

    // Auto-generate PO for low stock items (Manager)
    @Transactional
    public List<PurchaseOrder> autoGeneratePOsForLowStock(String createdBy) {
        List<Inventory> lowStockProducts = inventoryRepository.findLowStockProducts();
        
        return lowStockProducts.stream()
            .map(product -> {
                // Find a vendor (simple logic - first vendor found)
                List<User> vendors = userRepository.findByRole("Vendor");
                if (vendors.isEmpty()) {
                    throw new RuntimeException("No vendors available");
                }
                
                User vendor = vendors.get(0); // Simple selection - can be enhanced
                int orderQuantity = (product.getReorderLevel() * 2) - product.getStock(); // Order enough to reach 2x reorder level
                
                return createPurchaseOrder(product.getId(), vendor.getId(), orderQuantity, 
                                          "Auto-generated for low stock", createdBy);
            })
            .toList();
    }

    // Approve PO (Admin)
    @Transactional
    public PurchaseOrder approvePurchaseOrder(Long poId, String approvedBy) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        if (!"PENDING".equals(po.getStatus())) {
            throw new RuntimeException("Purchase Order is not in PENDING status");
        }

        po.setStatus("APPROVED");
        po.setApprovedBy(approvedBy);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        // Notify vendor and manager
        String message = String.format("✅ Purchase Order #%d has been APPROVED", poId);
        notificationService.createPOStatusNotification(poId, "APPROVED", po.getVendorEmail(), po.getCreatedBy());

        return saved;
    }

    // Vendor accepts PO
    @Transactional
    public PurchaseOrder acceptPurchaseOrder(Long poId, LocalDateTime deliveryDate) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        String status = po.getStatus() != null ? po.getStatus().trim().toUpperCase() : "";
        boolean allowedStatus = status.equals("APPROVED") || status.equals("PENDING") || status.equals("PENDING APPROVAL") || status.equals("ACCEPTED");
        if (!allowedStatus) {
            throw new RuntimeException("Purchase Order cannot be accepted in current status: " + po.getStatus());
        }

        po.setStatus("ACCEPTED");
        po.setDeliveryDate(deliveryDate);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        // Notify manager and admin
        String message = String.format("👍 Purchase Order #%d has been ACCEPTED by vendor. Expected delivery: %s", 
                                      poId, deliveryDate);
        notificationService.createPOStatusNotification(poId, "ACCEPTED", null, po.getCreatedBy());
        notificationService.createRoleNotification("PO_STATUS", message, "Admin", "NORMAL");

        return saved;
    }

    // Vendor rejects PO
    @Transactional
    public PurchaseOrder rejectPurchaseOrder(Long poId, String reason) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        String status = po.getStatus() != null ? po.getStatus().trim().toUpperCase() : "";
        boolean allowedStatus = status.equals("PENDING") || status.equals("PENDING APPROVAL") || status.equals("APPROVED") || status.equals("ACCEPTED");
        if (!allowedStatus) {
            throw new RuntimeException("Purchase Order cannot be rejected in current status: " + po.getStatus());
        }

        po.setStatus("REJECTED");
        String existingNotes = po.getNotes() == null ? "" : po.getNotes();
        String rejectionNote = "Rejection reason: " + reason;
        po.setNotes(existingNotes.isBlank() ? rejectionNote : existingNotes + " | " + rejectionNote);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        // Notify manager and admin
        String message = String.format("❌ Purchase Order #%d has been REJECTED by vendor. Reason: %s", 
                                      poId, reason);
        notificationService.createPOStatusNotification(poId, "REJECTED", null, po.getCreatedBy());
        notificationService.createRoleNotification("PO_STATUS", message, "Admin", "HIGH");

        return saved;
    }

    // Vendor dispatches order
    @Transactional
    public PurchaseOrder dispatchOrder(Long poId, String trackingNumber) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        if (!"ACCEPTED".equals(po.getStatus())) {
            throw new RuntimeException("Purchase Order is not in ACCEPTED status");
        }

        po.setStatus("DISPATCHED");
        po.setDispatchTracking(trackingNumber);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        // Notify manager and admin
        String message = String.format("🚚 Purchase Order #%d has been DISPATCHED. Tracking: %s", 
                                      poId, trackingNumber);
        notificationService.createPOStatusNotification(poId, "DISPATCHED", null, po.getCreatedBy());
        notificationService.createRoleNotification("PO_STATUS", message, "Admin", "NORMAL");

        return saved;
    }

    // Manager completes PO (when goods received)
    @Transactional
    public PurchaseOrder completePurchaseOrder(Long poId, String completedBy) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        if (!"DISPATCHED".equals(po.getStatus())) {
            throw new RuntimeException("Purchase Order is not in DISPATCHED status");
        }

        po.setStatus("COMPLETED");
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        // Auto-record stock-in
        stockTransactionService.recordStockIn(po.getProductId(), po.getQuantity(), 
                                             "PO #" + poId + " received", completedBy);

        // Notify vendor and admin
        String message = String.format("✨ Purchase Order #%d has been COMPLETED. Stock updated.", poId);
        notificationService.createPOStatusNotification(poId, "COMPLETED", po.getVendorEmail(), null);
        notificationService.createRoleNotification("PO_STATUS", message, "Admin", "NORMAL");

        return saved;
    }

    // Get all POs
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    // Get POs by status
    public List<PurchaseOrder> getPurchaseOrdersByStatus(String status) {
        return purchaseOrderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    // Get POs by vendor
    public List<PurchaseOrder> getPurchaseOrdersByVendor(Long vendorId) {
        return purchaseOrderRepository.findByVendorIdOrderByCreatedAtDesc(vendorId);
    }

    // Get PO by ID
    public PurchaseOrder getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Purchase Order not found with ID: " + id));
    }

    // Delete PO (admin only, only if PENDING or REJECTED)
    @Transactional
    public void deletePurchaseOrder(Long id) {
        PurchaseOrder po = getPurchaseOrderById(id);
        if (!"PENDING".equals(po.getStatus()) && !"REJECTED".equals(po.getStatus())) {
            throw new RuntimeException("Can only delete PENDING or REJECTED purchase orders");
        }
        purchaseOrderRepository.deleteById(id);
    }
}
