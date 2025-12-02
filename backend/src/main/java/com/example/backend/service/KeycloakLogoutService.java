package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KeycloakLogoutService {
    
    private final KeycloakUrlService keycloakUrlService;
    
    @Autowired
    public KeycloakLogoutService(KeycloakUrlService keycloakUrlService) {
        this.keycloakUrlService = keycloakUrlService;
    }
    
    public Map<String, Object> logout(String idToken) {
        try {
            String logoutUrl = keycloakUrlService.buildLogoutUrl(idToken);
            return createSuccessResponse(logoutUrl);
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
    
    private Map<String, Object> createSuccessResponse(String logoutUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("logoutUrl", logoutUrl);
        return result;
    }
    
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", errorMessage);
        return result;
    }
}