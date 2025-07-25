package com.eventhub.dto;

import com.eventhub.model.entity.Payment;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
    
    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    // Card payment details
    @Size(min = 16, max = 19, message = "Card number must be between 16-19 digits")
    private String cardNumber;
    
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Expiry date must be in MM/YY format")
    private String expiryDate;
    
    @Size(min = 3, max = 4, message = "CVV must be 3-4 digits")
    private String cvv;
    
    @Size(max = 100, message = "Card holder name must not exceed 100 characters")
    private String cardHolderName;
    
    // UPI details
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+$", message = "Invalid UPI ID format")
    private String upiId;
    
    // Net banking details
    private String bankCode;
}