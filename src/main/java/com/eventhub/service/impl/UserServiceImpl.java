package com.eventhub.service.impl;

import com.eventhub.dto.UserDTO;
import com.eventhub.model.entity.User;
import com.eventhub.repository.UserRepository;
import com.eventhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDTO(user);
    }
    
    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return convertToDTO(user);
    }
    
    @Override
    public UserDTO getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return getUserByEmail(email);
    }
    
    @Override
    public UserDTO updateUserProfile(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Update allowed fields (based on actual User entity)
        existingUser.setName(userDTO.getName());
        existingUser.setPhone(userDTO.getPhone());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }
    
    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAllByOrderByCreatedAtDesc();
        return users.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAllByOrderByCreatedAtDesc(pageable);
        return users.map(this::convertToDTO);
    }
    
    @Override
    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(keyword, pageable);
        return users.map(this::convertToDTO);
    }
    
    @Override
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setIsActive(!user.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Soft delete by deactivating the user
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    @Override
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActive(true);
        long inactiveUsers = userRepository.countByIsActive(false);
        
        // Use the correct role names and safe counting
        long totalAdmins = 0;
        try {
            totalAdmins = userRepository.countByRoleName("ROLE_ADMIN");
        } catch (Exception e) {
            // If role counting fails, use the backup method
            try {
                totalAdmins = userRepository.countUsersByAdminRole();
            } catch (Exception ex) {
                // If both fail, set to 0
                totalAdmins = 0;
            }
        }
        
        return new UserStatistics(totalUsers, activeUsers, inactiveUsers, totalAdmins);
    }
    
    @Override
    public UserDTO convertToDTO(User user) {
        if (user == null) return null;
        
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setIsActive(user.getIsActive());
        
        // Convert roles to string set
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            userDTO.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        }
        
        return userDTO;
    }
    
    @Override
    public User convertToEntity(UserDTO userDTO) {
        if (userDTO == null) return null;
        
        User user = new User();
        user.setId(userDTO.getId());
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setIsActive(userDTO.getIsActive() != null ? userDTO.getIsActive() : true);
        user.setCreatedAt(userDTO.getCreatedAt());
        user.setUpdatedAt(userDTO.getUpdatedAt());
        
        return user;
    }
}