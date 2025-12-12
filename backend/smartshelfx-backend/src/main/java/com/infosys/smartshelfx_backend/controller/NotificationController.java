package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Notification;
import com.infosys.smartshelfx_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Get all notifications for current user
    @GetMapping
    public ResponseEntity<?> getMyNotifications() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            
            List<Notification> notifications = notificationService.getNotificationsForUser(email, role);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching notifications: " + e.getMessage());
        }
    }

    // Get unread notifications for current user
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            
            List<Notification> notifications = notificationService.getUnreadNotifications(email, role);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching unread notifications: " + e.getMessage());
        }
    }

    // Get unread count for current user
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            
            Long count = notificationService.getUnreadCount(email, role);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching unread count: " + e.getMessage());
        }
    }

    // Mark notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            Notification notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error marking notification as read: " + e.getMessage());
        }
    }

    // Mark all notifications as read for current user
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            
            notificationService.markAllAsRead(email, role);
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error marking all as read: " + e.getMessage());
        }
    }

    // Delete notification
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting notification: " + e.getMessage());
        }
    }

    // Get all notifications (Admin only)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllNotifications() {
        try {
            List<Notification> notifications = notificationService.getAllNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching all notifications: " + e.getMessage());
        }
    }

    // Create system notification (Admin only)
    @PostMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSystemNotification(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String priority = request.getOrDefault("priority", "NORMAL");
            
            Notification notification = notificationService.createBroadcastNotification("SYSTEM", message, priority);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating system notification: " + e.getMessage());
        }
    }
}
