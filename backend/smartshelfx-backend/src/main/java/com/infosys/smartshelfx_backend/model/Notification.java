package com.infosys.smartshelfx_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type; // LOW_STOCK, PO_STATUS, SYSTEM

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "user_email")
    private String userEmail; // null means broadcast to all

    @Column(name = "user_role", length = 50)
    private String userRole; // null means all roles, or specific role (Admin, Manager, Vendor)

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(length = 50)
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // PRODUCT, PURCHASE_ORDER, STOCK_TRANSACTION

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { 
        this.isRead = isRead;
        if (isRead && readAt == null) {
            readAt = LocalDateTime.now();
        }
    }

    public boolean isRead() { return this.isRead; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
