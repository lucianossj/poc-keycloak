package com.example.backend.service;

import com.example.backend.config.KeycloakProperties;
import com.example.backend.integration.KeycloakIntegration;
import com.example.backend.model.Customer;
import com.example.backend.model.LoginResponse;
import com.example.backend.model.dto.CustomerDTO;
import com.example.backend.model.dto.LoginRequestDTO;
import com.example.backend.model.dto.RegisterRequestDTO;
import com.example.backend.repository.CustomerRepository;
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
public class AuthorizationService {

    @Autowired
    private KeycloakIntegration keycloakIntegration;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private KeycloakAdminService keycloakAdminService;
    
    @Autowired
    private KeycloakProperties keycloakProperties;
    
    @Autowired
    private RestTemplate restTemplate;

    public LoginResponse getUrl() {
        return LoginResponse.builder()
                .authUrl(keycloakIntegration.buildSocialAuthUrl())
                .build();
    }

    public Map<String, Object> exchangeCodeForToken(String code) {
        Map<String, Object> tokens = keycloakIntegration.exchangeCodeForToken(code);
        
        String accessToken = (String) tokens.get("access_token");
        Map<String, Object> userInfo = keycloakIntegration.getUserInfo("Bearer " + accessToken);
        
        String keycloakUserId = (String) userInfo.get("sub");
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        
        boolean customerExistsByKeycloakId = customerRepository.existsByKeycloakUserId(keycloakUserId);
        boolean customerExistsByEmail = customerRepository.existsByEmail(email);
        
        boolean isFirstLogin = false;
        
        if (!customerExistsByKeycloakId && customerExistsByEmail) {
            log.info("Usuário já existe com email {} mas sem keycloakUserId. Vinculando conta Google...", email);
            
            try {
                customerRepository.findByEmail(email).ifPresent(existingCustomer -> {
                    existingCustomer.setKeycloakUserId(keycloakUserId);
                    existingCustomer.setUpdatedAt(LocalDateTime.now());
                    customerRepository.save(existingCustomer);
                    log.info("Conta Google vinculada ao customer existente: {}", existingCustomer.getId());
                });
                
                isFirstLogin = shouldShowCompleteProfile(email);
                
            } catch (Exception e) {
                log.error("Erro ao vincular conta Google ao customer existente: {}", e.getMessage());
            }
            
        } else if (!customerExistsByKeycloakId && !customerExistsByEmail) {
            log.info("Primeiro login detectado para novo usuário: {} ({})", name, email);
            
            try {
                CustomerDTO newCustomer = CustomerDTO.builder()
                        .name(name)
                        .email(email)
                        .keycloakUserId(keycloakUserId)
                        .build();
                
                customerService.create(newCustomer);
                log.info("Customer criado com sucesso no primeiro login: {}", email);
                
                isFirstLogin = true;
                
            } catch (Exception e) {
                log.error("Erro ao criar customer no primeiro login: {}", e.getMessage());
            }
        } else {
            log.info("Login de usuário existente: {} ({})", name, email);
            isFirstLogin = shouldShowCompleteProfile(keycloakUserId);
        }
        
        Map<String, Object> response = new HashMap<>(tokens);
        response.put("is_first_login", isFirstLogin);
        response.put("user_info", userInfo);
        
        return response;
    }
    
