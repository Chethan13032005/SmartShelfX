package com.infosys.smartshelfx_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@yourdomain.com}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String to, String name) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("Welcome to SmartShelfX");
            msg.setText("Hi " + (name != null ? name : "") + ",\n\n"
                    + "Welcome to SmartShelfX! Your account has been created successfully.\n\n"
                    + "Regards,\nSmartShelfX Team");
            mailSender.send(msg);
            logger.info("Welcome email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}", to, e);
        }
    }

    /**
     * Attempts to send a password reset email. Returns true if the email was sent,
     * false otherwise. This method intentionally does not throw so that the
     * password reset flow can continue in development environments where mail
     * server is not configured.
     */
    public boolean sendPasswordResetEmail(String to, String name, String newPassword) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("SmartShelfX - Password Reset");
            msg.setText("Hi " + (name != null ? name : "") + ",\n\n"
                    + "Your password has been reset successfully.\n\n"
                    + "Your new temporary password is: " + newPassword + "\n\n"
                    + "For security reasons, we recommend changing this password after logging in.\n\n"
                    + "If you did not request this password reset, please contact our support team immediately.\n\n"
                    + "Regards,\nSmartShelfX Team");
            mailSender.send(msg);
            logger.info("Password reset email sent to {}", to);
            return true;
        } catch (Exception e) {
            // Log the failure but do not throw - caller will handle behavior for dev vs prod
            logger.error("Failed to send password reset email to {}: {}", to, e.getMessage());
            return false;
        }
    }

    /**
     * Send Purchase Order notification email to vendor
     */
    public void sendPONotificationEmail(String to, String vendorName, String poNumber, int itemCount, String status) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("SmartShelfX - New Purchase Order #" + poNumber);
            
            String statusMessage = "";
            if ("PENDING".equals(status)) {
                statusMessage = "A new purchase order has been created and is pending approval.";
            } else if ("Pending Approval".equals(status)) {
                statusMessage = "A new purchase order has been created and requires your attention once approved.";
            }
            
            msg.setText("Hi " + (vendorName != null ? vendorName : "") + ",\n\n"
                    + statusMessage + "\n\n"
                    + "Purchase Order Number: " + poNumber + "\n"
                    + "Number of Items: " + itemCount + "\n"
                    + "Status: " + status + "\n\n"
                    + "Please log in to the SmartShelfX portal to view the complete details and take necessary action.\n\n"
                    + "Regards,\nSmartShelfX Team");
            mailSender.send(msg);
            logger.info("PO notification email sent to {} for PO #{}", to, poNumber);
        } catch (Exception e) {
            logger.error("Failed to send PO notification email to {} for PO #{}", to, poNumber, e);
        }
    }

    /**
     * Send grouped Purchase Order notification email to vendor (for multiple POs)
     */
    public void sendGroupedPONotificationEmail(String to, String vendorName, List<String> poNumbers, int totalItems) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("SmartShelfX - " + poNumbers.size() + " New Purchase Orders Created");
            
            StringBuilder poList = new StringBuilder();
            for (String poNum : poNumbers) {
                poList.append("  - ").append(poNum).append("\n");
            }
            
            msg.setText("Hi " + (vendorName != null ? vendorName : "") + ",\n\n"
                    + poNumbers.size() + " new purchase orders have been created from AI-powered restock recommendations.\n\n"
                    + "Purchase Order Numbers:\n" + poList.toString() + "\n"
                    + "Total Items: " + totalItems + "\n"
                    + "Status: Pending Approval\n\n"
                    + "These orders will be available for processing once approved by the admin.\n"
                    + "Please log in to the SmartShelfX portal to view complete details.\n\n"
                    + "Regards,\nSmartShelfX Team");
            mailSender.send(msg);
            logger.info("Grouped PO notification email sent to {} for {} POs", to, poNumbers.size());
        } catch (Exception e) {
            logger.error("Failed to send grouped PO notification email to {}", to, e);
        }
    }
}
