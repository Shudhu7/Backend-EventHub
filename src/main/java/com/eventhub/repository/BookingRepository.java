package com.eventhub.repository;

import com.eventhub.model.entity.Booking;
import com.eventhub.model.entity.User;
import com.eventhub.model.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByTicketId(String ticketId);
    
    Boolean existsByTicketId(String ticketId);
    
    List<Booking> findByUser(User user);
    
    Page<Booking> findByUser(User user, Pageable pageable);
    
    List<Booking> findByEvent(Event event);
    
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    // Added missing methods for service implementations
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    
    Page<Booking> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<Booking> findByEventOrderByCreatedAtDesc(Event event);
    
    List<Booking> findAllByOrderByCreatedAtDesc();
    
    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<Booking> findByStatusOrderByCreatedAtDesc(Booking.BookingStatus status);
    
    List<Booking> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Booking b WHERE b.event.id = :eventId ORDER BY b.createdAt DESC")
    List<Booking> findByEventIdOrderByCreatedAtDesc(@Param("eventId") Long eventId);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
    List<Booking> findByUserIdAndStatus(@Param("userId") Long userId, 
                                       @Param("status") Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.event.id = :eventId AND b.status = :status")
    List<Booking> findByEventIdAndStatus(@Param("eventId") Long eventId, 
                                        @Param("status") Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.event.id = :eventId")
    Optional<Booking> findByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);
    
    @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Booking> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    // Count methods for statistics
    Long countByStatus(Booking.BookingStatus status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Long getConfirmedBookingsCount();
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'PENDING'")
    Long getPendingBookingsCount();
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CANCELLED'")
    Long getCancelledBookingsCount();
    
    // Revenue calculations - using correct field names
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT SUM(b.serviceFee) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal getTotalServiceFees();
    
    // Calculate subtotal by subtracting service fee from total amount
    @Query("SELECT SUM(b.totalAmount - b.serviceFee) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal getTotalSubtotalAmount();
    
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = 'CONFIRMED' AND " +
           "b.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT SUM(b.numberOfTickets) FROM Booking b WHERE b.event.id = :eventId AND b.status = 'CONFIRMED'")
    Integer getTotalBookedTicketsForEvent(@Param("eventId") Long eventId);
    
    // Statistics queries
    @Query("SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status")
    List<Object[]> getBookingStatusStatistics();
    
    @Query("SELECT DATE(b.createdAt), COUNT(b) FROM Booking b WHERE b.createdAt >= :startDate GROUP BY DATE(b.createdAt) ORDER BY DATE(b.createdAt)")
    List<Object[]> getDailyBookingStats(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT e.category, COUNT(b) FROM Booking b JOIN b.event e WHERE b.status = 'CONFIRMED' GROUP BY e.category ORDER BY COUNT(b) DESC")
    List<Object[]> getBookingsByEventCategory();
}