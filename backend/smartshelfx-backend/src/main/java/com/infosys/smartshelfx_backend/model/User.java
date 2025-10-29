package com.infosys.smartshelfx_backend.model;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
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

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String role = "Store Manager";

    private String company;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "warehouse_location")
    private String warehouseLocation;

    @Column(nullable = false)
    private boolean enabled = true;

    // Constructor with required fields
    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        // Automatically set fullName when firstName and lastName are provided
        updateFullName();
    }

    // Helper method to update full name from first and last names
    private void updateFullName() {
        if (firstName != null && !firstName.trim().isEmpty()) {
            if (lastName != null && !lastName.trim().isEmpty()) {
                this.fullName = firstName + " " + lastName;
            } else {
                this.fullName = firstName;
            }
        } else if (fullName == null) {
            // Set a default value if both firstName and fullName are null
            this.fullName = "";
        }
    }

    // JPA lifecycle callback - called before insert and update
    @PrePersist
    @PreUpdate
    private void ensureFullName() {
        updateFullName();
    }

    // Override setters to keep fullName in sync
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateFullName();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateFullName();
    }
}