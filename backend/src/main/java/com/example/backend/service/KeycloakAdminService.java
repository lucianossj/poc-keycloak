package com.example.backend.service;

import com.example.backend.config.KeycloakProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class KeycloakAdminService {

    @Autowired
    private KeycloakProperties keycloakProperties;

    @Autowired
    private RestTemplate restTemplate;

    private String adminAccessToken;
    private LocalDateTime tokenExpiration;

    public String getAdminAccessToken() {
        if (adminAccessToken != null && tokenExpiration != null && 
            LocalDateTime.now().isBefore(tokenExpiration)) {
            return adminAccessToken;
        }

        String tokenUrl = keycloakProperties.getAdminTokenEndpoint();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("username", keycloakProperties.getAdminUsername());
        body.add("password", keycloakProperties.getAdminPassword());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            adminAccessToken = (String) responseBody.get("access_token");
            Integer expiresIn = (Integer) responseBody.get("expires_in");
            tokenExpiration = LocalDateTime.now().plusSeconds(expiresIn - 30);

            log.info("Token de admin obtido com sucesso. Expira em: {}", tokenExpiration);
            return adminAccessToken;

        } catch (Exception e) {
            log.error("Erro ao obter token de admin: {}", e.getMessage());
            throw new RuntimeException("Falha ao autenticar como admin no Keycloak", e);
        }
    }

    public Map<String, Object> getUserById(String keycloakUserId) {
        String token = getAdminAccessToken();
        String url = keycloakProperties.getAdminUsersEndpoint() + "/" + keycloakUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            log.info("Usuário recuperado do Keycloak: {}", keycloakUserId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao buscar usuário por ID no Keycloak: {}", e.getMessage());
            throw new RuntimeException("Falha ao buscar usuário no Keycloak", e);
        }
    }

    public void updateCustomAttributes(String keycloakUserId, Map<String, List<String>> attributes) {
        String token = getAdminAccessToken();
        String url = keycloakProperties.getAdminUsersEndpoint() + "/" + keycloakUserId;

        Map<String, Object> currentUser = getUserById(keycloakUserId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, List<String>> existingAttributes = (Map<String, List<String>>) currentUser.get("attributes");
        if (existingAttributes == null) {
            existingAttributes = new HashMap<>();
        }
        existingAttributes.putAll(attributes);

        Map<String, Object> body = new HashMap<>();
        body.put("id", keycloakUserId);
        body.put("username", currentUser.get("username"));
        body.put("email", currentUser.get("email"));
        body.put("firstName", currentUser.get("firstName"));
        body.put("lastName", currentUser.get("lastName"));
        body.put("enabled", currentUser.get("enabled"));
        body.put("emailVerified", currentUser.get("emailVerified"));
        body.put("attributes", existingAttributes);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            log.debug("Atualizando atributos customizados no Keycloak para usuário: {}", keycloakUserId);
            log.debug("Novos atributos: {}", attributes);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Atributos customizados atualizados no Keycloak (username mantido: {})", currentUser.get("username"));
            }
        } catch (Exception e) {
            log.error("Erro ao atualizar atributos no Keycloak: {}", e.getMessage());
            throw new RuntimeException("Falha ao atualizar atributos no Keycloak", e);
        }
    }

    public Map<String, Object> getUserByEmail(String email) {
        String token = getAdminAccessToken();
        String url = keycloakProperties.getAdminUsersEndpoint() + "?email=" + email;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);
            List<Map<String, Object>> users = response.getBody();

            if (users != null && !users.isEmpty()) {
                return users.get(0);
            }
            return null;
        } catch (Exception e) {
            log.error("Erro ao buscar usuário por email: {}", e.getMessage());
            throw new RuntimeException("Falha ao buscar usuário", e);
        }
    }

    public void deleteUser(String keycloakUserId) {
        String token = getAdminAccessToken();
        String url = keycloakProperties.getAdminUsersEndpoint() + "/" + keycloakUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("Usuário deletado do Keycloak: {}", keycloakUserId);
        } catch (Exception e) {
            log.error("Erro ao deletar usuário do Keycloak: {}", e.getMessage());
            throw new RuntimeException("Falha ao deletar usuário do Keycloak", e);
        }
    }
}
