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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        return convertToDTO(savedReview);
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
        
        // Update review
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        
        Review updatedReview = reviewRepository.save(review);
        return convertToDTO(updatedReview);
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
        
        reviewRepository.delete(review);
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