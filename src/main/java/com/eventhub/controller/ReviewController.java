package com.eventhub.controller;

import com.eventhub.dto.ReviewDTO;
import com.eventhub.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            ReviewDTO createdReview = reviewService.createReview(reviewDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Review created successfully");
            response.put("data", createdReview);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            ReviewDTO updatedReview = reviewService.updateReview(id, reviewDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Review updated successfully");
            response.put("data", updatedReview);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        try {
            ReviewDTO reviewDTO = reviewService.getReviewById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", reviewDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getReviewsByEventId(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            if (page >= 0 && size > 0) {
                Sort sort = sortDir.equalsIgnoreCase("desc") 
                    ? Sort.by(sortBy).descending() 
                    : Sort.by(sortBy).ascending();
                
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<ReviewDTO> reviews = reviewService.getReviewsByEventId(eventId, pageable);
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("data", reviews.getContent());
                response.put("currentPage", reviews.getNumber());
                response.put("totalItems", reviews.getTotalElements());
                response.put("totalPages", reviews.getTotalPages());
                
                return ResponseEntity.ok(response);
            } else {
                List<ReviewDTO> reviews = reviewService.getReviewsByEventId(eventId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("data", reviews);
                response.put("count", reviews.size());
                
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/user/current")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUserReviews() {
        try {
            List<ReviewDTO> reviews = reviewService.getCurrentUserReviews();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", reviews);
            response.put("count", reviews.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReviewsByUserId(@PathVariable Long userId) {
        try {
            List<ReviewDTO> reviews = reviewService.getReviewsByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", reviews);
            response.put("count", reviews.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Review deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @GetMapping("/event/{eventId}/average-rating")
    public ResponseEntity<?> getAverageRating(@PathVariable Long eventId) {
        try {
            Double averageRating = reviewService.getAverageRatingByEventId(eventId);
            Long reviewCount = reviewService.getReviewCountByEventId(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("averageRating", averageRating != null ? averageRating : 0.0);
            response.put("reviewCount", reviewCount != null ? reviewCount : 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/event/{eventId}/rating-distribution")
    public ResponseEntity<?> getRatingDistribution(@PathVariable Long eventId) {
        try {
            List<ReviewService.RatingDistribution> distribution = reviewService.getRatingDistributionByEventId(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", distribution);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/can-review")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> canUserReviewEvent(@RequestParam Long eventId, @RequestParam Long userId) {
        try {
            boolean canReview = reviewService.canUserReviewEvent(userId, eventId);
            boolean hasReviewed = reviewService.hasUserReviewedEvent(userId, eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("canReview", canReview && !hasReviewed);
            response.put("hasReviewed", hasReviewed);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}