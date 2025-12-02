package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    
    private String url = "http://localhost:8080";
    private String realm = "poc-ecommerce";
    private String clientId = "poc-ecommerce-app";
    private String clientSecret = "";
    private String redirectUri = "http://localhost:4200/auth/callback";
    private String postLogoutRedirectUri = "http://localhost:4200/login";
    private String idpHint = "google";

    // Getters and Setters
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getRealm() {
        return realm;
    }
    
    public void setRealm(String realm) {
        this.realm = realm;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    public String getRedirectUri() {
        return redirectUri;
    }
    
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    
    public String getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }
    
    public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }
    
    public String getIdpHint() {
        return idpHint;
    }

    public void setIdpHint(String idpHint) {
        this.idpHint = idpHint;
    }

    // Helper methods
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
    
    public boolean hasClientSecret() {
        return clientSecret != null && !clientSecret.trim().isEmpty();
    }
}