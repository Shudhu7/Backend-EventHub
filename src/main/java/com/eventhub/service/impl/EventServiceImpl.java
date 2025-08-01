package com.eventhub.service.impl;

import com.eventhub.dto.EventDTO;
import com.eventhub.model.entity.Event;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.ReviewRepository;
import com.eventhub.service.EventService;
import com.eventhub.service.WebSocketService; // ADD THIS IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap; // ADD THIS IMPORT
import java.util.List;
import java.util.Map; // ADD THIS IMPORT
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    // ADD THIS AUTOWIRED FIELD
    @Autowired
    private WebSocketService webSocketService;
    
    @Override
    public EventDTO createEvent(EventDTO eventDTO) {
        Event event = convertToEntity(eventDTO);
        event.setAvailableSeats(event.getTotalSeats());
        event.setIsActive(true);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        
        Event savedEvent = eventRepository.save(event);
        EventDTO result = convertToDTO(savedEvent);
        
        // ADD THIS REAL-TIME NOTIFICATION CODE
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_EVENT");
            notification.put("event", result);
            notification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendGlobalEventNotification(notification);
        } catch (Exception e) {
            // Log the error but don't fail the event creation
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public EventDTO updateEvent(Long id, EventDTO eventDTO) {
        Event existingEvent = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        // Update allowed fields
        existingEvent.setTitle(eventDTO.getTitle());
        existingEvent.setDescription(eventDTO.getDescription());
        existingEvent.setDate(eventDTO.getDate());
        existingEvent.setTime(eventDTO.getTime());
        existingEvent.setLocation(eventDTO.getLocation());
        existingEvent.setPrice(eventDTO.getPrice());
        existingEvent.setCategory(eventDTO.getCategory());
        existingEvent.setImage(eventDTO.getImage());
        existingEvent.setUpdatedAt(LocalDateTime.now());
        
        // Update total seats and adjust available seats if needed
        if (eventDTO.getTotalSeats() != null) {
            int seatDifference = eventDTO.getTotalSeats() - existingEvent.getTotalSeats();
            existingEvent.setTotalSeats(eventDTO.getTotalSeats());
            existingEvent.setAvailableSeats(existingEvent.getAvailableSeats() + seatDifference);
        }
        
        Event updatedEvent = eventRepository.save(existingEvent);
        EventDTO result = convertToDTO(updatedEvent);
        
        // ADD THIS REAL-TIME UPDATE NOTIFICATION CODE
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "EVENT_UPDATED");
            notification.put("event", result);
            notification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendGlobalEventNotification(notification);
            webSocketService.sendEventUpdate(id.toString(), result);
        } catch (Exception e) {
            // Log the error but don't fail the event update
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public EventDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return convertToDTO(event);
    }
    
    @Override
    public List<EventDTO> getAllActiveEvents() {
        List<Event> events = eventRepository.findActiveEvents();
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<EventDTO> getAllActiveEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findActiveEvents(pageable);
        return events.map(this::convertToDTO);
    }
    
    @Override
    public List<EventDTO> getEventsByCategory(Event.Category category) {
        List<Event> events = eventRepository.findActiveEventsByCategory(category);
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<EventDTO> getEventsByCategory(Event.Category category, Pageable pageable) {
        Page<Event> events = eventRepository.findActiveEventsByCategory(category, pageable);
        return events.map(this::convertToDTO);
    }
    
    @Override
    public List<EventDTO> searchEvents(String keyword) {
        List<Event> events = eventRepository.searchActiveEvents(keyword);
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<EventDTO> filterEvents(Event.Category category, String location, 
                                     LocalDate startDate, LocalDate endDate,
                                     BigDecimal minPrice, BigDecimal maxPrice,
                                     Pageable pageable) {
        Page<Event> events = eventRepository.filterEvents(category, location, 
            startDate, endDate, minPrice, maxPrice, pageable);
        return events.map(this::convertToDTO);
    }
    
    @Override
    public List<EventDTO> getEventsByDate(LocalDate date) {
        List<Event> events = eventRepository.findActiveEventsByDate(date);
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EventDTO> getEventsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Event> events = eventRepository.findActiveEventsByDateRange(startDate, endDate);
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EventDTO> getEventsByLocation(String location) {
        List<Event> events = eventRepository.findActiveEventsByLocation(location);
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EventDTO> getEventsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Event> events = eventRepository.findActiveEventsByPriceRange(minPrice, maxPrice);
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EventDTO> getUpcomingEvents() {
        List<Event> events = eventRepository.findUpcomingEvents();
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EventDTO> getPastEvents() {
        List<Event> events = eventRepository.findPastEvents();
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EventDTO> getAvailableEvents() {
        List<Event> events = eventRepository.findAvailableEvents();
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        // Soft delete
        event.setIsActive(false);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
        
        // ADD THIS REAL-TIME DELETE NOTIFICATION CODE
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "EVENT_DELETED");
            notification.put("eventId", id);
            notification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendGlobalEventNotification(notification);
        } catch (Exception e) {
            // Log the error but don't fail the event deletion
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
    }
    
    @Override
    public void updateAvailableSeats(Long eventId, Integer seatsBooked) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        int newAvailableSeats = event.getAvailableSeats() - seatsBooked;
        if (newAvailableSeats < 0) {
            throw new RuntimeException("Not enough available seats");
        }
        
        event.setAvailableSeats(newAvailableSeats);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
        
        // ADD THIS REAL-TIME SEAT UPDATE CODE
        try {
            Map<String, Object> seatUpdate = new HashMap<>();
            seatUpdate.put("eventId", eventId);
            seatUpdate.put("availableSeats", newAvailableSeats);
            seatUpdate.put("totalSeats", event.getTotalSeats());
            seatUpdate.put("bookedSeats", event.getTotalSeats() - newAvailableSeats);
            seatUpdate.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendSeatUpdate(eventId.toString(), seatUpdate);
            
            // Also send general event update
            Map<String, Object> eventUpdate = new HashMap<>();
            eventUpdate.put("type", "SEAT_UPDATE");
            eventUpdate.put("eventId", eventId);
            eventUpdate.put("availableSeats", newAvailableSeats);
            eventUpdate.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendGlobalEventNotification(eventUpdate);
        } catch (Exception e) {
            // Log the error but don't fail the seat update
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
    }
    
    @Override
    public EventStatistics getEventStatistics() {
        long totalEvents = eventRepository.count();
        long activeEvents = eventRepository.countActiveEvents();
        long upcomingEvents = eventRepository.countUpcomingEvents();
        long pastEvents = eventRepository.countPastEvents();
        long availableEvents = eventRepository.countAvailableEvents();
        
        return new EventStatistics(totalEvents, activeEvents, upcomingEvents, pastEvents, availableEvents);
    }
    
    @Override
    public EventDTO convertToDTO(Event event) {
        if (event == null) return null;
        
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setDate(event.getDate());
        dto.setTime(event.getTime());
        dto.setLocation(event.getLocation());
        dto.setPrice(event.getPrice());
        dto.setTotalSeats(event.getTotalSeats());
        dto.setAvailableSeats(event.getAvailableSeats());
        dto.setCategory(event.getCategory());
        dto.setImage(event.getImage());
        dto.setIsActive(event.getIsActive());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        
        // Calculate average rating if reviews exist
        if (reviewRepository != null) {
            try {
                Double avgRating = reviewRepository.getAverageRatingByEventId(event.getId());
                dto.setAverageRating(avgRating != null ? avgRating : 0.0);
                
                Long reviewCount = reviewRepository.getReviewCountByEventId(event.getId());
                dto.setTotalReviews(reviewCount != null ? reviewCount.intValue() : 0);
            } catch (Exception e) {
                // If review repository methods fail, set defaults
                dto.setAverageRating(0.0);
                dto.setTotalReviews(0);
            }
        } else {
            dto.setAverageRating(0.0);
            dto.setTotalReviews(0);
        }
        
        return dto;
    }
    
    @Override
    public Event convertToEntity(EventDTO eventDTO) {
        if (eventDTO == null) return null;
        
        Event event = new Event();
        event.setId(eventDTO.getId());
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setDate(eventDTO.getDate());
        event.setTime(eventDTO.getTime());
        event.setLocation(eventDTO.getLocation());
        event.setPrice(eventDTO.getPrice());
        event.setTotalSeats(eventDTO.getTotalSeats());
        event.setAvailableSeats(eventDTO.getAvailableSeats());
        event.setCategory(eventDTO.getCategory());
        event.setImage(eventDTO.getImage());
        event.setIsActive(eventDTO.getIsActive() != null ? eventDTO.getIsActive() : true);
        event.setCreatedAt(eventDTO.getCreatedAt());
        event.setUpdatedAt(eventDTO.getUpdatedAt());
        
        return event;
    }
}