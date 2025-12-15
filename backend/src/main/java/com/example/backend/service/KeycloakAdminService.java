package com.example.backend.service;

import com.example.backend.config.KeycloakProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

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

            log.info("updateCustomAttributes: HTTP {}", response.getStatusCodeValue());
            if (response.hasBody()) {
                log.debug("updateCustomAttributes: response body={}", response.getBody());
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Atributos customizados atualizados no Keycloak (username mantido: {})", currentUser.get("username"));
            } else {
                log.warn("Falha ao atualizar atributos no Keycloak - status: {}", response.getStatusCodeValue());
            }
        } catch (HttpStatusCodeException e) {
            log.error("Erro ao atualizar atributos no Keycloak: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao atualizar atributos no Keycloak: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao atualizar atributos no Keycloak: {}", e.getMessage(), e);
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

    /**
     * Cria um usuário no Keycloak.
     * 
     * @param username Username para o Keycloak (idealmente CPF, ou email como fallback)
     * @param email Email do usuário
     * @param firstName Primeiro nome
     * @param lastName Sobrenome
     * @param attributes Atributos customizados
     * @param password Senha (opcional, para login com senha)
     * @return ID do usuário criado no Keycloak
     */
    public String createUser(String username, String email, String firstName, String lastName, 
                            Map<String, List<String>> attributes, String password) {
        String token = getAdminAccessToken();
        String url = keycloakProperties.getAdminUsersEndpoint();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("username", username);  // Usar username fornecido (CPF ou email)
        body.put("email", email);
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("enabled", true);
        body.put("emailVerified", true);
        
        if (attributes != null && !attributes.isEmpty()) {
            body.put("attributes", attributes);
        }
        
        if (password != null && !password.isEmpty()) {
            Map<String, Object> credential = new HashMap<>();
            credential.put("type", "password");
            credential.put("value", password);
            credential.put("temporary", false);
            body.put("credentials", List.of(credential));
            log.info("🔑 Configurando senha para usuário: {} (length: {})", username, password.length());
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            log.info("Criando usuário no Keycloak: username={}, email={}", username, email);
            
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
            
            String locationHeader = response.getHeaders().getFirst("Location");
            if (locationHeader != null) {
                String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                log.info("✅ Usuário criado no Keycloak: username={}, email={} (ID: {}) {}", 
                    username, email, userId, password != null ? "com senha" : "sem senha");
                return userId;
            } else {
                Map<String, Object> createdUser = getUserByEmail(email);
                if (createdUser != null) {
                    String userId = (String) createdUser.get("id");
                    log.info("✅ Usuário criado no Keycloak: username={}, email={} (ID: {}) {}", 
                        username, email, userId, password != null ? "com senha" : "sem senha");
                    return userId;
                }
            }
            
            throw new RuntimeException("Não foi possível obter o ID do usuário criado");
            
        } catch (Exception e) {
            log.error("Erro ao criar usuário no Keycloak: {}", e.getMessage());
            throw new RuntimeException("Falha ao criar usuário no Keycloak: " + e.getMessage(), e);
        }
    }

    public void linkIdentityProvider(String keycloakUserId, String identityProvider, 
                                     String federatedUserId, String federatedUsername) {
        String token = getAdminAccessToken();
        String url = keycloakProperties.getAdminUsersEndpoint() + "/" + keycloakUserId + 
                     "/federated-identity/" + identityProvider;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("identityProvider", identityProvider);
        body.put("userId", federatedUserId);
        body.put("userName", federatedUsername);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
            log.info("✅ Identity Provider '{}' vinculado ao usuário: {}", identityProvider, keycloakUserId);
        } catch (Exception e) {
            log.error("Erro ao vincular Identity Provider: {}", e.getMessage());
            throw new RuntimeException("Falha ao vincular Identity Provider", e);
        }
    }

    public List<Map<String, Object>> getFederatedIdentities(String keycloakUserId) {
        String token = getAdminAccessToken();
        String url = keycloakProperties.getAdminUsersEndpoint() + "/" + keycloakUserId + "/federated-identity";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao buscar federated identities: {}", e.getMessage());
            return List.of();
        }
    }
    /**
     * Atualiza o username do usuário no Keycloak.
     * 
     * IMPORTANTE: Keycloak não permite alterar username de usuários federados via REST API.
     * Solução: Recriar o usuário com novo username e migrar dados.
     */
    public String updateUsername(String oldKeycloakUserId, String newUsername) {
        // Buscar dados atuais do usuário
        Map<String, Object> currentUser = getUserById(oldKeycloakUserId);
        if (currentUser == null) {
            throw new RuntimeException("Usuário não encontrado: " + oldKeycloakUserId);
        }

        String currentUsername = (String) currentUser.get("username");
        
        // Se já é o CPF, não fazer nada
        if (newUsername.equals(currentUsername)) {
            log.info("✅ Username já é o CPF: {}", newUsername);
            return oldKeycloakUserId;
        }

        // Verificar se o novo username já existe
        Map<String, Object> existingUser = getUserByUsername(newUsername);
        if (existingUser != null && !oldKeycloakUserId.equals(existingUser.get("id"))) {
            throw new RuntimeException("Username já está em uso por outro usuário: " + newUsername);
        }

        // Salvar federated identities para re-vincular depois
        List<Map<String, Object>> federatedIdentities = getFederatedIdentities(oldKeycloakUserId);
        
        log.info("🔄 Recriando usuário para alterar username: {} -> {}", currentUsername, newUsername);
        
        try {
            // Salvar dados para recriar
            String email = (String) currentUser.get("email");
            String firstName = (String) currentUser.get("firstName");
            String lastName = (String) currentUser.get("lastName");
            Map<String, Object> attributes = (Map<String, Object>) currentUser.get("attributes");
            
            // PASSO 1: Deletar usuário antigo PRIMEIRO (para liberar o email)
            log.info("�️ Deletando usuário antigo: {}", oldKeycloakUserId);
            deleteUser(oldKeycloakUserId);
            log.info("✅ Usuário antigo deletado");
            
            // PASSO 2: Criar novo usuário com CPF como username
            log.info("�📝 Criando novo usuário: username={}, email={}", newUsername, email);
            String newKeycloakUserId = createUserWithoutPassword(newUsername, email, firstName, lastName, attributes);
            log.info("✅ Novo usuário criado: {}", newKeycloakUserId);
            
            // PASSO 3: Re-vincular Identity Providers
            if (federatedIdentities != null && !federatedIdentities.isEmpty()) {
                for (Map<String, Object> identity : federatedIdentities) {
                    String idp = (String) identity.get("identityProvider");
                    String userId = (String) identity.get("userId");
                    String userName = (String) identity.get("userName");
                    
                    log.info("🔗 Re-vinculando Identity Provider: {}", idp);
                    linkIdentityProvider(newKeycloakUserId, idp, userId, userName);
                }
            }
            
            log.info("✅ Username atualizado com sucesso: {} -> {} (novo ID: {})", 
                    currentUsername, newUsername, newKeycloakUserId);
            
            return newKeycloakUserId;
            
        } catch (Exception e) {
            log.error("❌ Erro ao recriar usuário: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao atualizar username no Keycloak: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cria usuário SEM senha (para usuários de login social)
     */
    private String createUserWithoutPassword(String username, String email, String firstName, 
                                            String lastName, Map<String, Object> attributes) {
        String token = getAdminAccessToken();
        String url = keycloakProperties.getAdminUsersEndpoint();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("enabled", true);
        body.put("emailVerified", true);
        body.put("attributes", attributes);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                // Extrair ID do Location header
                String location = response.getHeaders().getFirst("Location");
                if (location != null) {
                    String userId = location.substring(location.lastIndexOf('/') + 1);
                    log.info("✅ Usuário criado no Keycloak: {}", userId);
                    return userId;
                }
            }
            
            throw new RuntimeException("Falha ao criar usuário: response sem Location header");
            
        } catch (Exception e) {
            log.error("❌ Erro ao criar usuário: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao criar usuário no Keycloak", e);
        }
    }
    
    public void unlinkIdentityProvider(String keycloakUserId, String identityProvider) {
        String token = getAdminAccessToken();
        String url = keycloakProperties.getAdminUsersEndpoint() + "/" + keycloakUserId + 
                     "/federated-identity/" + identityProvider;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("🔓 Identity Provider '{}' desvinculado do usuário: {}", identityProvider, keycloakUserId);
        } catch (Exception e) {
            log.error("Erro ao desvincular Identity Provider: {}", e.getMessage());
            throw new RuntimeException("Falha ao desvincular Identity Provider", e);
        }
    }

    public Map<String, Object> getUserByUsername(String username) {
        String token = getAdminAccessToken();
        String url = keycloakProperties.getAdminUsersEndpoint() + "?username=" + username + "&exact=true";

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
            log.error("Erro ao buscar usuário por username: {}", e.getMessage());
            return null;
        }
    }
}
