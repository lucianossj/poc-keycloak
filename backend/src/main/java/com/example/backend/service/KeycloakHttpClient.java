package com.example.backend.service;

import com.example.backend.config.KeycloakProperties;
import com.example.backend.util.UrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class KeycloakHttpClient {
    
    private final RestTemplate restTemplate;
    private final KeycloakProperties keycloakProperties;
    
    @Autowired
    public KeycloakHttpClient(RestTemplate restTemplate, KeycloakProperties keycloakProperties) {
        this.restTemplate = restTemplate;
        this.keycloakProperties = keycloakProperties;
    }
    
    public Map<String, Object> exchangeCodeForToken(String code) {
        Map<String, String> params = buildAuthorizationCodeParams(code);
        Map<String, Object> response = postForToken(params);
        return extractTokens(response);
    }
    
    public Map<String, Object> getUserInfo(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(cleanBearerToken(bearerToken));
        
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                keycloakProperties.getUserInfoEndpoint(),
                HttpMethod.GET,
                request,
                Map.class
        );
        
        return response.getBody();
    }
    
    private Map<String, String> buildAuthorizationCodeParams(String code) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", keycloakProperties.getClientId());
        params.put("code", code);
        params.put("redirect_uri", keycloakProperties.getRedirectUri());
        
        if (keycloakProperties.hasClientSecret()) {
            params.put("client_secret", keycloakProperties.getClientSecret());
        }
        
        return params;
    }
    
    private Map<String, Object> postForToken(Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String body = UrlUtils.buildFormBody(params);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
                keycloakProperties.getTokenEndpoint(),
                request,
                Map.class
        );
        
        return response.getBody();
    }
    
    private Map<String, Object> extractTokens(Map<String, Object> response) {
        if (response == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> tokens = new HashMap<>();
        tokens.put("access_token", response.get("access_token"));
        tokens.put("refresh_token", response.get("refresh_token"));
        tokens.put("id_token", response.get("id_token"));
        
        return tokens;
    }
    
    private String cleanBearerToken(String bearerToken) {
        return bearerToken.replace("Bearer ", "");
    }
}