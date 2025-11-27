package com.example.backend.service;

import com.example.backend.integration.KeycloakIntegration;
import com.example.backend.model.GrantType;
import com.example.backend.model.LoginRequest;
import com.example.backend.model.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthorizationService {

    private final String keycloakUrl = "http://localhost:8080";
    private final String realm = "poc-ecommerce";
    private final String clientId = "poc-ecommerce-app";

    @Autowired
    private KeycloakIntegration keycloakIntegration;

    // TODO: Refactor
    public LoginResponse login(LoginRequest request) {
        GrantType grantType = request.getGrantType();
        LoginResponse response = new LoginResponse();
        response.setGrantType(grantType);

        switch (grantType) {
            case GOOGLE:
                String authUrl = keycloakIntegration.buildGoogleAuthUrl();
                response.setAuthUrl(authUrl);
                break;
            case PASSWORD:
            case PASSWORDLESS:
            case INSTAGRAM:
            default:
                response.setAuthUrl(null);
                break;
        }
        return response;
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
