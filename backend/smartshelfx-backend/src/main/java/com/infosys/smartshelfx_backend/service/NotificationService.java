package com.infosys.smartshelfx_backend.service;

import com.infosys.smartshelfx_backend.model.Notification;
import com.infosys.smartshelfx_backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Create a notification
    @Transactional
    public Notification createNotification(String type, String message, String userEmail, String userRole, 
                                          String priority, String relatedEntityType, Long relatedEntityId) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setUserEmail(userEmail);
        notification.setUserRole(userRole);
        notification.setPriority(priority != null ? priority : "NORMAL");
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setIsRead(false);
        
        return notificationRepository.save(notification);
    }

    // Create a broadcast notification (all users)
    @Transactional
    public Notification createBroadcastNotification(String type, String message, String priority) {
        return createNotification(type, message, null, null, priority, null, null);
    }

    // Create a role-based notification
    @Transactional
    public Notification createRoleNotification(String type, String message, String role, String priority) {
        return createNotification(type, message, null, role, priority, null, null);
    }

    // Create low stock alert
    @Transactional
    public Notification createLowStockAlert(Long productId, String productName, Integer currentStock, Integer reorderLevel) {
        String message = String.format("⚠️ Low Stock Alert: '%s' has only %d units left (Reorder Level: %d)", 
                                      productName, currentStock, reorderLevel);
        return createNotification("LOW_STOCK", message, null, "Manager", "HIGH", "PRODUCT", productId);
    }

    // Create PO status update notification
    @Transactional
    public Notification createPOStatusNotification(Long poId, String status, String vendorEmail, String managerEmail) {
        String message = String.format("📦 Purchase Order #%d status updated to: %s", poId, status);
        
        // Notify both vendor and manager
        if (vendorEmail != null) {
            createNotification("PO_STATUS", message, vendorEmail, null, "NORMAL", "PURCHASE_ORDER", poId);
        }
        if (managerEmail != null) {
            return createNotification("PO_STATUS", message, managerEmail, null, "NORMAL", "PURCHASE_ORDER", poId);
        }
        
        return null;
    }

    // Get all notifications for a user
    public List<Notification> getNotificationsForUser(String email, String role) {
        return notificationRepository.findByUserEmailOrBroadcast(email, role);
    }

    // Get unread notifications for a user
    public List<Notification> getUnreadNotifications(String email, String role) {
        return notificationRepository.findUnreadByUser(email, role);
    }

    // Get unread count for a user
    public Long getUnreadCount(String email, String role) {
        return notificationRepository.countUnreadByUser(email, role);
    }

    // Mark notification as read
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    // Mark all notifications as read for a user
    @Transactional
    public void markAllAsRead(String email, String role) {
        List<Notification> notifications = getUnreadNotifications(email, role);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    // Delete notification
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // Get all notifications (admin only)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
