package com.example.backend.service;

import com.example.backend.integration.KeycloakIntegration;
import com.example.backend.model.LoginResponse;
import com.example.backend.model.dto.CustomerDTO;
import com.example.backend.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
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
        
        boolean isFirstLogin = !customerRepository.existsByKeycloakUserId(keycloakUserId);
        
        if (isFirstLogin) {
            log.info("Primeiro login detectado para usuário: {} ({})", name, email);
            
            try {
                CustomerDTO newCustomer = CustomerDTO.builder()
                        .name(name)
                        .email(email)
                        .keycloakUserId(keycloakUserId)
                        .build();
                
                customerService.create(newCustomer);
                log.info("Customer criado com sucesso no primeiro login: {}", email);
            } catch (Exception e) {
                log.error("Erro ao criar customer no primeiro login: {}", e.getMessage());
            }
        }
        
        Map<String, Object> response = new HashMap<>(tokens);
        response.put("is_first_login", isFirstLogin);
        response.put("user_info", userInfo);
        
        return response;
    }

    public Map<String, Object> logout(String idToken) {
        return keycloakIntegration.logout(idToken);
    }

    public Map<String, Object> getUserInfo(String bearerToken) {
        return keycloakIntegration.getUserInfo(bearerToken);
    }

}
