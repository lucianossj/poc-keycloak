package com.example.backend.model;

import lombok.Data;

@Data
public class LoginRequest {
    private GrantType grantType;
    private String username;
    private String password;
}
