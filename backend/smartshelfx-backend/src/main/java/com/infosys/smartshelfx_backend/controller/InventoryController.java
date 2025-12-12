package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class InventoryController {
    private final InventoryRepository repo;
    
    public InventoryController(InventoryRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /api/products
     * Access: Admin, Manager, Vendor
     * Returns all products (Vendor sees only products in their POs - to be filtered later)
     */
    @GetMapping
    public List<Inventory> getAll() {
        return repo.findAll();
    }

    /**
     * POST /api/products
     * Access: Admin, Manager only
     * Creates a new product
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Inventory item) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) 
                ? auth.getName() : "system";

            // Map vendor to supplier if only one provided
            if ((item.getSupplier() == null || item.getSupplier().isEmpty()) && item.getVendor() != null) {
                item.setSupplier(item.getVendor());
            }
            if ((item.getVendor() == null || item.getVendor().isEmpty()) && item.getSupplier() != null) {
                item.setVendor(item.getSupplier());
            }

            item.setCreatedBy(username);
            item.setUpdatedBy(username);
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            
            Inventory saved = repo.save(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating product: " + e.getMessage());
        }
    }

    /**
     * PUT /api/products/{id}
     * Access: Admin, Manager only
     * Updates an existing product
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Inventory item) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) 
                ? auth.getName() : "system";

            Inventory existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Use setName() to be consistent with the field name
            if (item.getName() != null) existing.setName(item.getName());
            if (item.getCategory() != null) existing.setCategory(item.getCategory());
            if (item.getQuantity() >= 0) existing.setQuantity(item.getQuantity());
            if (item.getPrice() != null) existing.setPrice(item.getPrice());
            
            // Keep vendor/supplier in sync
            String vendor = item.getVendor();
            String supplier = item.getSupplier();
            if ((supplier == null || supplier.isEmpty()) && vendor != null) supplier = vendor;
            if ((vendor == null || vendor.isEmpty()) && supplier != null) vendor = supplier;
            existing.setSupplier(supplier);
            existing.setVendor(vendor);
            
            if (item.getLocation() != null) existing.setLocation(item.getLocation());
            if (item.getImageUrl() != null) existing.setImageUrl(item.getImageUrl());
            if (item.getSku() != null) existing.setSku(item.getSku());
            if (item.getReorderLevel() != null) existing.setReorderLevel(item.getReorderLevel());
            if (item.getReorderQuantity() != null) existing.setReorderQuantity(item.getReorderQuantity());
            
            existing.setUpdatedBy(username);
            existing.setUpdatedAt(LocalDateTime.now());
            
            return ResponseEntity.ok(repo.save(existing));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating product: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/products/{id}
     * Access: Admin only
     * Deletes a product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            if (!repo.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found");
            }

            repo.deleteById(id);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting product: " + e.getMessage());
        }
    }

    // Keep legacy endpoints for backwards compatibility with existing frontend
    @GetMapping("/view")
    public List<Inventory> getAllLegacy() {
        return getAll();
    }

    @PostMapping("/add")
    public ResponseEntity<?> createLegacy(@RequestBody Inventory item) {
        return create(item);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLegacy(@PathVariable Long id, @RequestBody Inventory item) {
        return update(id, item);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteLegacy(@PathVariable Long id) {
        return delete(id);
    }

    /**
     * POST /api/products/batch-import
     * Accepts a JSON array of products to import in batch.
     * Access: Admin, Manager
     */
    @PostMapping("/batch-import")
    public ResponseEntity<?> batchImport(@RequestBody List<Inventory> items) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) 
                ? auth.getName() : "system";

            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().body("No items provided");
            }

            // Normalize vendor/supplier fields and audit info
            LocalDateTime now = LocalDateTime.now();
            for (Inventory it : items) {
                if ((it.getSupplier() == null || it.getSupplier().isEmpty()) && it.getVendor() != null) {
                    it.setSupplier(it.getVendor());
                }
                if ((it.getVendor() == null || it.getVendor().isEmpty()) && it.getSupplier() != null) {
                    it.setVendor(it.getSupplier());
                }
                it.setCreatedBy(username);
                it.setUpdatedBy(username);
                it.setCreatedAt(now);
                it.setUpdatedAt(now);
            }

            List<Inventory> saved = repo.saveAll(items);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error batch importing products: " + e.getMessage());
        }
    }
}
