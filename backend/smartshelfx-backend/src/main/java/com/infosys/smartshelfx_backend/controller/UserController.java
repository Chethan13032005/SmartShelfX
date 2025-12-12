package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.LoginRequest;
import com.infosys.smartshelfx_backend.model.User;
import com.infosys.smartshelfx_backend.repository.UserRepository;
import com.infosys.smartshelfx_backend.security.JwtService;
import com.infosys.smartshelfx_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("user", registeredUser);
            response.put("message", "Welcome to SmartShelfX, " + registeredUser.getFullName());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

            // If client supplied a role, ensure it matches the stored role for safety
            if (loginRequest.getRole() != null && !loginRequest.getRole().trim().isEmpty()) {
                if (!loginRequest.getRole().equals(user.getRole())) {
                    return ResponseEntity.badRequest().body("Role does not match the user's assigned role");
                }
            }

            // Build a minimal UserDetails for token generation
            org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())
                    )
                );

            String token = jwtService.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            
            // Trigger password reset process (email will be sent with the new password)
            boolean emailSent = userService.resetPassword(email);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "If an account exists for this email, a temporary password has been set and an email was attempted.");
            response.put("emailSent", emailSent);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/vendors")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<User>> getAllVendors() {
        List<User> vendors = userService.getUsersByRole("Vendor");
        return ResponseEntity.ok(vendors);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String role = request.get("role");
            if (role == null || role.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Role is required");
            }
            User updated = userService.updateUserRole(id, role);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        try {
            User updated = userService.toggleUserStatus(id);
            Map<String, Object> response = new HashMap<>();
            response.put("user", updated);
            response.put("message", updated.isEnabled() ? "User enabled" : "User disabled");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            User updated = userService.updateUserDetails(id, updates);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());
        response.put("roleRaw", "'" + user.getRole() + "'");
        response.put("roleLower", user.getRole().toLowerCase());
        response.put("roleLength", user.getRole().length());
        return ResponseEntity.ok(response);
    }
}
