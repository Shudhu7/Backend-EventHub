// src/main/java/com/eventhub/controller/EventController.java
package com.eventhub.controller;

import com.eventhub.dto.EventDTO;
import com.eventhub.model.entity.Event;
import com.eventhub.service.EventService;
import com.eventhub.service.WebSocketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EventController {
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @GetMapping
    public ResponseEntity<?> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<EventDTO> events;
            
            // Handle category filtering - convert string to enum
            if (category != null && !category.isEmpty()) {
                try {
                    Event.Category categoryEnum = Event.Category.valueOf(category.toUpperCase());
                    events = eventService.getEventsByCategory(categoryEnum, pageable);
                } catch (IllegalArgumentException e) {
                    // Invalid category, return all events
                    events = eventService.getAllActiveEvents(pageable);
                }
            } else if (search != null && !search.isEmpty()) {
                // For search, we need to get list and convert to page manually
                // since searchEvents returns List, not Page
                List<EventDTO> searchResults = eventService.searchEvents(search);
                events = convertListToPage(searchResults, pageable);
            } else {
                events = eventService.getAllActiveEvents(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", events.getContent());
            response.put("currentPage", events.getNumber());
            response.put("totalPages", events.getTotalPages());
            response.put("totalElements", events.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        try {
            EventDTO event = eventService.getEventById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", event);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventDTO eventDTO) {
        try {
            System.out.println("🎯 EventController: Creating event - " + eventDTO.getTitle());
            
            EventDTO createdEvent = eventService.createEvent(eventDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Event created successfully");
            response.put("data", createdEvent);
            
            // 🚀 ADDITIONAL CONTROLLER-LEVEL WEBSOCKET NOTIFICATIONS
            try {
                // Send immediate dashboard notification for admin UI responsiveness
                Map<String, Object> immediateNotification = new HashMap<>();
                immediateNotification.put("type", "EVENT_CREATION_SUCCESS");
                immediateNotification.put("eventId", createdEvent.getId());
                immediateNotification.put("eventTitle", createdEvent.getTitle());
                immediateNotification.put("message", "Event '" + createdEvent.getTitle() + "' created successfully!");
                immediateNotification.put("timestamp", LocalDateTime.now());
                immediateNotification.put("source", "EventController");
                
                webSocketService.sendDashboardUpdate(immediateNotification);
                
                System.out.println("✅ EventController: Sent immediate dashboard notification");
                
            } catch (Exception wsError) {
                System.err.println("⚠️ EventController: Failed to send immediate WebSocket notification: " + wsError.getMessage());
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("❌ EventController: Failed to create event: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @Valid @RequestBody EventDTO eventDTO) {
        try {
            System.out.println("🔄 EventController: Updating event - " + id);
            
            EventDTO updatedEvent = eventService.updateEvent(id, eventDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Event updated successfully");
            response.put("data", updatedEvent);
            
            // 🚀 ADDITIONAL CONTROLLER-LEVEL UPDATE NOTIFICATIONS
            try {
                Map<String, Object> immediateUpdateNotification = new HashMap<>();
                immediateUpdateNotification.put("type", "EVENT_UPDATE_SUCCESS");
                immediateUpdateNotification.put("eventId", updatedEvent.getId());
                immediateUpdateNotification.put("eventTitle", updatedEvent.getTitle());
                immediateUpdateNotification.put("message", "Event '" + updatedEvent.getTitle() + "' updated successfully!");
                immediateUpdateNotification.put("timestamp", LocalDateTime.now());
                immediateUpdateNotification.put("source", "EventController");
                
                webSocketService.sendDashboardUpdate(immediateUpdateNotification);
                
                System.out.println("✅ EventController: Sent immediate update notification");
                
            } catch (Exception wsError) {
                System.err.println("⚠️ EventController: Failed to send update WebSocket notification: " + wsError.getMessage());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ EventController: Failed to update event: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            System.out.println("🗑️ EventController: Deleting event - " + id);
            
            // Get event details before deletion for notification
            EventDTO eventToDelete = eventService.getEventById(id);
            
            eventService.deleteEvent(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Event deleted successfully");
            
            // 🚀 SEND DELETION NOTIFICATION
            try {
                Map<String, Object> deletionNotification = new HashMap<>();
                deletionNotification.put("type", "EVENT_DELETED");
                deletionNotification.put("eventId", id);
                deletionNotification.put("eventTitle", eventToDelete.getTitle());
                deletionNotification.put("message", "Event '" + eventToDelete.getTitle() + "' has been deleted");
                deletionNotification.put("timestamp", LocalDateTime.now());
                deletionNotification.put("source", "EventController");
                
                webSocketService.sendDashboardUpdate(deletionNotification);
                webSocketService.sendGlobalEventNotification(deletionNotification);
                
                System.out.println("✅ EventController: Sent deletion notification");
                
            } catch (Exception wsError) {
                System.err.println("⚠️ EventController: Failed to send deletion WebSocket notification: " + wsError.getMessage());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ EventController: Failed to delete event: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchEvents(@RequestParam String keyword) {
        try {
            List<EventDTO> events = eventService.searchEvents(keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", events);
            response.put("count", events.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getEventsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Event.Category categoryEnum = Event.Category.valueOf(category.toUpperCase());
            Pageable pageable = PageRequest.of(page, size);
            Page<EventDTO> events = eventService.getEventsByCategory(categoryEnum, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", events.getContent());
            response.put("currentPage", events.getNumber());
            response.put("totalItems", events.getTotalElements());
            response.put("totalPages", events.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Invalid category: " + category);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/filter")
    public ResponseEntity<?> filterEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Event.Category categoryEnum = null;
            if (category != null && !category.isEmpty()) {
                try {
                    categoryEnum = Event.Category.valueOf(category.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Invalid category, will be treated as null
                }
            }
            
            Page<EventDTO> events = eventService.filterEvents(
                categoryEnum, location, startDate, endDate, minPrice, maxPrice, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", events.getContent());
            response.put("currentPage", events.getNumber());
            response.put("totalItems", events.getTotalElements());
            response.put("totalPages", events.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingEvents(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<EventDTO> upcomingEvents = eventService.getUpcomingEvents();
            
            // Limit the results if needed
            if (limit > 0 && upcomingEvents.size() > limit) {
                upcomingEvents = upcomingEvents.subList(0, limit);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", upcomingEvents);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableEvents() {
        try {
            List<EventDTO> availableEvents = eventService.getAvailableEvents();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", availableEvents);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEventStatistics() {
        try {
            EventService.EventStatistics statistics = eventService.getEventStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Helper method to convert List to Page for compatibility
     */
    private Page<EventDTO> convertListToPage(List<EventDTO> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        
        List<EventDTO> pageContent = list.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, list.size());
    }
}