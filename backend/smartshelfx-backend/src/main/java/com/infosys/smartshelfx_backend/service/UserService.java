package com.infosys.smartshelfx_backend.service;

import com.infosys.smartshelfx_backend.model.User;
import com.infosys.smartshelfx_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import jakarta.annotation.PostConstruct;

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

    // One-time safety: normalize any legacy roles in DB on application startup
    @PostConstruct
    public void normalizeExistingRoles() {
        List<User> users = userRepository.findAll();
        boolean changed = false;
        for (User u : users) {
            String role = u.getRole();
            String normalized;
            if (role == null || role.trim().isEmpty()) {
                normalized = "Manager";
            } else {
                String r = role.trim().toLowerCase();
                if (r.equals("admin")) normalized = "Admin";
                else if (r.equals("manager") || r.equals("store manager") || r.equals("storemanager")) normalized = "Manager";
                else if (r.equals("vendor")) normalized = "Vendor";
                else normalized = "Manager";
            }
            if (!normalized.equals(u.getRole())) {
                u.setRole(normalized);
                changed = true;
            }
        }
        if (changed) {
            userRepository.saveAll(users);
        }
    }

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

        // Normalize role to supported values (Admin, Manager, Vendor), default Manager
        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            user.setRole("Manager");
        } else {
            String r = role.trim().toLowerCase();
            if (r.equals("admin")) user.setRole("Admin");
            else if (r.equals("manager") || r.equals("store manager")) user.setRole("Manager");
            else if (r.equals("vendor")) user.setRole("Vendor");
            else user.setRole("Manager");
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

    /**
     * Resets the user's password to a generated temporary password and attempts
     * to send it via email. Returns true when the email was sent successfully,
     * false when the email could not be delivered (the temporary password is
     * still applied to the user's account in the database).
     */
    public boolean resetPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("No account found with this email address"));

        // Generate random password (8 chars with upper, lower, digit)
        String newPassword = generateRandomPassword();
        
        // Update user password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Send email with new password. Email failures are logged but do not fail the
        // password reset operation so that development/testing is not blocked when
        // SMTP isn't configured.
        boolean emailSent = emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), newPassword);
        if (emailSent) {
            logger.info("Password reset email sent to: {}", email);
        } else {
            logger.warn("Password reset email could not be sent to {}. Temporary password set in DB.", email);
        }

        return emailSent;
    }

    private String generateRandomPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String allChars = upperCase + lowerCase + digits;
        
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        // Ensure at least one of each type
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        
        // Fill remaining 5 characters
        for (int i = 0; i < 5; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public User updateUserRole(Long id, String role) {
        User user = getUserById(id);

        // Normalize incoming role to supported canonical values
        String normalized;
        if (role == null || role.trim().isEmpty()) {
            normalized = "Manager";
        } else {
            String r = role.trim().toLowerCase();
            if (r.equals("admin")) normalized = "Admin";
            else if (r.equals("manager") || r.equals("store manager") || r.equals("storemanager")) normalized = "Manager";
            else if (r.equals("vendor")) normalized = "Vendor";
            else normalized = "Manager";
        }

        user.setRole(normalized);
        return userRepository.save(user);
    }

    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    public User updateUserDetails(Long id, Map<String, Object> updates) {
        User user = getUserById(id);

        if (updates == null) return user;

        Object firstName = updates.get("firstName");
        Object lastName = updates.get("lastName");
        Object company = updates.get("company");
        Object phoneNumber = updates.get("phoneNumber");
        Object warehouseLocation = updates.get("warehouseLocation");
        // Role can still be managed via dedicated endpoint; ignore email/password edits here for safety

        if (firstName instanceof String) user.setFirstName(((String) firstName).trim());
        if (lastName instanceof String) user.setLastName(((String) lastName).trim());
        if (company instanceof String) user.setCompany(((String) company).trim());
        if (phoneNumber instanceof String) user.setPhoneNumber(((String) phoneNumber).trim());
        if (warehouseLocation instanceof String) user.setWarehouseLocation(((String) warehouseLocation).trim());

        return userRepository.save(user);
    }
}
