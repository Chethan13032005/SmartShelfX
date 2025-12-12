package com.infosys.smartshelfx_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name = "stock_transactions")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Inventory product;

    @Column(nullable = false, length = 10)
    private String type; // "IN" or "OUT"

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String notes;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Inventory getProduct() { return product; }
    public void setProduct(Inventory product) { this.product = product; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
