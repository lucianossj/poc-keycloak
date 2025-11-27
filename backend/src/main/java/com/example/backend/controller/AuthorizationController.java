package com.example.backend.controller;

import com.example.backend.model.LoginRequest;
import com.example.backend.model.LoginResponse;
import com.example.backend.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "false")
public class AuthorizationController {

    @Autowired
    private AuthorizationService authorizationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authorizationService.login(request));
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
}
