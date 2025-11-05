package com.infosys.smartshelfx_backend.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}