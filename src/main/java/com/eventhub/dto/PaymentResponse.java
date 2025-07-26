package com.eventhub.dto;

import com.eventhub.model.entity.Payment;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private String transactionId;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus status;
    private String message;
    private String paymentGatewayResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long bookingId;
    private Long userId;
    private Long eventId;
    private String ticketId;
}