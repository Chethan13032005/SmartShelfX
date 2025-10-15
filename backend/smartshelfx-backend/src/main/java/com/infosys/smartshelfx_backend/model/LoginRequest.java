package com.infosys.smartshelfx_backend.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}