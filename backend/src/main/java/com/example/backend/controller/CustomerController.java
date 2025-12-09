package com.example.backend.controller;

import com.example.backend.model.dto.CustomerDTO;
import com.example.backend.model.dto.UpdateCustomerInfoDTO;
import com.example.backend.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    
    private final CustomerService customerService;
    
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> findAll() {
        log.info("GET /api/customers - Listando todos os clientes");
        return ResponseEntity.ok(customerService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> findById(@PathVariable String id) {
        log.info("GET /api/customers/{} - Buscando cliente por ID", id);
        try {
            return ResponseEntity.ok(customerService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/by-email/{email}")
    public ResponseEntity<CustomerDTO> findByEmail(@PathVariable String email) {
        log.info("GET /api/customers/by-email/{} - Buscando cliente por email", email);
        try {
            return ResponseEntity.ok(customerService.findByEmail(email));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/by-keycloak/{keycloakUserId}")
    public ResponseEntity<CustomerDTO> findByKeycloakUserId(@PathVariable String keycloakUserId) {
        log.info("GET /api/customers/by-keycloak/{} - Buscando cliente por Keycloak ID", keycloakUserId);
        try {
            return ResponseEntity.ok(customerService.findByKeycloakUserId(keycloakUserId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<CustomerDTO> create(@Valid @RequestBody CustomerDTO dto) {
        log.info("POST /api/customers - Criando novo cliente: {}", dto.getEmail());
        try {
            CustomerDTO created = customerService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            log.error("Erro ao criar cliente: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> update(@PathVariable String id, @Valid @RequestBody CustomerDTO dto) {
        log.info("PUT /api/customers/{} - Atualizando cliente", id);
        try {
            CustomerDTO updated = customerService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Erro ao atualizar cliente: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/update-info/{keycloakUserId}")
    public ResponseEntity<CustomerDTO> updateInfo(
            @PathVariable String keycloakUserId,
            @Valid @RequestBody UpdateCustomerInfoDTO dto) {
        
        log.info("PATCH /api/customers/update-info/{}", keycloakUserId);
        
        try {
            CustomerDTO updated = customerService.updateInfo(keycloakUserId, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Erro ao atualizar informações: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("DELETE /api/customers/{} - Deletando cliente", id);
        try {
            customerService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erro ao deletar cliente: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
