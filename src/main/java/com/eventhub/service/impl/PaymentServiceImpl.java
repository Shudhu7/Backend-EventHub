package com.eventhub.service.impl;

import com.eventhub.dto.PaymentRequest;
import com.eventhub.dto.PaymentResponse;
import com.eventhub.dto.RefundRequest;
import com.eventhub.model.entity.Booking;
import com.eventhub.model.entity.Payment;
import com.eventhub.repository.BookingRepository;
import com.eventhub.repository.PaymentRepository;
import com.eventhub.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        // Get booking
        Booking booking = bookingRepository.findById(paymentRequest.getBookingId())
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Check if payment already exists
        if (paymentRepository.findByBooking(booking).isPresent()) {
            throw new RuntimeException("Payment already exists for this booking");
        }
        
        // Validate booking status
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Payment can only be processed for pending bookings");
        }
        
        // Validate payment amount
        if (paymentRequest.getAmount().compareTo(booking.getTotalAmount()) != 0) {
            throw new RuntimeException("Payment amount does not match booking total");
        }
        
        // Create payment entity
        Payment payment = new Payment();
        payment.setTransactionId(generateTransactionId());
        payment.setBooking(booking);
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        // Process payment based on method
        Payment processedPayment;
        switch (paymentRequest.getPaymentMethod()) {
            case CARD:
                processedPayment = processCardPaymentInternal(payment, paymentRequest);
                break;
            case UPI:
                processedPayment = processUpiPaymentInternal(payment, paymentRequest);
                break;
            case NET_BANKING:
                processedPayment = processNetBankingPaymentInternal(payment, paymentRequest);
                break;
            case WALLET:
                processedPayment = processWalletPaymentInternal(payment, paymentRequest);
                break;
            default:
                throw new RuntimeException("Unsupported payment method");
        }
        
        // Update booking status if payment successful
        if (processedPayment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
        }
        
        return convertToResponse(processedPayment);
    }
    
    @Override
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new RuntimeException("Payment not found with transaction ID: " + transactionId));
        return convertToResponse(payment);
    }
    
    @Override
    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Payment payment = paymentRepository.findByBooking(booking)
            .orElseThrow(() -> new RuntimeException("Payment not found for booking"));
        
        return convertToResponse(payment);
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return payments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentResponse> getAllPayments() {
        List<Payment> payments = paymentRepository.findAllByOrderByCreatedAtDesc();
        return payments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatusOrderByCreatedAtDesc(status);
        return payments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByPaymentMethod(Payment.PaymentMethod paymentMethod) {
        List<Payment> payments = paymentRepository.findByPaymentMethodOrderByCreatedAtDesc(paymentMethod);
        return payments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments = paymentRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
        return payments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public PaymentResponse processRefund(RefundRequest refundRequest) {
        Payment payment = paymentRepository.findByTransactionId(refundRequest.getTransactionId())
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        // Validate refund conditions
        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Can only refund successful payments");
        }
        
        if (payment.getAmount().compareTo(refundRequest.getRefundAmount()) < 0) {
            throw new RuntimeException("Refund amount cannot exceed payment amount");
        }
        
        // Process refund
        try {
            // In real implementation, this would call the payment gateway's refund API
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            payment.setPaymentGatewayResponse("Refund processed successfully");
            payment.setUpdatedAt(LocalDateTime.now());
            
            // Update booking status
            Booking booking = payment.getBooking();
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
            
            Payment updatedPayment = paymentRepository.save(payment);
            return convertToResponse(updatedPayment);
            
        } catch (Exception e) {
            throw new RuntimeException("Refund processing failed: " + e.getMessage());
        }
    }
    
    @Override
    public boolean verifyPayment(String transactionId) {
        // In real implementation, this would verify with actual payment gateway
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElse(null);
        
        return payment != null && payment.getStatus() == Payment.PaymentStatus.SUCCESS;
    }
    
    @Override
    public PaymentResponse updatePaymentStatus(String transactionId, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        Payment updatedPayment = paymentRepository.save(payment);
        
        return convertToResponse(updatedPayment);
    }
    
    @Override
    public PaymentStatistics getPaymentStatistics() {
        long totalPayments = paymentRepository.count();
        long successfulPayments = paymentRepository.countByStatus(Payment.PaymentStatus.SUCCESS);
        long failedPayments = paymentRepository.countByStatus(Payment.PaymentStatus.FAILED);
        long pendingPayments = paymentRepository.countByStatus(Payment.PaymentStatus.PENDING);
        long refundedPayments = paymentRepository.countByStatus(Payment.PaymentStatus.REFUNDED);
        
        BigDecimal totalAmount = paymentRepository.getTotalSuccessfulPayments();
        BigDecimal successfulAmount = totalAmount; // Same as total for successful payments
        
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (successfulAmount == null) successfulAmount = BigDecimal.ZERO;
        
        // Get payment method statistics
        List<PaymentMethodStats> methodStats = List.of(
            new PaymentMethodStats(Payment.PaymentMethod.CARD, 
                paymentRepository.countByPaymentMethod(Payment.PaymentMethod.CARD), 
                paymentRepository.getTotalAmountByPaymentMethod(Payment.PaymentMethod.CARD)),
            new PaymentMethodStats(Payment.PaymentMethod.UPI, 
                paymentRepository.countByPaymentMethod(Payment.PaymentMethod.UPI), 
                paymentRepository.getTotalAmountByPaymentMethod(Payment.PaymentMethod.UPI)),
            new PaymentMethodStats(Payment.PaymentMethod.NET_BANKING, 
                paymentRepository.countByPaymentMethod(Payment.PaymentMethod.NET_BANKING), 
                paymentRepository.getTotalAmountByPaymentMethod(Payment.PaymentMethod.NET_BANKING)),
            new PaymentMethodStats(Payment.PaymentMethod.WALLET, 
                paymentRepository.countByPaymentMethod(Payment.PaymentMethod.WALLET), 
                paymentRepository.getTotalAmountByPaymentMethod(Payment.PaymentMethod.WALLET))
        );
        
        return new PaymentStatistics(totalPayments, successfulPayments, failedPayments,
            pendingPayments, refundedPayments, totalAmount, successfulAmount, methodStats);
    }
    
    @Override
    public String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    @Override
    public PaymentResponse processCardPayment(PaymentRequest paymentRequest) {
        return processPayment(paymentRequest);
    }
    
    @Override
    public PaymentResponse processUpiPayment(PaymentRequest paymentRequest) {
        return processPayment(paymentRequest);
    }
    
    @Override
    public PaymentResponse processNetBankingPayment(PaymentRequest paymentRequest) {
        return processPayment(paymentRequest);
    }
    
    // Private helper methods for different payment methods
    private Payment processCardPaymentInternal(Payment payment, PaymentRequest request) {
        // Simulate card payment processing
        try {
            // Validate card details
            if (request.getCardNumber() == null || request.getCardNumber().length() < 16) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setPaymentGatewayResponse("Invalid card number");
            } else if (request.getExpiryDate() == null) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setPaymentGatewayResponse("Invalid expiry date");
            } else if (request.getCvv() == null || request.getCvv().length() != 3) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setPaymentGatewayResponse("Invalid CVV");
            } else {
                // Simulate successful payment
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setPaymentGatewayResponse("Card payment successful");
            }
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setPaymentGatewayResponse("Card payment failed: " + e.getMessage());
        }
        
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    private Payment processUpiPaymentInternal(Payment payment, PaymentRequest request) {
        // Simulate UPI payment processing
        try {
            if (request.getUpiId() == null || !request.getUpiId().contains("@")) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setPaymentGatewayResponse("Invalid UPI ID");
            } else {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setPaymentGatewayResponse("UPI payment successful");
            }
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setPaymentGatewayResponse("UPI payment failed: " + e.getMessage());
        }
        
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    private Payment processNetBankingPaymentInternal(Payment payment, PaymentRequest request) {
        // Simulate net banking payment processing
        try {
            if (request.getBankCode() == null || request.getBankCode().isEmpty()) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setPaymentGatewayResponse("Invalid bank code");
            } else {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setPaymentGatewayResponse("Net banking payment successful");
            }
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setPaymentGatewayResponse("Net banking payment failed: " + e.getMessage());
        }
        
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    private Payment processWalletPaymentInternal(Payment payment, PaymentRequest request) {
        // Simulate wallet payment processing
        try {
            if (request.getWalletId() == null || request.getWalletId().isEmpty()) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setPaymentGatewayResponse("Invalid wallet ID");
            } else {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setPaymentGatewayResponse("Wallet payment successful");
            }
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setPaymentGatewayResponse("Wallet payment failed: " + e.getMessage());
        }
        
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    // Helper method to convert Payment entity to PaymentResponse DTO
    private PaymentResponse convertToResponse(Payment payment) {
        if (payment == null) return null;
        
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setTransactionId(payment.getTransactionId());
        response.setBookingId(payment.getBooking().getId());
        response.setUserId(payment.getBooking().getUser().getId());
        response.setEventId(payment.getBooking().getEvent().getId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setPaymentGatewayResponse(payment.getPaymentGatewayResponse());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        
        return response;
    }