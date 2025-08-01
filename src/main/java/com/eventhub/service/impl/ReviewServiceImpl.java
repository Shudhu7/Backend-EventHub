package com.eventhub.service.impl;

import com.eventhub.dto.ReviewDTO;
import com.eventhub.model.entity.Event;
import com.eventhub.model.entity.Review;
import com.eventhub.model.entity.User;
import com.eventhub.repository.BookingRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.ReviewRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.service.ReviewService;
import com.eventhub.service.WebSocketService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; 
import java.util.HashMap; 
import java.util.List;
import java.util.Map; 
import java.util.Optional;
import java.util.stream.Collectors;
import com.eventhub.dto.PaymentRequest;
import com.eventhub.dto.PaymentResponse;
import com.eventhub.dto.RefundRequest;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    // ADD THIS WEBSOCKET SERVICE INJECTION
    @Autowired
    private WebSocketService webSocketService;
    
    @Override
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get event
        Event event = eventRepository.findById(reviewDTO.getEventId())
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        // Check if user can review this event (has confirmed booking)
        if (!canUserReviewEvent(user.getId(), event.getId())) {
            throw new RuntimeException("You can only review events you have attended");
        }
        
        // Check if user already reviewed this event
        if (hasUserReviewedEvent(user.getId(), event.getId())) {
            throw new RuntimeException("You have already reviewed this event");
        }
        
        // Create review
        Review review = new Review();
        review.setUser(user);
        review.setEvent(event);
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        
        Review savedReview = reviewRepository.save(review);
        ReviewDTO result = convertToDTO(savedReview);
        
        // ADD REAL-TIME NOTIFICATION CODE
        try {
            // Send review notification to event subscribers
            Map<String, Object> reviewNotification = new HashMap<>();
            reviewNotification.put("type", "NEW_REVIEW");
            reviewNotification.put("eventId", event.getId());
            reviewNotification.put("review", result);
            reviewNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendEventUpdate(event.getId().toString(), reviewNotification);
            
            // Send global notification for new review
            Map<String, Object> globalNotification = new HashMap<>();
            globalNotification.put("type", "REVIEW_ADDED");
            globalNotification.put("eventId", event.getId());
            globalNotification.put("eventTitle", event.getTitle());
            globalNotification.put("rating", result.getRating());
            globalNotification.put("userName", result.getUserName());
            globalNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendGlobalEventNotification(globalNotification);
            
            // Update event rating statistics in real-time
            Double newAverageRating = getAverageRatingByEventId(event.getId());
            Long newReviewCount = getReviewCountByEventId(event.getId());
            
            Map<String, Object> ratingUpdate = new HashMap<>();
            ratingUpdate.put("type", "RATING_UPDATE");
            ratingUpdate.put("eventId", event.getId());
            ratingUpdate.put("averageRating", newAverageRating != null ? newAverageRating : 0.0);
            ratingUpdate.put("reviewCount", newReviewCount != null ? newReviewCount : 0);
            ratingUpdate.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendEventUpdate(event.getId().toString(), ratingUpdate);
            
        } catch (Exception e) {
            // Log the error but don't fail the review creation
            System.err.println("Failed to send review WebSocket notification: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public ReviewDTO updateReview(Long id, ReviewDTO reviewDTO) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Check if current user owns this review
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own reviews");
        }
        
        // Store old rating for comparison
        Integer oldRating = review.getRating();
        
        // Update review
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        
        Review updatedReview = reviewRepository.save(review);
        ReviewDTO result = convertToDTO(updatedReview);
        
        // ADD REAL-TIME UPDATE NOTIFICATION CODE
        try {
            // Send review update notification
            Map<String, Object> updateNotification = new HashMap<>();
            updateNotification.put("type", "REVIEW_UPDATED");
            updateNotification.put("eventId", review.getEvent().getId());
            updateNotification.put("reviewId", id);
            updateNotification.put("review", result);
            updateNotification.put("oldRating", oldRating);
            updateNotification.put("newRating", reviewDTO.getRating());
            updateNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendEventUpdate(review.getEvent().getId().toString(), updateNotification);
            
            // Update rating statistics if rating changed
            if (!oldRating.equals(reviewDTO.getRating())) {
                Double newAverageRating = getAverageRatingByEventId(review.getEvent().getId());
                Long newReviewCount = getReviewCountByEventId(review.getEvent().getId());
                
                Map<String, Object> ratingUpdate = new HashMap<>();
                ratingUpdate.put("type", "RATING_UPDATE");
                ratingUpdate.put("eventId", review.getEvent().getId());
                ratingUpdate.put("averageRating", newAverageRating != null ? newAverageRating : 0.0);
                ratingUpdate.put("reviewCount", newReviewCount != null ? newReviewCount : 0);
                ratingUpdate.put("timestamp", LocalDateTime.now());
                
                webSocketService.sendEventUpdate(review.getEvent().getId().toString(), ratingUpdate);
            }
            
        } catch (Exception e) {
            // Log the error but don't fail the review update
            System.err.println("Failed to send review update WebSocket notification: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        return convertToDTO(review);
    }
    
    @Override
    public List<ReviewDTO> getReviewsByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        List<Review> reviews = reviewRepository.findByEventOrderByCreatedAtDesc(event);
        return reviews.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ReviewDTO> getReviewsByEventId(Long eventId, Pageable pageable) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        Page<Review> reviews = reviewRepository.findByEvent(event, pageable);
        return reviews.map(this::convertToDTO);
    }
    
    @Override
    public List<ReviewDTO> getReviewsByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewDTO> getCurrentUserReviews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return getReviewsByUserId(user.getId());
    }
    
    @Override
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Check if current user owns this review or is admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean isAdmin = currentUser.getRoles().stream()
            .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"));
        
        if (!review.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("You can only delete your own reviews");
        }
        
        // Store event info before deletion
        Event event = review.getEvent();
        Long eventId = event.getId();
        
        reviewRepository.delete(review);
        
        // ADD REAL-TIME DELETE NOTIFICATION CODE
        try {
            // Send review deletion notification
            Map<String, Object> deleteNotification = new HashMap<>();
            deleteNotification.put("type", "REVIEW_DELETED");
            deleteNotification.put("eventId", eventId);
            deleteNotification.put("reviewId", id);
            deleteNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendEventUpdate(eventId.toString(), deleteNotification);
            
            // Update rating statistics after deletion
            Double newAverageRating = getAverageRatingByEventId(eventId);
            Long newReviewCount = getReviewCountByEventId(eventId);
            
            Map<String, Object> ratingUpdate = new HashMap<>();
            ratingUpdate.put("type", "RATING_UPDATE");
            ratingUpdate.put("eventId", eventId);
            ratingUpdate.put("averageRating", newAverageRating != null ? newAverageRating : 0.0);
            ratingUpdate.put("reviewCount", newReviewCount != null ? newReviewCount : 0);
            ratingUpdate.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendEventUpdate(eventId.toString(), ratingUpdate);
            
        } catch (Exception e) {
            // Log the error but don't fail the review deletion
            System.err.println("Failed to send review delete WebSocket notification: " + e.getMessage());
        }
    }
    
    @Override
    public Double getAverageRatingByEventId(Long eventId) {
        return reviewRepository.getAverageRatingByEventId(eventId);
    }
    
    @Override
    public Long getReviewCountByEventId(Long eventId) {
        return reviewRepository.getReviewCountByEventId(eventId);
    }
    
    @Override
    public List<RatingDistribution> getRatingDistributionByEventId(Long eventId) {
        List<Object[]> distribution = reviewRepository.getRatingDistributionByEventId(eventId);
        return distribution.stream()
            .map(data -> new RatingDistribution((Integer) data[0], (Long) data[1]))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean canUserReviewEvent(Long userId, Long eventId) {
        // Check if user has a confirmed booking for this event
        return bookingRepository.findByUserIdAndEventId(userId, eventId)
            .map(booking -> booking.getStatus().equals(com.eventhub.model.entity.Booking.BookingStatus.CONFIRMED))
            .orElse(false);
    }
    
    @Override
    public boolean hasUserReviewedEvent(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        return reviewRepository.existsByUserAndEvent(user, event);
    }
    
    @Override
    public ReviewDTO convertToDTO(Review review) {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(review.getId());
        reviewDTO.setEventId(review.getEvent().getId());
        reviewDTO.setUserId(review.getUser().getId());
        reviewDTO.setRating(review.getRating());
        reviewDTO.setComment(review.getComment());
        reviewDTO.setCreatedAt(review.getCreatedAt());
        reviewDTO.setUpdatedAt(review.getUpdatedAt());
        
        // User details
        reviewDTO.setUserName(review.getUser().getName());
        reviewDTO.setUserEmail(review.getUser().getEmail());
        
        // Event details
        reviewDTO.setEventTitle(review.getEvent().getTitle());
        reviewDTO.setEventImage(review.getEvent().getImage());
        
        return reviewDTO;
    }
    
    @Override
    public Review convertToEntity(ReviewDTO reviewDTO) {
        Review review = new Review();
        review.setId(reviewDTO.getId());
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        
        return review;
    }
}