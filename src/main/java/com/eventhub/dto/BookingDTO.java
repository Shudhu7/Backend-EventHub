package com.eventhub.dto;

import com.eventhub.model.entity.Booking;
import com.eventhub.model.entity.Payment;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Long id;
    private String ticketId;
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotNull(message = "Number of tickets is required")
    @Min(value = 1, message = "At least 1 ticket must be booked")
    private Integer numberOfTickets;
    
    private BigDecimal totalAmount;
    private BigDecimal serviceFee;
    private Booking.BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Event details for display
    private String eventTitle;
    private String eventLocation;
    private String eventImage;
    private String eventCategory;
    
    // User details
    private String userName;
    private String userEmail;
    
    // Payment details
    private String transactionId;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus paymentStatus;
}