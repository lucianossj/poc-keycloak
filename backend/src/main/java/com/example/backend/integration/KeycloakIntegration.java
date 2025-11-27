package com.example.backend.integration;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KeycloakIntegration {
    public String buildGoogleAuthUrl() {
        return KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/auth?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + java.net.URLEncoder.encode("http://localhost:4200/auth/callback", java.nio.charset.StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=openid%20profile%20email" +
                "&kc_idp_hint=google";
    }

    // TODO: Validate Cliente secret usage
    private final String KEYCLOAK_URL = "http://localhost:8080";
    private final String REALM = "poc-ecommerce";
    private final String CLIENT_ID = "poc-ecommerce-app";
    private final String CLIENT_SECRET = "";

    private final RestTemplate restTemplate = new RestTemplate();

    private String buildFormBody(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> java.net.URLEncoder.encode(e.getKey(), java.nio.charset.StandardCharsets.UTF_8) + "=" +
                        java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    public Map<String, Object> login(String username, String password) {
        String url = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("client_id", CLIENT_ID);
        params.put("username", username);
        params.put("password", password);
        if (!CLIENT_SECRET.isEmpty()) params.put("client_secret", CLIENT_SECRET);
        String body = buildFormBody(params);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return response.getBody();
    }

    public Map<String, Object> exchangeCodeForToken(String code) {
        String url = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", CLIENT_ID);
        params.put("code", code);
        params.put("redirect_uri", "http://localhost:4200/auth/callback");
        if (!CLIENT_SECRET.isEmpty()) params.put("client_secret", CLIENT_SECRET);
        String body = buildFormBody(params);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) return new HashMap<>();
        Map<String, Object> tokens = new HashMap<>();
        tokens.put("access_token", responseBody.get("access_token"));
        tokens.put("refresh_token", responseBody.get("refresh_token"));
        tokens.put("id_token", responseBody.get("id_token"));
        return tokens;
    }

    public Map<String, Object> logout(String idToken) {
        String url = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/logout";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Map<String, String> params = new HashMap<>();
        params.put("id_token_hint", idToken);
        String body = buildFormBody(params);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return response.getBody();
    }

    public Map<String, Object> getUserInfo(String bearerToken) {
        String url = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken.replace("Bearer ", ""));
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }
}
