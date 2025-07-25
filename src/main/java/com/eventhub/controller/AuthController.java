package com.eventhub.controller;

import com.eventhub.dto.LoginRequest;
import com.eventhub.dto.RegisterRequest;
import com.eventhub.dto.UserDTO;
import com.eventhub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String jwt = authService.authenticateUser(loginRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User authenticated successfully");
            response.put("token", jwt);
            response.put("type", "Bearer");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            UserDTO userDTO = authService.registerUser(registerRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User registered successfully");
            response.put("user", userDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            UserDTO userDTO = authService.registerAdmin(registerRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Admin registered successfully");
            response.put("user", userDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            boolean isValid = authService.validateToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("valid", isValid);
            
            if (isValid) {
                String username = authService.getUsernameFromToken(token);
                response.put("username", username);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("valid", false);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam String email) {
        try {
            boolean exists = authService.existsByEmail(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("available", !exists);
            response.put("message", exists ? "Email is already taken" : "Email is available");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}