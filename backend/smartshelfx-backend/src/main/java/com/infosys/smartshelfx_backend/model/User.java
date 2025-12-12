package com.infosys.smartshelfx_backend.model;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;

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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String role = "Manager";

    private String company;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "warehouse_location")
    private String warehouseLocation;

    @Column(nullable = false)
    private boolean enabled = true;

    // Constructors
    public User() {}

    public User(Long id, String email, String password, String firstName, String lastName, 
                String fullName, String name, String role, String company, 
                String phoneNumber, String warehouseLocation, boolean enabled) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.name = name;
        this.role = role;
        this.company = company;
        this.phoneNumber = phoneNumber;
        this.warehouseLocation = warehouseLocation;
        this.enabled = enabled;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // JPA lifecycle callback
    @PrePersist
    @PreUpdate
    private void ensureFullName() {
        if (fullName == null || fullName.trim().isEmpty()) {
            if (firstName != null && !firstName.trim().isEmpty()) {
                if (lastName != null && !lastName.trim().isEmpty()) {
                    this.fullName = firstName + " " + lastName;
                } else {
                    this.fullName = firstName;
                }
            } else if (email != null) {
                this.fullName = email;
            } else {
                this.fullName = "";
            }
        }
        
        if (name == null || name.trim().isEmpty()) {
            this.name = (fullName != null && !fullName.trim().isEmpty()) ? fullName : 
                        (email != null && !email.trim().isEmpty()) ? email : "";
        }
    }
}