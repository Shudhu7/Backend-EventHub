package com.eventhub.service.impl;

import com.eventhub.dto.UserDTO;
import com.eventhub.model.entity.Role;
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
import com.eventhub.dto.PaymentRequest;
import com.eventhub.dto.PaymentResponse;
import com.eventhub.dto.RefundRequest;
import java.util.List;
import java.util.Optional;
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
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Update allowed fields
        if (userDTO.getName() != null) {
            user.setName(userDTO.getName());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDTO);
    }
    
    @Override
    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
        Page<User> users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            keyword, keyword, pageable);
        return users.map(this::convertToDTO);
    }
    
    @Override
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setIsActive(!user.getIsActive());
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Soft delete by deactivating the user
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    @Override
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findByIsActive(true).size();
        long inactiveUsers = userRepository.findByIsActive(false).size();
        long totalAdmins = userRepository.countUsersByAdminRole();
        
        return new UserStatistics(totalUsers, activeUsers, inactiveUsers, totalAdmins);
    }
    
    @Override
    public UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setIsActive(user.getIsActive());
        
        // Convert roles to string set
        userDTO.setRoles(user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toSet()));
        
        return userDTO;
    }
    
    @Override
    public User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setIsActive(userDTO.getIsActive());
        
        return user;
    }
}