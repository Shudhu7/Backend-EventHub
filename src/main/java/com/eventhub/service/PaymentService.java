package com.eventhub.service;

import com.eventhub.dto.PaymentRequest;
import com.eventhub.dto.PaymentResponse;
import com.eventhub.dto.RefundRequest;
import com.eventhub.model.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
    
    /**
     * Process payment for booking
     */
    PaymentResponse processPayment(PaymentRequest paymentRequest);
    
    /**
     * Get payment by transaction ID
     */
    PaymentResponse getPaymentByTransactionId(String transactionId);
    
    /**
     * Get payment by booking ID
     */
    PaymentResponse getPaymentByBookingId(Long bookingId);
    
    /**
     * Get payments by user ID
     */
    List<PaymentResponse> getPaymentsByUserId(Long userId);
    
    /**
     * Get all payments (Admin only)
     */
    List<PaymentResponse> getAllPayments();
    
    /**
     * Get payments by status
     */
    List<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status);
    
    /**
     * Get payments by payment method
     */
    List<PaymentResponse> getPaymentsByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    /**
     * Get payments by date range
     */
    List<PaymentResponse> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Process refund
     */
    PaymentResponse processRefund(RefundRequest refundRequest);
    
    /**
     * Verify payment with payment gateway
     */
    boolean verifyPayment(String transactionId);
    
    /**
     * Update payment status
     */
    PaymentResponse updatePaymentStatus(String transactionId, Payment.PaymentStatus status);
    
    /**
     * Get payment statistics (Admin only)
     */
    PaymentStatistics getPaymentStatistics();
    
    /**
     * Generate transaction ID
     */
    String generateTransactionId();
    
    /**
     * Process card payment
     */
    PaymentResponse processCardPayment(PaymentRequest paymentRequest);
    
    /**
     * Process UPI payment
     */
    PaymentResponse processUpiPayment(PaymentRequest paymentRequest);
    
    /**
     * Process net banking payment
     */
    PaymentResponse processNetBankingPayment(PaymentRequest paymentRequest);
    
    /**
     * Inner class for payment statistics
     */
    class PaymentStatistics {
        private long totalPayments;
        private long successfulPayments;
        private long failedPayments;
        private long pendingPayments;
        private long refundedPayments;
        private BigDecimal totalAmount;
        private BigDecimal successfulAmount;
        private List<PaymentMethodStats> methodStats;
        
        public PaymentStatistics() {}
        
        public PaymentStatistics(long totalPayments, long successfulPayments, long failedPayments,
                               long pendingPayments, long refundedPayments, BigDecimal totalAmount,
                               BigDecimal successfulAmount, List<PaymentMethodStats> methodStats) {
            this.totalPayments = totalPayments;
            this.successfulPayments = successfulPayments;
            this.failedPayments = failedPayments;
            this.pendingPayments = pendingPayments;
            this.refundedPayments = refundedPayments;
            this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
            this.successfulAmount = successfulAmount != null ? successfulAmount : BigDecimal.ZERO;
            this.methodStats = methodStats;
        }
        
        // Getters and Setters
        public long getTotalPayments() { return totalPayments; }
        public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }
        
        public long getSuccessfulPayments() { return successfulPayments; }
        public void setSuccessfulPayments(long successfulPayments) { this.successfulPayments = successfulPayments; }
        
        public long getFailedPayments() { return failedPayments; }
        public void setFailedPayments(long failedPayments) { this.failedPayments = failedPayments; }
        
        public long getPendingPayments() { return pendingPayments; }
        public void setPendingPayments(long pendingPayments) { this.pendingPayments = pendingPayments; }
        
        public long getRefundedPayments() { return refundedPayments; }
        public void setRefundedPayments(long refundedPayments) { this.refundedPayments = refundedPayments; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public BigDecimal getSuccessfulAmount() { return successfulAmount; }
        public void setSuccessfulAmount(BigDecimal successfulAmount) { this.successfulAmount = successfulAmount; }
        
        public List<PaymentMethodStats> getMethodStats() { return methodStats; }
        public void setMethodStats(List<PaymentMethodStats> methodStats) { this.methodStats = methodStats; }
    }
    
    /**
     * Inner class for payment method statistics
     */
    class PaymentMethodStats {
        private Payment.PaymentMethod method;
        private long count;
        private BigDecimal totalAmount;
        
        public PaymentMethodStats() {}
        
        public PaymentMethodStats(Payment.PaymentMethod method, long count, BigDecimal totalAmount) {
            this.method = method;
            this.count = count;
            this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        }
        
        // Getters and Setters
        public Payment.PaymentMethod getMethod() { return method; }
        public void setMethod(Payment.PaymentMethod method) { this.method = method; }
        
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}