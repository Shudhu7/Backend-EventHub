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
import java.util.Optional;
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
        
        // Create payment entity
        Payment payment = new Payment();
        payment.setTransactionId(generateTransactionId());
        payment.setBooking(booking);
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        
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
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByPaymentMethod(Payment.PaymentMethod paymentMethod) {
        List<Payment> payments = paymentRepository.findByPaymentMethod(paymentMethod);
        return payments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments = paymentRepository.findPaymentsCreatedBetween(startDate, endDate);
        return payments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public PaymentResponse processRefund(RefundRequest refundRequest) {
        Payment originalPayment = paymentRepository.findByTransactionId(refundRequest.getTransactionId())
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (originalPayment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Can only refund successful payments");
        }
        
        if (refundRequest.getRefundAmount().compareTo(originalPayment.getAmount()) > 0) {
            throw new RuntimeException("Refund amount cannot exceed original payment amount");
        }
        
        // Create refund payment entry
        Payment refundPayment = new Payment();
        refundPayment.setTransactionId(generateTransactionId());
        refundPayment.setBooking(originalPayment.getBooking());
        refundPayment.setAmount(refundRequest.getRefundAmount().negate()); // Negative amount for refund
        refundPayment.setPaymentMethod(originalPayment.getPaymentMethod());
        refundPayment.setStatus(Payment.PaymentStatus.REFUNDED);
        refundPayment.setPaymentGatewayResponse("Refund processed: " + refundRequest.getReason());
        
        Payment savedRefund = paymentRepository.save(refundPayment);
        
        // Update original payment status
        originalPayment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(originalPayment);
        
        // Update booking status
        Booking booking = originalPayment.getBooking();
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        return convertToResponse(savedRefund);
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
        Payment updatedPayment = paymentRepository.save(payment);
        
        return convertToResponse(updatedPayment);
    }
    
    @Override
    public PaymentStatistics getPaymentStatistics() {
        long totalPayments = paymentRepository.count();
        long successfulPayments = paymentRepository.getSuccessfulPaymentsCount();
        long failedPayments = paymentRepository.getFailedPaymentsCount();
        long pendingPayments = paymentRepository.getPendingPaymentsCount();
        long refundedPayments = paymentRepository.getRefundedPaymentsCount();
        
        BigDecimal totalAmount = paymentRepository.getTotalSuccessfulPayments();
        BigDecimal successfulAmount = totalAmount; // Same as total for successful payments
        
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (successfulAmount == null) successfulAmount = BigDecimal.ZERO;
        
        // Get payment method statistics
        List<Object[]> methodStatsData = paymentRepository.getPaymentMethodStatistics();
        List<PaymentMethodStats> methodStats = methodStatsData.stream()
            .map(data -> new PaymentMethodStats((Payment.PaymentMethod) data[0], (Long) data[1], BigDecimal.ZERO))
            .collect(Collectors.toList());
        
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
            } else {
                // Simulate successful payment
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setPaymentGatewayResponse("Card payment successful");
            }
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setPaymentGatewayResponse("Card payment failed: " + e.getMessage());
        }
        
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
        
        return paymentRepository.save(payment);
    }
    
    private Payment processNetBankingPaymentInternal(Payment payment, PaymentRequest request) {
        // Simulate net banking payment processing
        try {
            if (request.getBankCode() == null) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setPaymentGatewayResponse("Bank code required");
            } else {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setPaymentGatewayResponse("Net banking payment successful");
            }
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setPaymentGatewayResponse("Net banking payment failed: " + e.getMessage());
        }
        
        return paymentRepository.save(payment);
    }
    
    private Payment processWalletPaymentInternal(Payment payment, PaymentRequest request) {
        // Simulate wallet payment processing
        try {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaymentGatewayResponse("Wallet payment successful");
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setPaymentGatewayResponse("Wallet payment failed: " + e.getMessage());
        }
        
        return paymentRepository.save(payment);
    }
    
    @Override
    public PaymentResponse convertToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(payment.getTransactionId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setCreatedAt(payment.getCreatedAt());
        response.setBookingId(payment.getBooking().getId());
        response.setTicketId(payment.getBooking().getTicketId());
        
        // Set message based on status
        switch (payment.getStatus()) {
            case SUCCESS:
                response.setMessage("Payment completed successfully");
                break;
            case FAILED:
                response.setMessage("Payment failed");
                break;
            case PENDING:
                response.setMessage("Payment is being processed");
                break;
            case REFUNDED:
                response.setMessage("Payment has been refunded");
                break;
            default:
                response.setMessage("Payment status unknown");
        }
        
        return response;
    }
}