package com.infosys.smartshelfx_backend.repository;

import com.infosys.smartshelfx_backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Get all notifications for a specific user (by email or broadcast)
    @Query("SELECT n FROM Notification n WHERE " +
           "(n.userEmail = :email OR n.userEmail IS NULL) AND " +
           "(n.userRole = :role OR n.userRole IS NULL) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByUserEmailOrBroadcast(@Param("email") String email, @Param("role") String role);
    
    // Get unread notifications for a user
    @Query("SELECT n FROM Notification n WHERE " +
           "n.isRead = false AND " +
           "(n.userEmail = :email OR n.userEmail IS NULL) AND " +
           "(n.userRole = :role OR n.userRole IS NULL) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUser(@Param("email") String email, @Param("role") String role);
    
    // Count unread notifications for a user
    @Query("SELECT COUNT(n) FROM Notification n WHERE " +
           "n.isRead = false AND " +
           "(n.userEmail = :email OR n.userEmail IS NULL) AND " +
           "(n.userRole = :role OR n.userRole IS NULL)")
    Long countUnreadByUser(@Param("email") String email, @Param("role") String role);
    
    // Get notifications by type
    List<Notification> findByTypeOrderByCreatedAtDesc(String type);
    
    // Get notifications by role
    List<Notification> findByUserRoleOrderByCreatedAtDesc(String userRole);
    
    // Get notifications related to a specific entity
    List<Notification> findByRelatedEntityTypeAndRelatedEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
}
