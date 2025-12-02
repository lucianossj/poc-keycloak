package com.example.backend.service;

import com.example.backend.integration.KeycloakIntegration;
import com.example.backend.model.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthorizationService {

    @Autowired
    private KeycloakIntegration keycloakIntegration;

    public LoginResponse getUrl() {
        return LoginResponse.builder()
                .authUrl(keycloakIntegration.buildSocialAuthUrl())
                .build();
    }

    public Map<String, Object> exchangeCodeForToken(String code) {
        return keycloakIntegration.exchangeCodeForToken(code);
    }

    public Map<String, Object> logout(String idToken) {
        return keycloakIntegration.logout(idToken);
    }

    public Map<String, Object> getUserInfo(String bearerToken) {
        return keycloakIntegration.getUserInfo(bearerToken);
    }

}
