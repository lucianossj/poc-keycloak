package com.example.backend.controller;

import com.example.backend.model.LoginResponse;
import com.example.backend.model.dto.LoginRequestDTO;
import com.example.backend.model.dto.RegisterRequestDTO;
import com.example.backend.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthorizationController {

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping(value = "/url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> getUrl() {
        return ResponseEntity.ok(authorizationService.getUrl());
    }

    @PostMapping("/token")
    public ResponseEntity<?> exchangeCodeForToken(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        return ResponseEntity.ok(authorizationService.exchangeCodeForToken(code));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String idToken = body.get("id_token");
        return ResponseEntity.ok(authorizationService.logout(idToken));
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> userInfo(@RequestHeader("Authorization") String bearerToken) {
        return ResponseEntity.ok(authorizationService.getUserInfo(bearerToken));
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        return ResponseEntity.ok(authorizationService.login(loginRequest));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO registerRequest) {
        return ResponseEntity.ok(authorizationService.register(registerRequest));
    }
}
