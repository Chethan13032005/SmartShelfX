package com.infosys.smartshelfx_backend.service;

import com.infosys.smartshelfx_backend.model.User;
import com.infosys.smartshelfx_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // @Autowired
    // private SmsService smsService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    private static final Pattern PWD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public User registerUser(User user) {
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new RuntimeException("Invalid email format");
        }

        if (!PWD_PATTERN.matcher(user.getPassword()).matches()) {
            throw new RuntimeException("Password does not meet security requirements");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);

        // send welcome email (async in real app)
        try {
            emailService.sendWelcomeEmail(saved.getEmail(), saved.getFullName());
        } catch (Exception e) {
            logger.error("Failed to send email: {}", e.getMessage());
        }

        // send SMS if phone number is provided
        // try {
        //     if (saved.getPhoneNumber() != null && !saved.getPhoneNumber().isEmpty()) {
        //         smsService.sendSms(saved.getPhoneNumber(), "Welcome to SmartShelfX, " + saved.getFullName());
        //     }
        // } catch (Exception e) {
        //     logger.error("Failed to send SMS: {}", e.getMessage());
        // }

        return saved;
    }

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return user;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
