package com.example.backend.integration;

import com.example.backend.service.KeycloakHttpClient;
import com.example.backend.service.KeycloakLogoutService;
import com.example.backend.service.KeycloakUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KeycloakIntegration {
    
    private final KeycloakUrlService urlService;
    private final KeycloakHttpClient httpClient;
    private final KeycloakLogoutService logoutService;
    
    @Autowired
    public KeycloakIntegration(
            KeycloakUrlService urlService,
            KeycloakHttpClient httpClient,
            KeycloakLogoutService logoutService) {
        this.urlService = urlService;
        this.httpClient = httpClient;
        this.logoutService = logoutService;
    }
    
    public String buildSocialAuthUrl() {
        return urlService.buildSocialAuthUrl();
    }

    public Map<String, Object> exchangeCodeForToken(String code) {
        return httpClient.exchangeCodeForToken(code);
    }
    
    public Map<String, Object> logout(String idToken) {
        return logoutService.logout(idToken);
    }
    
    public Map<String, Object> getUserInfo(String bearerToken) {
        return httpClient.getUserInfo(bearerToken);
    }
}
