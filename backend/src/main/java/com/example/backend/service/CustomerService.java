package com.example.backend.service;

import com.example.backend.model.Customer;
import com.example.backend.model.dto.CustomerDTO;
import com.example.backend.model.dto.UpdateCustomerInfoDTO;
import com.example.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final KeycloakAdminService keycloakAdminService;
    
    public List<CustomerDTO> findAll() {
        return customerRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }
    
    public CustomerDTO findById(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return toDTO(customer);
    }
    
    public CustomerDTO findByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return toDTO(customer);
    }
    
    public CustomerDTO findByKeycloakUserId(String keycloakUserId) {
        Customer customer = customerRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return toDTO(customer);
    }
    
    public CustomerDTO create(CustomerDTO dto) {
        validateDataToCreation(dto);
        Customer customer = buildCustomer(dto);
        
        return toDTO(customerRepository.save(customer));
    }

    public CustomerDTO update(String id, CustomerDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        validateDataToUpdate(dto, customer);
        customer.setUpdatedAt(LocalDateTime.now());

        return toDTO(customerRepository.save(customer));
    }

    public CustomerDTO updateInfo(String keycloakUserId, UpdateCustomerInfoDTO dto) {
        Customer customer = findCustomerByKeycloakUserId(keycloakUserId);
        
        String cleanDocument = updateCustomerBasicInfo(customer, dto, keycloakUserId);
        customer = saveCustomerToDatabase(customer);
        
        syncWithKeycloak(keycloakUserId, cleanDocument, dto, customer);
        
        return toDTO(customer);
    }
    
    private Customer findCustomerByKeycloakUserId(String keycloakUserId) {
        return customerRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }
    
    private String updateCustomerBasicInfo(Customer customer, UpdateCustomerInfoDTO dto, String keycloakUserId) {
        String cleanDocument = updateDocumentIfProvided(customer, dto, keycloakUserId);
        updateBirthDateIfProvided(customer, dto);
        return cleanDocument;
    }
    
    private String updateDocumentIfProvided(Customer customer, UpdateCustomerInfoDTO dto, String keycloakUserId) {
        if (dto.getDocument() == null) {
            return null;
        }
        
        String cleanDocument = cleanDocumentNumber(dto.getDocument());
        validateDocumentNotInUse(cleanDocument, customer, keycloakUserId);
        customer.setDocument(cleanDocument);
        
        return cleanDocument;
    }
    
    private String cleanDocumentNumber(String document) {
        return document.replaceAll("[^0-9]", "");
    }
    
    private void validateDocumentNotInUse(String cleanDocument, Customer customer, String keycloakUserId) {
        if (cleanDocument.equals(customer.getDocument())) {
            return;
        }
        
        customerRepository.findByDocument(cleanDocument).ifPresent(existing -> {
            if (!existing.getKeycloakUserId().equals(keycloakUserId)) {
                throw new RuntimeException("CPF já cadastrado para outro usuário");
            }
        });
    }
    
    private void updateBirthDateIfProvided(Customer customer, UpdateCustomerInfoDTO dto) {
        if (dto.getBirthDate() != null) {
            customer.setBirthDate(dto.getBirthDate());
        }
    }
    
    private Customer saveCustomerToDatabase(Customer customer) {
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }
    
    private void syncWithKeycloak(String keycloakUserId, String cleanDocument, 
                                   UpdateCustomerInfoDTO dto, Customer customer) {
        if (cleanDocument == null && dto.getBirthDate() == null) {
            return;
        }
        
        try {
            updateKeycloakCustomAttributes(keycloakUserId, cleanDocument, dto);
            updateKeycloakUsernameIfNeeded(keycloakUserId, cleanDocument, customer);
        } catch (Exception e) {
            log.error("Erro ao sincronizar com Keycloak: {}", e.getMessage());
        }
    }
    
    private void updateKeycloakCustomAttributes(String keycloakUserId, String cleanDocument, 
                                                UpdateCustomerInfoDTO dto) {
        java.util.Map<String, java.util.List<String>> attributes = buildKeycloakAttributes(cleanDocument, dto);
        keycloakAdminService.updateCustomAttributes(keycloakUserId, attributes);
    }
    
    private java.util.Map<String, java.util.List<String>> buildKeycloakAttributes(
            String cleanDocument, UpdateCustomerInfoDTO dto) {
        java.util.Map<String, java.util.List<String>> attributes = new java.util.HashMap<>();
        
        if (cleanDocument != null) {
            attributes.put("document", java.util.List.of(cleanDocument));
        }
        
        if (dto.getBirthDate() != null) {
            attributes.put("birthDate", java.util.List.of(dto.getBirthDate().toString()));
        }
        
        return attributes;
    }
    
    private void updateKeycloakUsernameIfNeeded(String keycloakUserId, String cleanDocument, 
                                                 Customer customer) {
        if (cleanDocument == null) {
            return;
        }
        
        try {
            String currentUsername = fetchCurrentUsername(keycloakUserId);
            
            if (shouldUpdateUsername(currentUsername, cleanDocument)) {
                String newKeycloakUserId = performUsernameUpdate(keycloakUserId, cleanDocument, currentUsername);
                updateCustomerKeycloakIdIfChanged(customer, keycloakUserId, newKeycloakUserId);
            }
            
        } catch (Exception usernameError) {
            log.error("Erro ao atualizar username: {}", usernameError.getMessage());
        }
    }
    
    private String fetchCurrentUsername(String keycloakUserId) {
        java.util.Map<String, Object> currentUser = keycloakAdminService.getUserById(keycloakUserId);
        return currentUser != null ? (String) currentUser.get("username") : null;
    }
    
    private boolean shouldUpdateUsername(String currentUsername, String cleanDocument) {
        return currentUsername != null && !currentUsername.equals(cleanDocument);
    }
    
    private String performUsernameUpdate(String keycloakUserId, String cleanDocument, String currentUsername) {
        return keycloakAdminService.updateUsername(keycloakUserId, cleanDocument);
    }
    
    private void updateCustomerKeycloakIdIfChanged(Customer customer, String oldKeycloakUserId, 
                                                    String newKeycloakUserId) {
        if (newKeycloakUserId.equals(oldKeycloakUserId)) {
            return;
        }
        
        customer.setKeycloakUserId(newKeycloakUserId);
        customerRepository.save(customer);
    }
    
    public void delete(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        String keycloakUserId = customer.getKeycloakUserId();
        
        customerRepository.deleteById(id);
        
        if (keycloakUserId != null) {
            try {
                keycloakAdminService.deleteUser(keycloakUserId);
            } catch (Exception e) {
                log.error("Erro ao deletar usuário do Keycloak: {}", e.getMessage());
            }
        }
    }

    private static Customer buildCustomer(CustomerDTO dto) {
        return Customer.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .birthDate(dto.getBirthDate())
                .document(dto.getDocument())
                .keycloakUserId(dto.getKeycloakUserId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void validateDataToCreation(CustomerDTO dto) {
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (dto.getDocument() != null && customerRepository.existsByDocument(dto.getDocument())) {
            throw new RuntimeException("Documento já cadastrado");
        }

        if (dto.getKeycloakUserId() != null && customerRepository.existsByKeycloakUserId(dto.getKeycloakUserId())) {
            throw new RuntimeException("Usuário Keycloak já possui cadastro");
        }
    }

    private void validateDataToUpdate(CustomerDTO dto, Customer customer) {
        if (dto.getEmail() != null && !dto.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email já cadastrado");
            }
            customer.setEmail(dto.getEmail());
        }

        if (dto.getDocument() != null && !dto.getDocument().equals(customer.getDocument())) {
            if (customerRepository.existsByDocument(dto.getDocument())) {
                throw new RuntimeException("Documento já cadastrado");
            }
            customer.setDocument(dto.getDocument());
        }

        if (dto.getName() != null) {
            customer.setName(dto.getName());
        }

        if (dto.getBirthDate() != null) {
            customer.setBirthDate(dto.getBirthDate());
        }
    }

    private CustomerDTO toDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .birthDate(customer.getBirthDate())
                .document(customer.getDocument())
                .keycloakUserId(customer.getKeycloakUserId())
                .build();
    }
}
