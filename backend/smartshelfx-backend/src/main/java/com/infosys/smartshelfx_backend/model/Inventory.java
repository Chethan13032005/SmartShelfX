package com.infosys.smartshelfx_backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;  // maps to DB 'name' column

    // Legacy code referenced productName; keep for UI compatibility but mark transient (not a DB column)
    @Transient
    private String productName;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "current_stock")
    private int quantity; // maps to DB 'current_stock'

    private String location;

    private String category;

    @Column(name = "vendor_email")
    private String vendor; // maps to DB 'vendor_email'

    @Column(name = "reorder_level")
    private Integer reorderLevel = 10;

    // Column may not exist in legacy schema; mark transient to avoid errors
    @Transient
    private Integer reorderQuantity = 20;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    private String supplier;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        this.productName = name;  // keep transient field in sync
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { 
        this.productName = productName;
        this.name = productName; // keep name aligned
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Alias methods for stock (same as quantity)
    public int getStock() { return quantity; }
    public void setStock(int stock) { this.quantity = stock; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public String getVendorEmail() { return vendor; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public Integer getReorderQuantity() { return reorderQuantity; }
    public void setReorderQuantity(Integer reorderQuantity) { this.reorderQuantity = reorderQuantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
