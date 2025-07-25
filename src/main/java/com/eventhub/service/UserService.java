package com.eventhub.service;

import com.eventhub.dto.UserDTO;
import com.eventhub.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    
    /**
     * Get user by ID
     */
    UserDTO getUserById(Long id);
    
    /**
     * Get user by email
     */
    UserDTO getUserByEmail(String email);
    
    /**
     * Get current user profile
     */
    UserDTO getCurrentUserProfile();
    
    /**
     * Update user profile
     */
    UserDTO updateUserProfile(Long id, UserDTO userDTO);
    
    /**
     * Get all users (Admin only)
     */
    List<UserDTO> getAllUsers();
    
    /**
     * Get users with pagination (Admin only)
     */
    Page<UserDTO> getAllUsers(Pageable pageable);
    
    /**
     * Search users by name or email (Admin only)
     */
    Page<UserDTO> searchUsers(String keyword, Pageable pageable);
    
    /**
     * Activate/Deactivate user (Admin only)
     */
    UserDTO toggleUserStatus(Long id);
    
    /**
     * Delete user (Admin only)
     */
    void deleteUser(Long id);
    
    /**
     * Get user statistics (Admin only)
     */
    UserStatistics getUserStatistics();
    
    /**
     * Convert entity to DTO
     */
    UserDTO convertToDTO(User user);
    
    /**
     * Convert DTO to entity
     */
    User convertToEntity(UserDTO userDTO);
    
    /**
     * Inner class for user statistics
     */
    class UserStatistics {
        private long totalUsers;
        private long activeUsers;
        private long inactiveUsers;
        private long totalAdmins;
        
        // Constructors
        public UserStatistics() {}
        
        public UserStatistics(long totalUsers, long activeUsers, long inactiveUsers, long totalAdmins) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
            this.totalAdmins = totalAdmins;
        }
        
        // Getters and Setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        
        public long getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }
        
        public long getTotalAdmins() { return totalAdmins; }
        public void setTotalAdmins(long totalAdmins) { this.totalAdmins = totalAdmins; }
    }
}