    private boolean shouldShowCompleteProfile(String identifier) {
        try {
            return customerRepository.findByKeycloakUserId(identifier)
                    .or(() -> customerRepository.findByEmail(identifier))
                    .map(customer -> customer.getDocument() == null || customer.getBirthDate() == null)
                    .orElse(false);
        } catch (Exception e) {
            log.error("Erro ao verificar necessidade de complete-profile: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> logout(String idToken) {
        return keycloakIntegration.logout(idToken);
    }

    public Map<String, Object> getUserInfo(String bearerToken) {
        return keycloakIntegration.getUserInfo(bearerToken);
    }
    
    public Map<String, Object> login(LoginRequestDTO loginRequest) {
        String username = getUsernameForLogin(loginRequest.getEmail());
        String tokenUrl = keycloakProperties.getTokenEndpoint();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = buildLoginRequestBody(username, loginRequest.getPassword());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return decodeJwtOnLogin(tokenUrl, request, loginRequest);
    }

    public Map<String, Object> register(RegisterRequestDTO registerRequest) {
        log.info("Registrando novo usuário: {}", registerRequest.getEmail());
        
        if (customerRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        String cleanDocument = registerRequest.getDocument() != null ? 
            registerRequest.getDocument().replaceAll("[^0-9]", "") : null;
        
        if (cleanDocument == null || cleanDocument.isEmpty()) {
            throw new RuntimeException("CPF é obrigatório");
        }
        
        if (customerRepository.existsByDocument(cleanDocument)) {
            throw new RuntimeException("CPF já cadastrado");
        }
        
        String[] nameParts = registerRequest.getName().split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("document", List.of(cleanDocument));
        
        if (registerRequest.getBirthDate() != null) {
            attributes.put("birthDate", List.of(registerRequest.getBirthDate().toString()));
        }
        
        try {
            // IMPORTANTE: Usar CPF como username desde o início
            String keycloakUserId = keycloakAdminService.createUser(
                cleanDocument,  // username = CPF
                registerRequest.getEmail(),  // email
                firstName,
                lastName,
                attributes,
                registerRequest.getPassword()
            );
            
            CustomerDTO customer = CustomerDTO.builder()
                    .name(registerRequest.getName())
                    .email(registerRequest.getEmail())
                    .document(cleanDocument)
                    .birthDate(registerRequest.getBirthDate())
                    .keycloakUserId(keycloakUserId)
                    .build();
            
            customerService.create(customer);
            
            log.info("✅ Usuário registrado com sucesso: {}", registerRequest.getEmail());
            
            // Aguardar 500ms para garantir que o usuário esteja disponível no Keycloak
            Thread.sleep(500);
            
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .email(registerRequest.getEmail())
                    .password(registerRequest.getPassword())
                    .build();
            
            return login(loginRequest);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrompida: {}", e.getMessage());
            throw new RuntimeException("Erro interno no servidor", e);
        } catch (Exception e) {
            log.error("Erro ao registrar usuário: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao criar usuário: " + e.getMessage(), e);
        }
    }

    private String getUsernameForLogin(String email) {
        if (!email.matches("^\\d+$")) {
            return customerRepository.findByEmail(email)
                    .map(Customer::getDocument)
                    .filter(doc -> doc != null && !doc.isBlank())
                    .orElse(email);
        }
        return email;
    }

    private MultiValueMap<String, String> buildLoginRequestBody(String username, String password) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", keycloakProperties.getClientId());
        body.add("scope", "openid email profile");

        if (keycloakProperties.hasClientSecret()) {
            body.add("client_secret", keycloakProperties.getClientSecret());
        }

        body.add("username", username);
        body.add("password", password);

        return body;
    }

    private Map<String, Object> decodeJwtOnLogin(
            String tokenUrl, HttpEntity<MultiValueMap<String, String>> request, LoginRequestDTO loginRequest
    ) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> tokenResponse = response.getBody();

            String accessToken = (String) tokenResponse.get("access_token");

            String[] jwtParts = accessToken.split("\\.");
            if (jwtParts.length == 3) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(jwtParts[1]));
                Map<String, Object> claims = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class);

                String keycloakUserId = (String) claims.get("sub");
                String email = (String) claims.get("email");
                String preferredUsername = (String) claims.get("preferred_username");
                String name = (String) claims.get("name");

                boolean isFirstLogin = shouldShowCompleteProfile(keycloakUserId);

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("sub", keycloakUserId);
                userInfo.put("email", email);
                userInfo.put("preferred_username", preferredUsername);
                userInfo.put("name", name);

                Map<String, Object> result = new HashMap<>(tokenResponse);
                result.put("user_info", userInfo);
                result.put("is_first_login", isFirstLogin);

                log.info("📤 Chaves na resposta final: {}", result.keySet());
                log.info("📤 id_token na resposta final? {}", result.containsKey("id_token"));

                log.info("Login com senha realizado com sucesso para: {}", loginRequest.getEmail());
                return result;
            } else {
                throw new RuntimeException("Token JWT inválido");
            }

        } catch (Exception e) {
            log.error("Erro ao fazer login com senha: {}", e.getMessage());
            throw new RuntimeException("Credenciais inválidas", e);
        }
    }

}
