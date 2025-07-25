package com.eventhub.service;

import com.eventhub.dto.ReviewDTO;
import com.eventhub.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    
    /**
     * Create new review
     */
    ReviewDTO createReview(ReviewDTO reviewDTO);
    
    /**
     * Update existing review
     */
    ReviewDTO updateReview(Long id, ReviewDTO reviewDTO);
    
    /**
     * Get review by ID
     */
    ReviewDTO getReviewById(Long id);
    
    /**
     * Get reviews by event ID
     */
    List<ReviewDTO> getReviewsByEventId(Long eventId);
    
    /**
     * Get reviews by event ID with pagination
     */
    Page<ReviewDTO> getReviewsByEventId(Long eventId, Pageable pageable);
    
    /**
     * Get reviews by user ID
     */
    List<ReviewDTO> getReviewsByUserId(Long userId);
    
    /**
     * Get current user's reviews
     */
    List<ReviewDTO> getCurrentUserReviews();
    
    /**
     * Delete review
     */
    void deleteReview(Long id);
    
    /**
     * Get average rating for event
     */
    Double getAverageRatingByEventId(Long eventId);
    
    /**
     * Get review count for event
     */
    Long getReviewCountByEventId(Long eventId);
    
    /**
     * Get rating distribution for event
     */
    List<RatingDistribution> getRatingDistributionByEventId(Long eventId);
    
    /**
     * Check if user can review event (has confirmed booking)
     */
    boolean canUserReviewEvent(Long userId, Long eventId);
    
    /**
     * Check if user already reviewed event
     */
    boolean hasUserReviewedEvent(Long userId, Long eventId);
    
    /**
     * Convert entity to DTO
     */
    ReviewDTO convertToDTO(Review review);
    
    /**
     * Convert DTO to entity
     */
    Review convertToEntity(ReviewDTO reviewDTO);
    
    /**
     * Inner class for rating distribution
     */
    class RatingDistribution {
        private Integer rating;
        private Long count;
        
        public RatingDistribution() {}
        
        public RatingDistribution(Integer rating, Long count) {
            this.rating = rating;
            this.count = count;
        }
        
        // Getters and Setters
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}