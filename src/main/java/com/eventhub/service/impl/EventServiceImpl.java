package com.eventhub.service.impl;

import com.eventhub.dto.EventDTO;
import com.eventhub.model.entity.Event;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.ReviewRepository;
import com.eventhub.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.eventhub.dto.PaymentRequest;
import com.eventhub.dto.PaymentResponse;
import com.eventhub.dto.RefundRequest;
@Service
@Transactional
public class EventServiceImpl implements EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Override
    public EventDTO createEvent(EventDTO eventDTO) {
        Event event = convertToEntity(eventDTO);
        event.setAvailableSeats(event.getTotalSeats());
        event.setIsActive(true);
        
        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
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
        
        // Update total seats and adjust available seats if needed
        if (eventDTO.getTotalSeats() != null) {
            int seatDifference = eventDTO.getTotalSeats() - existingEvent.getTotalSeats();
            existingEvent.setTotalSeats(eventDTO.getTotalSeats());
            existingEvent.setAvailableSeats(existingEvent.getAvailableSeats() + seatDifference);
        }
        
        Event updatedEvent = eventRepository.save(existingEvent);
        return convertToDTO(updatedEvent);
    }
    
    @Override
    public EventDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return convertToDTO(event);
    }
    
    @Override
    public List<EventDTO> getAllActiveEvents() {
        List<Event> events = eventRepository.findByIsActiveTrue();
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<EventDTO> getAllActiveEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findByIsActiveTrue(pageable);
        return events.map(this::convertToDTO);
    }
    
    @Override
    public List<EventDTO> getEventsByCategory(Event.Category category) {
        List<Event> events = eventRepository.findByCategory(category);
        return events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<EventDTO> getEventsByCategory(Event.Category category, Pageable pageable) {
        Page<Event> events = eventRepository.findByCategoryAndIsActiveTrue(category, pageable);
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
        Page<Event> events = eventRepository.findEventsWithFilters(
            category, location, startDate, endDate, minPrice, maxPrice, pageable);
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
        eventRepository.save(event);
    }
    
    @Override
    public void updateAvailableSeats(Long eventId, Integer seatsBooked) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        if (event.getAvailableSeats() < seatsBooked) {
            throw new RuntimeException("Not enough available seats");
        }
        
        event.setAvailableSeats(event.getAvailableSeats() - seatsBooked);
        eventRepository.save(event);
    }
    
    @Override
    public EventStatistics getEventStatistics() {
        long totalEvents = eventRepository.count();
        long activeEvents = eventRepository.countActiveEvents();
        long upcomingEvents = eventRepository.countUpcomingEvents();
        long pastEvents = totalEvents - upcomingEvents;
        long availableEvents = eventRepository.findAvailableEvents().size();
        
        return new EventStatistics(totalEvents, activeEvents, upcomingEvents, pastEvents, availableEvents);
    }
    
    @Override
    public EventDTO convertToDTO(Event event) {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(event.getId());
        eventDTO.setTitle(event.getTitle());
        eventDTO.setDescription(event.getDescription());
        eventDTO.setDate(event.getDate());
        eventDTO.setTime(event.getTime());
        eventDTO.setLocation(event.getLocation());
        eventDTO.setPrice(event.getPrice());
        eventDTO.setTotalSeats(event.getTotalSeats());
        eventDTO.setAvailableSeats(event.getAvailableSeats());
        eventDTO.setCategory(event.getCategory());
        eventDTO.setImage(event.getImage());
        eventDTO.setCreatedAt(event.getCreatedAt());
        eventDTO.setUpdatedAt(event.getUpdatedAt());
        eventDTO.setIsActive(event.getIsActive());
        
        // Calculate average rating and review count
        Double averageRating = reviewRepository.getAverageRatingByEventId(event.getId());
        Long reviewCount = reviewRepository.getReviewCountByEventId(event.getId());
        
        eventDTO.setAverageRating(averageRating != null ? averageRating : 0.0);
        eventDTO.setTotalReviews(reviewCount != null ? reviewCount.intValue() : 0);
        
        return eventDTO;
    }
    
    @Override
    public Event convertToEntity(EventDTO eventDTO) {
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
        
        return event;
    }
}