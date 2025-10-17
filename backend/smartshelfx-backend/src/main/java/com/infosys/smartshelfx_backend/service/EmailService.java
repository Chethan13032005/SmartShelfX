package com.infosys.smartshelfx_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}
