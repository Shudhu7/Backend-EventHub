package com.eventhub.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    private Long userId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User details for display
    private String userName;
    private String userEmail;
    
    // Event details for display
    private String eventTitle;
    private String eventImage;
}