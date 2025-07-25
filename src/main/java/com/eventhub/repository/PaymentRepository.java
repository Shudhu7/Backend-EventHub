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
    
    @Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    BigDecimal getTotalSuccessfulPayments();
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' AND " +
           "p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getSuccessfulPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS'")
    Long getSuccessfulPaymentsCount();
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'FAILED'")
    Long getFailedPaymentsCount();
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PENDING'")
    Long getPendingPaymentsCount();
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'REFUNDED'")
    Long getRefundedPaymentsCount();
    
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS' GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodStatistics();
}