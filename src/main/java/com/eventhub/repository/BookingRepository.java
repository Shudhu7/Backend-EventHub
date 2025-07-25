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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByTicketId(String ticketId);
    
    List<Booking> findByUser(User user);
    
    Page<Booking> findByUser(User user, Pageable pageable);
    
    List<Booking> findByEvent(Event event);
    
    List<Booking> findByStatus(Booking.BookingStatus status);
    
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
    
    @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Booking> findBookingsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = 'CONFIRMED' AND " +
           "b.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Long getConfirmedBookingsCount();
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CANCELLED'")
    Long getCancelledBookingsCount();
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'PENDING'")
    Long getPendingBookingsCount();
    
    @Query("SELECT SUM(b.numberOfTickets) FROM Booking b WHERE b.event.id = :eventId AND b.status = 'CONFIRMED'")
    Integer getConfirmedTicketsByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT b FROM Booking b JOIN FETCH b.event JOIN FETCH b.user WHERE b.user.id = :userId")
    List<Booking> findByUserIdWithEventAndUser(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Booking b JOIN FETCH b.event e JOIN FETCH b.user u WHERE b.id = :bookingId")
    Optional<Booking> findByIdWithEventAndUser(@Param("bookingId") Long bookingId);
    
    @Query("SELECT b FROM Booking b WHERE b.event.date BETWEEN :startDate AND :endDate AND b.status = 'CONFIRMED'")
    List<Booking> findConfirmedBookingsByEventDateRange(@Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId")
    Long countBookingsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.event.id = :eventId")
    Long countBookingsByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT b FROM Booking b WHERE b.status = :status ORDER BY b.createdAt DESC")
    Page<Booking> findByStatusOrderByCreatedAtDesc(@Param("status") Booking.BookingStatus status, Pageable pageable);
    
    @Query("SELECT DISTINCT b.event FROM Booking b WHERE b.user.id = :userId AND b.status = 'CONFIRMED'")
    List<Event> findEventsByUserIdAndConfirmedStatus(@Param("userId") Long userId);
    
    @Query("SELECT SUM(b.serviceFee) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal getTotalServiceFees();
    
    @Query("SELECT SUM(b.serviceFee) FROM Booking b WHERE b.status = 'CONFIRMED' AND " +
           "b.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getServiceFeesByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.event.id = :eventId")
    Optional<Booking> findByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.event.date = CURRENT_DATE AND b.status = 'CONFIRMED'")
    Long getTodaysBookingsCount();
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE MONTH(b.createdAt) = MONTH(CURRENT_DATE) AND YEAR(b.createdAt) = YEAR(CURRENT_DATE) AND b.status = 'CONFIRMED'")
    Long getThisMonthBookingsCount();
    
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt >= :fromDate")
    List<Booking> findRecentBookingsByStatus(@Param("status") Booking.BookingStatus status, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT AVG(b.totalAmount) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal getAverageBookingAmount();
    
    @Query("SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status")
    List<Object[]> getBookingStatusStatistics();
    
    @Query("SELECT DATE(b.createdAt), COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED' AND b.createdAt >= :fromDate GROUP BY DATE(b.createdAt) ORDER BY DATE(b.createdAt)")
    List<Object[]> getDailyBookingStats(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT e.category, COUNT(b) FROM Booking b JOIN b.event e WHERE b.status = 'CONFIRMED' GROUP BY e.category")
    List<Object[]> getBookingsByCategory();
    
    boolean existsByUserAndEvent(User user, Event event);
    
    boolean existsByTicketId(String ticketId);
}