package com.example.backend.service;

import com.example.backend.config.KeycloakProperties;
import com.example.backend.util.UrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KeycloakUrlService {
    
    private final KeycloakProperties keycloakProperties;
    
    @Autowired
    public KeycloakUrlService(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }
    
    public String buildSocialAuthUrl() {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", keycloakProperties.getClientId());
        params.put("redirect_uri", keycloakProperties.getRedirectUri());
        params.put("response_type", "code");
        params.put("scope", "openid profile email");
        params.put("kc_idp_hint", keycloakProperties.getIdpHint());

        return keycloakProperties.getAuthEndpoint() + "?" + UrlUtils.buildQueryString(params);
    }
    
    public String buildLogoutUrl(String idToken) {
        Map<String, String> params = new HashMap<>();
        params.put("id_token_hint", idToken);
        params.put("post_logout_redirect_uri", keycloakProperties.getPostLogoutRedirectUri());
        
        return keycloakProperties.getLogoutEndpoint() + "?" + UrlUtils.buildQueryString(params);
    }
}