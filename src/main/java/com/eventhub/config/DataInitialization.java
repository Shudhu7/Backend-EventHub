package com.eventhub.config;

import com.eventhub.model.entity.Role;
import com.eventhub.model.entity.User;
import com.eventhub.repository.RoleRepository;
import com.eventhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitialization implements CommandLineRunner {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        if (!roleRepository.existsByName(Role.RoleName.ROLE_USER)) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USER);
            roleRepository.save(userRole);
        }
        
        if (!roleRepository.existsByName(Role.RoleName.ROLE_ADMIN)) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }
        
        // Create default admin user if it doesn't exist
        if (!userRepository.existsByEmail("admin@eventhub.com")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@eventhub.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setPhone("+91-9876543210");
            admin.setIsActive(true);
            
            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin Role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            
            userRepository.save(admin);
            System.out.println("Default admin user created: admin@eventhub.com / admin123");
        }
        
        // Create test user if it doesn't exist
        if (!userRepository.existsByEmail("user@eventhub.com")) {
            User user = new User();
            user.setName("Test User");
            user.setEmail("user@eventhub.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setPhone("+91-9876543211");
            user.setIsActive(true);
            
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("User Role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);
            
            userRepository.save(user);
            System.out.println("Default test user created: user@eventhub.com / user123");
        }
    }
}