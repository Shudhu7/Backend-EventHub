package com.eventhub.repository;

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

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Basic queries
    @Query("SELECT e FROM Event e WHERE e.isActive = true")
    List<Event> findActiveEvents();
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true")
    Page<Event> findActiveEvents(Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.category = :category")
    List<Event> findActiveEventsByCategory(@Param("category") Event.Category category);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.category = :category")
    Page<Event> findActiveEventsByCategory(@Param("category") Event.Category category, Pageable pageable);
    
    // Search functionality
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Event> searchActiveEvents(@Param("keyword") String keyword);
    
    // Filter events with multiple criteria
    @Query("SELECT e FROM Event e WHERE e.isActive = true " +
           "AND (:category IS NULL OR e.category = :category) " +
           "AND (:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:startDate IS NULL OR e.date >= :startDate) " +
           "AND (:endDate IS NULL OR e.date <= :endDate) " +
           "AND (:minPrice IS NULL OR e.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR e.price <= :maxPrice)")
    Page<Event> filterEvents(@Param("category") Event.Category category,
                            @Param("location") String location,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate,
                            @Param("minPrice") BigDecimal minPrice,
                            @Param("maxPrice") BigDecimal maxPrice,
                            Pageable pageable);
    
    // Date-based queries
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.date = :date")
    List<Event> findActiveEventsByDate(@Param("date") LocalDate date);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.date BETWEEN :startDate AND :endDate")
    List<Event> findActiveEventsByDateRange(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    // Location-based queries
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Event> findActiveEventsByLocation(@Param("location") String location);
    
    // Price-based queries
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.price BETWEEN :minPrice AND :maxPrice")
    List<Event> findActiveEventsByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                           @Param("maxPrice") BigDecimal maxPrice);
    
    // Temporal queries
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.date > CURRENT_DATE")
    List<Event> findUpcomingEvents();
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.date < CURRENT_DATE")
    List<Event> findPastEvents();
    
    // Availability queries
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.availableSeats > 0")
    List<Event> findAvailableEvents();
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.availableSeats > 0 AND e.date > CURRENT_DATE")
    List<Event> findAvailableUpcomingEvents();
    
    // Count queries for statistics
    @Query("SELECT COUNT(e) FROM Event e WHERE e.isActive = true")
    Long countActiveEvents();
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.isActive = true AND e.date > CURRENT_DATE")
    Long countUpcomingEvents();
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.isActive = true AND e.date < CURRENT_DATE")
    Long countPastEvents();
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.isActive = true AND e.availableSeats > 0")
    Long countAvailableEvents();
    
    // Category statistics
    @Query("SELECT e.category, COUNT(e) FROM Event e WHERE e.isActive = true GROUP BY e.category")
    List<Object[]> getEventCategoryStatistics();
    
    // Location statistics
    @Query("SELECT e.location, COUNT(e) FROM Event e WHERE e.isActive = true GROUP BY e.location ORDER BY COUNT(e) DESC")
    List<Object[]> getEventLocationStatistics();
    
    // Price statistics
    @Query("SELECT AVG(e.price) FROM Event e WHERE e.isActive = true")
    BigDecimal getAverageEventPrice();
    
    @Query("SELECT MIN(e.price) FROM Event e WHERE e.isActive = true")
    BigDecimal getMinimumEventPrice();
    
    @Query("SELECT MAX(e.price) FROM Event e WHERE e.isActive = true")
    BigDecimal getMaximumEventPrice();
    
    // Seat statistics
    @Query("SELECT SUM(e.totalSeats) FROM Event e WHERE e.isActive = true")
    Long getTotalSeatsAvailable();
    
    @Query("SELECT SUM(e.availableSeats) FROM Event e WHERE e.isActive = true")
    Long getTotalAvailableSeats();
    
    // Popular events (by bookings)
    @Query("SELECT e FROM Event e JOIN e.bookings b WHERE e.isActive = true " +
           "GROUP BY e ORDER BY COUNT(b) DESC")
    List<Event> findPopularEvents(Pageable pageable);
    
    // Events by organizer (if you have organizer field)
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.createdAt BETWEEN :startDate AND :endDate")
    List<Event> findEventsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
}