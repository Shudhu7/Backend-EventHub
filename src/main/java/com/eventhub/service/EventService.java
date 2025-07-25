package com.eventhub.service;

import com.eventhub.dto.EventDTO;
import com.eventhub.model.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EventService {
    
    /**
     * Create new event (Admin only)
     */
    EventDTO createEvent(EventDTO eventDTO);
    
    /**
     * Update existing event (Admin only)
     */
    EventDTO updateEvent(Long id, EventDTO eventDTO);
    
    /**
     * Get event by ID
     */
    EventDTO getEventById(Long id);
    
    /**
     * Get all active events
     */
    List<EventDTO> getAllActiveEvents();
    
    /**
     * Get active events with pagination
     */
    Page<EventDTO> getAllActiveEvents(Pageable pageable);
    
    /**
     * Get events by category
     */
    List<EventDTO> getEventsByCategory(Event.Category category);
    
    /**
     * Get events by category with pagination
     */
    Page<EventDTO> getEventsByCategory(Event.Category category, Pageable pageable);
    
    /**
     * Search events by keyword
     */
    List<EventDTO> searchEvents(String keyword);
    
    /**
     * Filter events with multiple criteria
     */
    Page<EventDTO> filterEvents(Event.Category category, String location, 
                               LocalDate startDate, LocalDate endDate,
                               BigDecimal minPrice, BigDecimal maxPrice,
                               Pageable pageable);
    
    /**
     * Get events by date
     */
    List<EventDTO> getEventsByDate(LocalDate date);
    
    /**
     * Get events by date range
     */
    List<EventDTO> getEventsByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get events by location
     */
    List<EventDTO> getEventsByLocation(String location);
    
    /**
     * Get events by price range
     */
    List<EventDTO> getEventsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Get upcoming events
     */
    List<EventDTO> getUpcomingEvents();
    
    /**
     * Get past events
     */
    List<EventDTO> getPastEvents();
    
    /**
     * Get available events (with available seats)
     */
    List<EventDTO> getAvailableEvents();
    
    /**
     * Soft delete event (Admin only)
     */
    void deleteEvent(Long id);
    
    /**
     * Update available seats after booking
     */
    void updateAvailableSeats(Long eventId, Integer seatsBooked);
    
    /**
     * Get event statistics (Admin only)
     */
    EventStatistics getEventStatistics();
    
    /**
     * Convert entity to DTO
     */
    EventDTO convertToDTO(Event event);
    
    /**
     * Convert DTO to entity
     */
    Event convertToEntity(EventDTO eventDTO);
    
    /**
     * Inner class for event statistics
     */
    class EventStatistics {
        private long totalEvents;
        private long activeEvents;
        private long upcomingEvents;
        private long pastEvents;
        private long availableEvents;
        
        // Constructors
        public EventStatistics() {}
        
        public EventStatistics(long totalEvents, long activeEvents, long upcomingEvents, 
                              long pastEvents, long availableEvents) {
            this.totalEvents = totalEvents;
            this.activeEvents = activeEvents;
            this.upcomingEvents = upcomingEvents;
            this.pastEvents = pastEvents;
            this.availableEvents = availableEvents;
        }
        
        // Getters and Setters
        public long getTotalEvents() { return totalEvents; }
        public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }
        
        public long getActiveEvents() { return activeEvents; }
        public void setActiveEvents(long activeEvents) { this.activeEvents = activeEvents; }
        
        public long getUpcomingEvents() { return upcomingEvents; }
        public void setUpcomingEvents(long upcomingEvents) { this.upcomingEvents = upcomingEvents; }
        
        public long getPastEvents() { return pastEvents; }
        public void setPastEvents(long pastEvents) { this.pastEvents = pastEvents; }
        
        public long getAvailableEvents() { return availableEvents; }
        public void setAvailableEvents(long availableEvents) { this.availableEvents = availableEvents; }
    }
}