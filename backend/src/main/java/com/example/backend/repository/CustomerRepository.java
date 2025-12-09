package com.example.backend.repository;

import com.example.backend.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    
    Optional<Customer> findByEmail(String email);
    
    Optional<Customer> findByKeycloakUserId(String keycloakUserId);
    
    Optional<Customer> findByDocument(String document);
    
    boolean existsByEmail(String email);
    
    boolean existsByDocument(String document);
    
    boolean existsByKeycloakUserId(String keycloakUserId);
}
