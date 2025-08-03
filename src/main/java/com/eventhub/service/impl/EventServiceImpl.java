// src/main/java/com/eventhub/service/impl/EventServiceImpl.java
package com.eventhub.service.impl;

import com.eventhub.dto.EventDTO;
import com.eventhub.model.entity.Event;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.ReviewRepository;
import com.eventhub.service.EventService;
import com.eventhub.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Override
    public EventDTO createEvent(EventDTO eventDTO) {
        System.out.println("🎯 EventServiceImpl: Creating event - " + eventDTO.getTitle());
        
        Event event = convertToEntity(eventDTO);
        event.setAvailableSeats(event.getTotalSeats());
        event.setIsActive(true);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        
        Event savedEvent = eventRepository.save(event);
        EventDTO result = convertToDTO(savedEvent);
        
        System.out.println("✅ EventServiceImpl: Event saved to database with ID: " + result.getId());
        
        // ⚡ ENHANCED REAL-TIME NOTIFICATION - Send after transaction commits
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    System.out.println("📡 EventServiceImpl: Transaction committed, sending notifications...");
                    sendEventCreationNotifications(result);
                }
            });
        } else {
            // Fallback if transaction synchronization is not active
            System.out.println("⚠️ EventServiceImpl: No transaction synchronization, sending notifications immediately");
            sendEventCreationNotifications(result);
        }
        
        return result;
    }
    
    /**
     * Send comprehensive real-time notifications for event creation
     */
    private void sendEventCreationNotifications(EventDTO eventDTO) {
        try {
            System.out.println("🚀 Sending real-time notifications for new event: " + eventDTO.getTitle());
            
            // 1. Send to global event subscribers (all users)
            Map<String, Object> globalNotification = new HashMap<>();
            globalNotification.put("type", "NEW_EVENT");
            globalNotification.put("event", eventDTO);
            globalNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendGlobalEventNotification(globalNotification);
            System.out.println("✅ Sent global event notification");
            
            // 2. Send to admin dashboard subscribers (admins only)
            Map<String, Object> adminDashboardNotification = new HashMap<>();
            adminDashboardNotification.put("type", "NEW_EVENT_CREATED");
            adminDashboardNotification.put("eventId", eventDTO.getId());
            adminDashboardNotification.put("eventTitle", eventDTO.getTitle());
            adminDashboardNotification.put("eventCategory", eventDTO.getCategory());
            adminDashboardNotification.put("eventDate", eventDTO.getDate());
            adminDashboardNotification.put("eventPrice", eventDTO.getPrice());
            adminDashboardNotification.put("totalSeats", eventDTO.getTotalSeats());
            adminDashboardNotification.put("availableSeats", eventDTO.getAvailableSeats());
            adminDashboardNotification.put("message", "New event '" + eventDTO.getTitle() + "' has been created successfully");
            adminDashboardNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendDashboardUpdate(adminDashboardNotification);
            System.out.println("✅ Sent admin dashboard notification");
            
            // 3. Send event-specific update
            Map<String, Object> specificEventNotification = new HashMap<>();
            specificEventNotification.put("type", "EVENT_CREATED");
            specificEventNotification.put("event", eventDTO);
            specificEventNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendEventUpdate(eventDTO.getId().toString(), specificEventNotification);
            System.out.println("✅ Sent event-specific notification");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send WebSocket notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public EventDTO updateEvent(Long id, EventDTO eventDTO) {
        System.out.println("🔄 EventServiceImpl: Updating event - " + id);
        
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
        
        System.out.println("✅ EventServiceImpl: Event updated in database");
        
        // ⚡ ENHANCED REAL-TIME UPDATE NOTIFICATION
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    System.out.println("📡 EventServiceImpl: Update transaction committed, sending notifications...");
                    sendEventUpdateNotifications(result);
                }
            });
        } else {
            sendEventUpdateNotifications(result);
        }
        
        return result;
    }
    
    /**
     * Send comprehensive real-time notifications for event updates
     */
    private void sendEventUpdateNotifications(EventDTO eventDTO) {
        try {
            System.out.println("🔄 Sending real-time notifications for updated event: " + eventDTO.getTitle());
            
            // 1. Global event update
            Map<String, Object> globalNotification = new HashMap<>();
            globalNotification.put("type", "EVENT_UPDATED");
            globalNotification.put("event", eventDTO);
            globalNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendGlobalEventNotification(globalNotification);
            
            // 2. Admin dashboard update
            Map<String, Object> adminDashboardNotification = new HashMap<>();
            adminDashboardNotification.put("type", "EVENT_UPDATED");
            adminDashboardNotification.put("eventId", eventDTO.getId());
            adminDashboardNotification.put("eventTitle", eventDTO.getTitle());
            adminDashboardNotification.put("message", "Event '" + eventDTO.getTitle() + "' has been updated");
            adminDashboardNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendDashboardUpdate(adminDashboardNotification);
            
            // 3. Event-specific update
            webSocketService.sendEventUpdate(eventDTO.getId().toString(), eventDTO);
            
            System.out.println("✅ Sent all event update notifications");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send WebSocket update notifications: " + e.getMessage());
            e.printStackTrace();
        }
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
    public Page<EventDTO> getAllEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);
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
    public Page<EventDTO> searchEvents(String keyword, Pageable pageable) {
        Page<Event> events = eventRepository.searchActiveEvents(keyword, pageable);
        return events.map(this::convertToDTO);
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
    public List<EventDTO> getUpcomingEvents(int limit) {
        // Use the existing findUpcomingEvents() method and limit in service layer
        List<Event> allUpcoming = eventRepository.findUpcomingEvents();
        
        // Limit the results in Java to avoid MySQL syntax issues
        List<Event> limitedEvents = allUpcoming.stream()
            .limit(limit)
            .collect(Collectors.toList());
            
        return limitedEvents.stream()
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
    public List<String> getAllCategories() {
        return Arrays.stream(Event.Category.values())
            .map(Enum::name)
            .collect(Collectors.toList());
    }
    
    @Override
    public EventDTO toggleEventStatus(Long id) {
        System.out.println("🔄 EventServiceImpl: Toggling status for event - " + id);
        
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        boolean oldStatus = event.getIsActive();
        event.setIsActive(!oldStatus);
        event.setUpdatedAt(LocalDateTime.now());
        
        Event updatedEvent = eventRepository.save(event);
        EventDTO result = convertToDTO(updatedEvent);
        
        System.out.println("✅ EventServiceImpl: Event status changed from " + oldStatus + " to " + result.getIsActive());
        
        // Send real-time notification for status change
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    System.out.println("📡 EventServiceImpl: Status change transaction committed, sending notifications...");
                    sendEventStatusChangeNotifications(result);
                }
            });
        } else {
            sendEventStatusChangeNotifications(result);
        }
        
        return result;
    }
    
    /**
     * Send comprehensive real-time notifications for event status changes
     */
    private void sendEventStatusChangeNotifications(EventDTO eventDTO) {
        try {
            System.out.println("🔄 Sending real-time notifications for status change: " + eventDTO.getTitle() + 
                              " (Active: " + eventDTO.getIsActive() + ")");
            
            // 1. Global status change notification
            Map<String, Object> globalNotification = new HashMap<>();
            globalNotification.put("type", "EVENT_STATUS_CHANGED");
            globalNotification.put("event", eventDTO);
            globalNotification.put("isActive", eventDTO.getIsActive());
            globalNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendGlobalEventNotification(globalNotification);
            
            // 2. Admin dashboard notification
            Map<String, Object> adminNotification = new HashMap<>();
            adminNotification.put("type", "EVENT_STATUS_CHANGED");
            adminNotification.put("eventId", eventDTO.getId());
            adminNotification.put("eventTitle", eventDTO.getTitle());
            adminNotification.put("isActive", eventDTO.getIsActive());
            adminNotification.put("message", "Event '" + eventDTO.getTitle() + "' is now " + 
                                             (eventDTO.getIsActive() ? "active" : "inactive"));
            adminNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendDashboardUpdate(adminNotification);
            
            // 3. Event-specific notification
            webSocketService.sendEventUpdate(eventDTO.getId().toString(), globalNotification);
            
            System.out.println("✅ Sent all status change notifications");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send status change notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void deleteEvent(Long id) {
        System.out.println("🗑️ EventServiceImpl: Deleting event - " + id);
        
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        String eventTitle = event.getTitle(); // Store title before deletion
        
        // Soft delete
        event.setIsActive(false);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
        
        System.out.println("✅ EventServiceImpl: Event soft deleted from database");
        
        // Send real-time delete notification
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    System.out.println("📡 EventServiceImpl: Delete transaction committed, sending notifications...");
                    sendEventDeletionNotifications(id, eventTitle);
                }
            });
        } else {
            sendEventDeletionNotifications(id, eventTitle);
        }
    }
    
    /**
     * Send comprehensive real-time notifications for event deletion
     */
    private void sendEventDeletionNotifications(Long eventId, String eventTitle) {
        try {
            System.out.println("🗑️ Sending real-time notifications for deleted event: " + eventTitle);
            
            // 1. Global deletion notification
            Map<String, Object> globalNotification = new HashMap<>();
            globalNotification.put("type", "EVENT_DELETED");
            globalNotification.put("eventId", eventId);
            globalNotification.put("eventTitle", eventTitle);
            globalNotification.put("message", "Event '" + eventTitle + "' has been deleted");
            globalNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendGlobalEventNotification(globalNotification);
            
            // 2. Admin dashboard notification
            Map<String, Object> adminDashboardNotification = new HashMap<>();
            adminDashboardNotification.put("type", "EVENT_DELETED");
            adminDashboardNotification.put("eventId", eventId);
            adminDashboardNotification.put("eventTitle", eventTitle);
            adminDashboardNotification.put("message", "Event '" + eventTitle + "' has been deleted successfully");
            adminDashboardNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendDashboardUpdate(adminDashboardNotification);
            
            System.out.println("✅ Sent all event deletion notifications");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send WebSocket deletion notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void updateAvailableSeats(Long eventId, Integer seatsBooked) {
        System.out.println("💺 EventServiceImpl: Updating seats for event " + eventId + ", booking " + seatsBooked + " seats");
        
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        int newAvailableSeats = event.getAvailableSeats() - seatsBooked;
        if (newAvailableSeats < 0) {
            throw new RuntimeException("Not enough available seats");
        }
        
        event.setAvailableSeats(newAvailableSeats);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
        
        System.out.println("✅ EventServiceImpl: Seats updated - Available: " + newAvailableSeats);
        
        // Send real-time seat update notification
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    System.out.println("📡 EventServiceImpl: Seat update transaction committed, sending notifications...");
                    sendSeatUpdateNotifications(eventId, event, newAvailableSeats);
                }
            });
        } else {
            sendSeatUpdateNotifications(eventId, event, newAvailableSeats);
        }
    }
    
    /**
     * Send comprehensive real-time notifications for seat updates
     */
    private void sendSeatUpdateNotifications(Long eventId, Event event, int newAvailableSeats) {
        try {
            System.out.println("💺 Sending real-time seat update notifications for event: " + event.getTitle());
            
            // 1. Send seat-specific update
            Map<String, Object> seatUpdate = new HashMap<>();
            seatUpdate.put("eventId", eventId);
            seatUpdate.put("availableSeats", newAvailableSeats);
            seatUpdate.put("totalSeats", event.getTotalSeats());
            seatUpdate.put("bookedSeats", event.getTotalSeats() - newAvailableSeats);
            seatUpdate.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendSeatUpdate(eventId.toString(), seatUpdate);
            
            // 2. Send general event update
            Map<String, Object> eventUpdate = new HashMap<>();
            eventUpdate.put("type", "SEAT_UPDATE");
            eventUpdate.put("eventId", eventId);
            eventUpdate.put("eventTitle", event.getTitle());
            eventUpdate.put("availableSeats", newAvailableSeats);
            eventUpdate.put("totalSeats", event.getTotalSeats());
            eventUpdate.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendGlobalEventNotification(eventUpdate);
            
            // 3. Send admin dashboard notification
            Map<String, Object> adminNotification = new HashMap<>();
            adminNotification.put("type", "SEAT_UPDATE");
            adminNotification.put("eventId", eventId);
            adminNotification.put("eventTitle", event.getTitle());
            adminNotification.put("availableSeats", newAvailableSeats);
            adminNotification.put("message", "Seats updated for '" + event.getTitle() + "' - " + newAvailableSeats + " seats remaining");
            adminNotification.put("timestamp", LocalDateTime.now());
            
            webSocketService.sendDashboardUpdate(adminNotification);
            
            System.out.println("✅ Sent all seat update notifications");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send WebSocket seat update notifications: " + e.getMessage());
            e.printStackTrace();
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