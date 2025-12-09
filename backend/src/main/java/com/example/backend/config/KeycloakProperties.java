package com.example.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak")
@Data
public class KeycloakProperties {
    
    private String url = "http://localhost:8080";
    private String realm = "poc-ecommerce";
    private String clientId = "poc-ecommerce-app";
    private String clientSecret = "";
    private String redirectUri = "http://localhost:4200/auth/callback";
    private String postLogoutRedirectUri = "http://localhost:4200/login";
    private String idpHint = "google";
    private String adminUsername = "admin";
    private String adminPassword = "admin";

    public String getTokenEndpoint() {
        return url + "/realms/" + realm + "/protocol/openid-connect/token";
    }
    
    public String getAuthEndpoint() {
        return url + "/realms/" + realm + "/protocol/openid-connect/auth";
    }
    
    public String getLogoutEndpoint() {
        return url + "/realms/" + realm + "/protocol/openid-connect/logout";
    }
    
    public String getUserInfoEndpoint() {
        return url + "/realms/" + realm + "/protocol/openid-connect/userinfo";
    }
    
    public String getAdminUsersEndpoint() {
        return url + "/admin/realms/" + realm + "/users";
    }
    
    public String getAdminTokenEndpoint() {
        return url + "/realms/master/protocol/openid-connect/token";
    }
    
    public boolean hasClientSecret() {
        return clientSecret != null && !clientSecret.trim().isEmpty();
    }
}