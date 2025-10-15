package com.infosys.smartshelfx_backend.model;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String role = "Store Manager";

    private String company;
    private String phoneNumber;
    private String warehouseLocation;

    @Column(nullable = false)
    private boolean enabled = true;

    // Default constructor
    public User() {
    }

    // Constructor with required fields
    public User(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    // No need for explicit getters and setters as they are provided by Lombok @Data
}