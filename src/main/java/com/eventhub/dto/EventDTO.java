package com.eventhub.dto;

import com.eventhub.model.entity.Event;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
public class EventDTO {
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    @NotNull(message = "Date is required")
    @Future(message = "Event date must be in the future")
    private LocalDate date;
    
    @NotNull(message = "Time is required")
    private LocalTime time;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;
    
    private Integer availableSeats;
    
    @NotNull(message = "Category is required")
    private Event.Category category;
    
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Double averageRating;
    private Integer totalReviews;
}