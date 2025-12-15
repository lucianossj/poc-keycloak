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
        if (isTokenValid()) {
            return adminAccessToken;
        }
        return refreshAdminToken();
    }

    public Map<String, Object> getUserById(String keycloakUserId) {
        String url = buildUserByIdUrl(keycloakUserId);
        HttpEntity<Void> request = buildAuthenticatedGetRequest();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao buscar usuário por ID: {}", e.getMessage());
            throw new RuntimeException("Falha ao buscar usuário no Keycloak", e);
        }
    }

    public Map<String, Object> getUserByEmail(String email) {
        String url = buildUserByEmailUrl(email);
        HttpEntity<Void> request = buildAuthenticatedGetRequest();

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);
            return extractFirstUser(response.getBody());
        } catch (Exception e) {
            log.error("Erro ao buscar usuário por email: {}", e.getMessage());
            throw new RuntimeException("Falha ao buscar usuário", e);
        }
    }

    public Map<String, Object> getUserByUsername(String username) {
        String url = buildUserByUsernameUrl(username);
        HttpEntity<Void> request = buildAuthenticatedGetRequest();

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);
            return extractFirstUser(response.getBody());
        } catch (Exception e) {
            log.error("Erro ao buscar usuário por username: {}", e.getMessage());
            return null;
        }
    }

    public String createUser(String username, String email, String firstName, String lastName,
                            Map<String, List<String>> attributes, String password) {
        Map<String, Object> body = buildCreateUserBody(username, email, firstName, lastName, attributes, password);
        HttpEntity<Map<String, Object>> request = buildAuthenticatedPostRequest(body);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                keycloakProperties.getAdminUsersEndpoint(), 
                HttpMethod.POST, 
                request, 
                Void.class
            );
            return extractUserIdFromResponse(response, email);
        } catch (Exception e) {
            log.error("Erro ao criar usuário: {}", e.getMessage());
            throw new RuntimeException("Falha ao criar usuário no Keycloak: " + e.getMessage(), e);
        }
    }

    public void updateCustomAttributes(String keycloakUserId, Map<String, List<String>> attributes) {
        Map<String, Object> currentUser = getUserById(keycloakUserId);
        Map<String, Object> updatedUser = buildUserWithUpdatedAttributes(currentUser, attributes);
        
        String url = buildUserByIdUrl(keycloakUserId);
        HttpEntity<Map<String, Object>> request = buildAuthenticatedPutRequest(updatedUser);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        } catch (HttpStatusCodeException e) {
            log.error("Erro ao atualizar atributos: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao atualizar atributos: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao atualizar atributos: {}", e.getMessage());
            throw new RuntimeException("Falha ao atualizar atributos no Keycloak", e);
        }
    }

    public String updateUsername(String oldKeycloakUserId, String newUsername) {
        Map<String, Object> currentUser = getUserById(oldKeycloakUserId);
        validateUserExists(currentUser, oldKeycloakUserId);

        String currentUsername = (String) currentUser.get("username");
        
        if (isUsernameSame(currentUsername, newUsername)) {
            return oldKeycloakUserId;
        }

        validateUsernameAvailable(newUsername, oldKeycloakUserId);
        
        List<Map<String, Object>> federatedIdentities = getFederatedIdentities(oldKeycloakUserId);
        
        return recreateUserWithNewUsername(oldKeycloakUserId, newUsername, currentUser, federatedIdentities);
    }

    public void deleteUser(String keycloakUserId) {
        String url = buildUserByIdUrl(keycloakUserId);
        HttpEntity<Void> request = buildAuthenticatedDeleteRequest();

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
        } catch (Exception e) {
            log.error("Erro ao deletar usuário: {}", e.getMessage());
            throw new RuntimeException("Falha ao deletar usuário do Keycloak", e);
        }
    }

    public void linkIdentityProvider(String keycloakUserId, String identityProvider,
                                     String federatedUserId, String federatedUsername) {
        String url = buildIdentityProviderUrl(keycloakUserId, identityProvider);
        Map<String, Object> body = buildLinkIdentityBody(identityProvider, federatedUserId, federatedUsername);
        HttpEntity<Map<String, Object>> request = buildAuthenticatedPostRequest(body);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
        } catch (Exception e) {
            log.error("Erro ao vincular Identity Provider: {}", e.getMessage());
            throw new RuntimeException("Falha ao vincular Identity Provider", e);
        }
    }

    public void unlinkIdentityProvider(String keycloakUserId, String identityProvider) {
        String url = buildIdentityProviderUrl(keycloakUserId, identityProvider);
        HttpEntity<Void> request = buildAuthenticatedDeleteRequest();

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
        } catch (Exception e) {
            log.error("Erro ao desvincular Identity Provider: {}", e.getMessage());
            throw new RuntimeException("Falha ao desvincular Identity Provider", e);
        }
    }

    public List<Map<String, Object>> getFederatedIdentities(String keycloakUserId) {
        String url = keycloakProperties.getAdminUsersEndpoint() + "/" + keycloakUserId + "/federated-identity";
        HttpEntity<Void> request = buildAuthenticatedGetRequest();

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao buscar federated identities: {}", e.getMessage());
            return List.of();
        }
    }

    private boolean isTokenValid() {
        return adminAccessToken != null && 
               tokenExpiration != null && 
               LocalDateTime.now().isBefore(tokenExpiration);
    }

    private String refreshAdminToken() {
        MultiValueMap<String, String> body = buildAdminTokenRequestBody();
        HttpEntity<MultiValueMap<String, String>> request = buildFormRequest(body);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                keycloakProperties.getAdminTokenEndpoint(), 
                request, 
                Map.class
            );
            return extractAndCacheToken(response.getBody());
        } catch (Exception e) {
            log.error("Erro ao obter token de admin: {}", e.getMessage());
            throw new RuntimeException("Falha ao autenticar como admin no Keycloak", e);
        }
    }

    private String extractAndCacheToken(Map<String, Object> responseBody) {
        adminAccessToken = (String) responseBody.get("access_token");
        Integer expiresIn = (Integer) responseBody.get("expires_in");
        tokenExpiration = LocalDateTime.now().plusSeconds(expiresIn - 30);
        return adminAccessToken;
    }

    private MultiValueMap<String, String> buildAdminTokenRequestBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("username", keycloakProperties.getAdminUsername());
        body.add("password", keycloakProperties.getAdminPassword());
        return body;
    }

    private Map<String, Object> buildCreateUserBody(String username, String email, String firstName,
                                                     String lastName, Map<String, List<String>> attributes,
                                                     String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("enabled", true);
        body.put("emailVerified", true);

        if (attributes != null && !attributes.isEmpty()) {
            body.put("attributes", attributes);
        }

        if (password != null && !password.isEmpty()) {
            body.put("credentials", List.of(buildPasswordCredential(password)));
        }

        return body;
    }

    private Map<String, Object> buildPasswordCredential(String password) {
        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", password);
        credential.put("temporary", false);
        return credential;
    }

    private Map<String, Object> buildUserWithUpdatedAttributes(Map<String, Object> currentUser,
                                                                Map<String, List<String>> newAttributes) {
        Map<String, List<String>> existingAttributes = (Map<String, List<String>>) currentUser.get("attributes");
        if (existingAttributes == null) {
            existingAttributes = new HashMap<>();
        }
        existingAttributes.putAll(newAttributes);

        Map<String, Object> updatedUser = new HashMap<>();
        updatedUser.put("id", currentUser.get("id"));
        updatedUser.put("username", currentUser.get("username"));
        updatedUser.put("email", currentUser.get("email"));
        updatedUser.put("firstName", currentUser.get("firstName"));
        updatedUser.put("lastName", currentUser.get("lastName"));
        updatedUser.put("enabled", currentUser.get("enabled"));
        updatedUser.put("emailVerified", currentUser.get("emailVerified"));
        updatedUser.put("attributes", existingAttributes);

        return updatedUser;
    }

    private Map<String, Object> buildLinkIdentityBody(String identityProvider, String federatedUserId,
                                                       String federatedUsername) {
        Map<String, Object> body = new HashMap<>();
        body.put("identityProvider", identityProvider);
        body.put("userId", federatedUserId);
        body.put("userName", federatedUsername);
        return body;
    }

    private String recreateUserWithNewUsername(String oldKeycloakUserId, String newUsername,
                                                Map<String, Object> currentUser,
                                                List<Map<String, Object>> federatedIdentities) {
        try {
            UserData userData = extractUserData(currentUser);
            
            deleteUser(oldKeycloakUserId);
            String newKeycloakUserId = createUserWithoutPassword(newUsername, userData);
            relinkFederatedIdentities(newKeycloakUserId, federatedIdentities);
            
            return newKeycloakUserId;
        } catch (Exception e) {
            log.error("Erro ao recriar usuário: {}", e.getMessage());
            throw new RuntimeException("Falha ao atualizar username: " + e.getMessage(), e);
        }
    }

    private UserData extractUserData(Map<String, Object> currentUser) {
        return new UserData(
            (String) currentUser.get("email"),
            (String) currentUser.get("firstName"),
            (String) currentUser.get("lastName"),
            (Map<String, Object>) currentUser.get("attributes")
        );
    }

    private String createUserWithoutPassword(String username, UserData userData) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("email", userData.email);
        body.put("firstName", userData.firstName);
        body.put("lastName", userData.lastName);
        body.put("enabled", true);
        body.put("emailVerified", true);
        body.put("attributes", userData.attributes);

        HttpEntity<Map<String, Object>> request = buildAuthenticatedPostRequest(body);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                keycloakProperties.getAdminUsersEndpoint(),
                HttpMethod.POST,
                request,
                String.class
            );
            return extractUserIdFromLocationHeader(response);
        } catch (Exception e) {
            log.error("Erro ao criar usuário sem senha: {}", e.getMessage());
            throw new RuntimeException("Falha ao criar usuário no Keycloak", e);
        }
    }

    private void relinkFederatedIdentities(String keycloakUserId, List<Map<String, Object>> federatedIdentities) {
        if (federatedIdentities == null || federatedIdentities.isEmpty()) {
            return;
        }

        for (Map<String, Object> identity : federatedIdentities) {
            String idp = (String) identity.get("identityProvider");
            String userId = (String) identity.get("userId");
            String userName = (String) identity.get("userName");
            linkIdentityProvider(keycloakUserId, idp, userId, userName);
        }
    }

    private void validateUserExists(Map<String, Object> user, String keycloakUserId) {
        if (user == null) {
            throw new RuntimeException("Usuário não encontrado: " + keycloakUserId);
        }
    }

    private boolean isUsernameSame(String currentUsername, String newUsername) {
        return newUsername.equals(currentUsername);
    }

    private void validateUsernameAvailable(String newUsername, String currentUserId) {
        Map<String, Object> existingUser = getUserByUsername(newUsername);
        if (existingUser != null && !currentUserId.equals(existingUser.get("id"))) {
            throw new RuntimeException("Username já está em uso por outro usuário: " + newUsername);
        }
    }

    private String extractUserIdFromResponse(ResponseEntity<Void> response, String email) {
        String locationHeader = response.getHeaders().getFirst("Location");
        if (locationHeader != null) {
            return extractUserIdFromLocation(locationHeader);
        }
        return getUserIdByEmail(email);
    }

    private String extractUserIdFromLocation(String locationHeader) {
        return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
    }

    private String extractUserIdFromLocationHeader(ResponseEntity<String> response) {
        String location = response.getHeaders().getFirst("Location");
        if (location == null) {
            throw new RuntimeException("Falha ao criar usuário: response sem Location header");
        }
        return extractUserIdFromLocation(location);
    }

    private String getUserIdByEmail(String email) {
        Map<String, Object> createdUser = getUserByEmail(email);
        if (createdUser != null) {
            return (String) createdUser.get("id");
        }
        throw new RuntimeException("Não foi possível obter o ID do usuário criado");
    }

    private Map<String, Object> extractFirstUser(List<Map<String, Object>> users) {
        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    private String buildUserByIdUrl(String keycloakUserId) {
        return keycloakProperties.getAdminUsersEndpoint() + "/" + keycloakUserId;
    }

    private String buildUserByEmailUrl(String email) {
        return keycloakProperties.getAdminUsersEndpoint() + "?email=" + email;
    }

    private String buildUserByUsernameUrl(String username) {
        return keycloakProperties.getAdminUsersEndpoint() + "?username=" + username + "&exact=true";
    }

    private String buildIdentityProviderUrl(String keycloakUserId, String identityProvider) {
        return keycloakProperties.getAdminUsersEndpoint() + "/" + keycloakUserId +
               "/federated-identity/" + identityProvider;
    }

    private HttpEntity<Void> buildAuthenticatedGetRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminAccessToken());
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Void> buildAuthenticatedDeleteRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminAccessToken());
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Map<String, Object>> buildAuthenticatedPostRequest(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Map<String, Object>> buildAuthenticatedPutRequest(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<MultiValueMap<String, String>> buildFormRequest(MultiValueMap<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(body, headers);
    }

    private record UserData(String email, String firstName, String lastName, Map<String, Object> attributes) {}
}
