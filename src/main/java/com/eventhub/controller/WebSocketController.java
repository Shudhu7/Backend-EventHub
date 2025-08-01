package com.eventhub.controller;

import com.eventhub.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {
    
    @Autowired
    private WebSocketService webSocketService;
    
    /**
     * Handle client subscription to specific event updates
     */
    @MessageMapping("/events/subscribe/{eventId}")
    @SendTo("/topic/events/{eventId}")
    public Map<String, Object> subscribeToEvent(@DestinationVariable String eventId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "SUBSCRIPTION_CONFIRMED");
        response.put("message", "Subscribed to event updates");
        response.put("eventId", eventId);
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Handle client subscription to all event updates
     */
    @SubscribeMapping("/topic/events")
    public Map<String, Object> subscribeToAllEvents(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "GLOBAL_SUBSCRIPTION_CONFIRMED");
        response.put("message", "Subscribed to all event updates");
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Handle client subscription to seat availability updates
     */
    @MessageMapping("/seats/subscribe/{eventId}")
    @SendTo("/topic/seats/{eventId}")
    public Map<String, Object> subscribeToSeatUpdates(@DestinationVariable String eventId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "SEAT_SUBSCRIPTION_CONFIRMED");
        response.put("message", "Subscribed to seat availability updates");
        response.put("eventId", eventId);
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Handle client subscription to review updates for specific event
     */
    @MessageMapping("/reviews/subscribe/{eventId}")
    @SendTo("/topic/reviews/{eventId}")
    public Map<String, Object> subscribeToReviewUpdates(@DestinationVariable String eventId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "REVIEW_SUBSCRIPTION_CONFIRMED");
        response.put("message", "Subscribed to review updates");
        response.put("eventId", eventId);
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Handle client subscription to user-specific notifications
     */
    @SubscribeMapping("/user/queue/notifications")
    public Map<String, Object> subscribeToUserNotifications(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "USER_NOTIFICATION_SUBSCRIPTION_CONFIRMED");
        response.put("message", "Subscribed to user notifications");
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Handle client subscription to booking updates
     */
    @SubscribeMapping("/user/queue/bookings")
    public Map<String, Object> subscribeToBookingUpdates(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "BOOKING_SUBSCRIPTION_CONFIRMED");
        response.put("message", "Subscribed to booking updates");
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Handle client subscription to admin dashboard updates
     */
    @SubscribeMapping("/topic/admin/dashboard")
    public Map<String, Object> subscribeToAdminDashboard(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "ADMIN_DASHBOARD_SUBSCRIPTION_CONFIRMED");
        response.put("message", "Subscribed to admin dashboard updates");
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Handle client subscription to payment updates
     */
    @MessageMapping("/payments/subscribe/{bookingId}")
    @SendTo("/topic/payments/{bookingId}")
    public Map<String, Object> subscribeToPaymentUpdates(@DestinationVariable String bookingId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "PAYMENT_SUBSCRIPTION_CONFIRMED");
        response.put("message", "Subscribed to payment updates");
        response.put("bookingId", bookingId);
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Handle client subscription to system notifications
     */
    @SubscribeMapping("/topic/system")
    public Map<String, Object> subscribeToSystemNotifications(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "SYSTEM_SUBSCRIPTION_CONFIRMED");
        response.put("message", "Subscribed to system notifications");
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Handle client heartbeat/ping messages
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public Map<String, Object> handlePing(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "PONG");
        response.put("message", "Connection alive");
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}