package com.example.backend.model;

import lombok.Data;

@Data
public class LoginResponse {
    private GrantType grantType;
    private String authUrl;
}
