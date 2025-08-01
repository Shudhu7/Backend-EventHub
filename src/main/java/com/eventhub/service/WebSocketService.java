// src/main/java/com/eventhub/service/WebSocketService.java
package com.eventhub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Send event updates to all subscribers
    public void sendEventUpdate(String eventId, Object eventData) {
        messagingTemplate.convertAndSend("/topic/events/" + eventId, eventData);
    }
    
    // Send global event notifications
    public void sendGlobalEventNotification(Object notification) {
        messagingTemplate.convertAndSend("/topic/events", notification);
    }
    
    // Send booking updates
    public void sendBookingUpdate(String userId, Object bookingData) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/bookings", bookingData);
    }
    
    // Send seat availability updates
    public void sendSeatUpdate(String eventId, Map<String, Object> seatData) {
        messagingTemplate.convertAndSend("/topic/seats/" + eventId, seatData);
    }
    
    // Send dashboard updates to admins
    public void sendDashboardUpdate(Object dashboardData) {
        messagingTemplate.convertAndSend("/topic/admin/dashboard", dashboardData);
    }
}