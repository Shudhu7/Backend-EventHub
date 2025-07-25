package com.eventhub.service;

import com.eventhub.dto.LoginRequest;
import com.eventhub.dto.RegisterRequest;
import com.eventhub.dto.UserDTO;

public interface AuthService {
    
    /**
     * Authenticate user and return JWT token
     */
    String authenticateUser(LoginRequest loginRequest);
    
    /**
     * Register new user
     */
    UserDTO registerUser(RegisterRequest registerRequest);
    
    /**
     * Register new admin (only for system initialization)
     */
    UserDTO registerAdmin(RegisterRequest registerRequest);
    
    /**
     * Validate JWT token
     */
    boolean validateToken(String token);
    
    /**
     * Extract username from JWT token
     */
    String getUsernameFromToken(String token);
    
    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);
}