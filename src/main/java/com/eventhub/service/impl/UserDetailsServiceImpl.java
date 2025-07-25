package com.eventhub.service.impl;

import com.eventhub.model.entity.User;
import com.eventhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eventhub.dto.PaymentRequest;
import com.eventhub.dto.PaymentResponse;
import com.eventhub.dto.RefundRequest;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
        
        return UserPrincipal.create(user);
    }
    
    public static class UserPrincipal implements UserDetails {
        private Long id;
        private String name;
        private String email;
        private String password;
        private List<GrantedAuthority> authorities;
        private Boolean isActive;
        
        public UserPrincipal(Long id, String name, String email, String password, 
                           List<GrantedAuthority> authorities, Boolean isActive) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.authorities = authorities;
            this.isActive = isActive;
        }
        
        public static UserPrincipal create(User user) {
            List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
            
            return new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getIsActive()
            );
        }
        
        @Override
        public List<GrantedAuthority> getAuthorities() {
            return authorities;
        }
        
        @Override
        public String getPassword() {
            return password;
        }
        
        @Override
        public String getUsername() {
            return email;
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return isActive;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
}