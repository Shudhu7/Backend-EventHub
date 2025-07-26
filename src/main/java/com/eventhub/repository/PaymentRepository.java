package com.eventhub.repository;

import com.eventhub.model.entity.Payment;
import com.eventhub.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    Optional<Payment> findByBooking(Booking booking);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    // Added missing methods that were causing the constructor exception
    List<Payment> findAllByOrderByCreatedAtDesc();
    
    List<Payment> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status);
    
    List<Payment> findByPaymentMethodOrderByCreatedAtDesc(Payment.PaymentMethod paymentMethod);
    
    List<Payment> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Payment> findPaymentsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    BigDecimal getTotalSuccessfulPayments();
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' AND " +
           "p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getSuccessfulPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    // Count methods for statistics
    Long countByStatus(Payment.PaymentStatus status);
    
    Long countByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS'")
    Long getSuccessfulPaymentsCount();
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'FAILED'")
    Long getFailedPaymentsCount();
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PENDING'")
    Long getPendingPaymentsCount();
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'REFUNDED'")
    Long getRefundedPaymentsCount();
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentMethod = :method AND p.status = 'SUCCESS'")
    BigDecimal getTotalAmountByPaymentMethod(@Param("method") Payment.PaymentMethod method);
    
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS' GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodStatistics();
}