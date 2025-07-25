package com.eventhub.dto;

import com.eventhub.model.entity.Payment;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class CreateBookingRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotNull(message = "Number of tickets is required")
    @Min(value = 1, message = "At least 1 ticket must be booked")
    private Integer numberOfTickets;
    
    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;
    
    // Optional payment details for card payments
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardHolderName;
    
    // Optional UPI details
    private String upiId;
}