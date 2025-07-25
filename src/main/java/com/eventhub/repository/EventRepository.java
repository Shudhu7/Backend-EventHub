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
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByIsActiveTrue();
    
    Page<Event> findByIsActiveTrue(Pageable pageable);
    
    List<Event> findByCategory(Event.Category category);
    
    Page<Event> findByCategoryAndIsActiveTrue(Event.Category category, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.date = :date")
    List<Event> findActiveEventsByDate(@Param("date") LocalDate date);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.date BETWEEN :startDate AND :endDate")
    List<Event> findActiveEventsByDateRange(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.location LIKE %:location%")
    List<Event> findActiveEventsByLocation(@Param("location") String location);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.price BETWEEN :minPrice AND :maxPrice")
    List<Event> findActiveEventsByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                            @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND " +
           "(e.title LIKE %:keyword% OR e.description LIKE %:keyword% OR e.location LIKE %:keyword%)")
    List<Event> searchActiveEvents(@Param("keyword") String keyword);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:location IS NULL OR e.location LIKE %:location%) AND " +
           "(:startDate IS NULL OR e.date >= :startDate) AND " +
           "(:endDate IS NULL OR e.date <= :endDate) AND " +
           "(:minPrice IS NULL OR e.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR e.price <= :maxPrice)")
    Page<Event> findEventsWithFilters(@Param("category") Event.Category category,
                                     @Param("location") String location,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.availableSeats > 0")
    List<Event> findAvailableEvents();
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.date > CURRENT_DATE")
    List<Event> findUpcomingEvents();
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.date < CURRENT_DATE")
    List<Event> findPastEvents();
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.isActive = true")
    Long countActiveEvents();
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.isActive = true AND e.date > CURRENT_DATE")
    Long countUpcomingEvents();
}