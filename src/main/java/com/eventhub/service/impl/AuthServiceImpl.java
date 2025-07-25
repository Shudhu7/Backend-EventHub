package com.eventhub.service.impl;

import com.eventhub.dto.LoginRequest;
import com.eventhub.dto.RegisterRequest;
import com.eventhub.dto.UserDTO;
import com.eventhub.model.entity.Role;
import com.eventhub.model.entity.User;
import com.eventhub.repository.RoleRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.service.AuthService;
import com.eventhub.service.UserService;
import com.eventhub.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    @Override
    public String authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.getIsActive()) {
                throw new RuntimeException("Account is deactivated");
            }
            
            String role = user.getRoles().iterator().next().getName().name();
            return jwtUtil.generateToken(userDetails.getUsername(), role);
            
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
    
    @Override
    public UserDTO registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }
        
        // Create new user account
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());
        user.setIsActive(true);
        
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("User Role not set."));
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        return userService.convertToDTO(savedUser);
    }
    
    @Override
    public UserDTO registerAdmin(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }
        
        // Create new admin account
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());
        user.setIsActive(true);
        
        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
            .orElseThrow(() -> new RuntimeException("Admin Role not set."));
        
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        return userService.convertToDTO(savedUser);
    }
    
    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
    
    @Override
    public String getUsernameFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}