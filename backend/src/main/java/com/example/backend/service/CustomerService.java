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
                .collect(Collectors.toList());
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
        log.info("Criando novo cliente: {}", dto.getEmail());
        
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        if (dto.getDocument() != null && customerRepository.existsByDocument(dto.getDocument())) {
            throw new RuntimeException("Documento já cadastrado");
        }
        
        if (dto.getKeycloakUserId() != null && customerRepository.existsByKeycloakUserId(dto.getKeycloakUserId())) {
            throw new RuntimeException("Usuário Keycloak já possui cadastro");
        }
        
        Customer customer = Customer.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .birthDate(dto.getBirthDate())
                .document(dto.getDocument())
                .keycloakUserId(dto.getKeycloakUserId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        customer = customerRepository.save(customer);
        log.info("Cliente criado com sucesso: {}", customer.getId());
        
        return toDTO(customer);
    }
    
    public CustomerDTO update(String id, CustomerDTO dto) {
        log.info("Atualizando cliente: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
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
        
        customer.setUpdatedAt(LocalDateTime.now());
        customer = customerRepository.save(customer);
        
        log.info("Cliente atualizado com sucesso: {}", id);
        return toDTO(customer);
    }
    
    public CustomerDTO updateInfo(String keycloakUserId, UpdateCustomerInfoDTO dto) {
        log.info("Atualizando informações do cliente: {}", keycloakUserId);
        
        Customer customer = customerRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        log.debug("Customer encontrado - ID MongoDB: {}, Email: {}", customer.getId(), customer.getEmail());
        
        String cleanDocument = null;
        if (dto.getDocument() != null) {
            cleanDocument = dto.getDocument().replaceAll("[^0-9]", "");
            log.debug("CPF limpo: {} → {}", dto.getDocument(), cleanDocument);
            
            if (!cleanDocument.equals(customer.getDocument())) {
                customerRepository.findByDocument(cleanDocument).ifPresent(existing -> {
                    if (!existing.getKeycloakUserId().equals(keycloakUserId)) {
                        throw new RuntimeException("CPF já cadastrado para outro usuário");
                    }
                });
            }
            
            customer.setDocument(cleanDocument);
        }
        
        if (dto.getBirthDate() != null) {
            customer.setBirthDate(dto.getBirthDate());
        }
        
        customer.setUpdatedAt(LocalDateTime.now());
        customer = customerRepository.save(customer);
        
        log.info("✅ Informações salvas no MongoDB");
        
        if (cleanDocument != null || dto.getBirthDate() != null) {
            try {
                java.util.Map<String, java.util.List<String>> attributes = new java.util.HashMap<>();
                
                if (cleanDocument != null) {
                    attributes.put("document", java.util.List.of(cleanDocument));
                }
                
                if (dto.getBirthDate() != null) {
                    attributes.put("birthDate", java.util.List.of(dto.getBirthDate().toString()));
                }
                
                log.debug("Sincronizando atributos customizados com Keycloak: {}", attributes);
                
                keycloakAdminService.updateCustomAttributes(keycloakUserId, attributes);
                
            } catch (Exception e) {
                log.error("❌ Erro ao sincronizar atributos com Keycloak (dados salvos no MongoDB): {}", e.getMessage());
            }
        }
        
        log.info("✅ Atualização concluída com sucesso");
        return toDTO(customer);
    }
    
    public void delete(String id) {
        log.info("Deletando cliente: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        String keycloakUserId = customer.getKeycloakUserId();
        
        customerRepository.deleteById(id);
        log.info("Cliente deletado do MongoDB: {}", id);
        
        if (keycloakUserId != null) {
            try {
                keycloakAdminService.deleteUser(keycloakUserId);
                log.info("Usuário deletado do Keycloak: {}", keycloakUserId);
            } catch (Exception e) {
                log.error("Erro ao deletar usuário do Keycloak (MongoDB já foi deletado): {}", e.getMessage());
            }
        }
        
        log.info("✅ Cliente deletado com sucesso");
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
