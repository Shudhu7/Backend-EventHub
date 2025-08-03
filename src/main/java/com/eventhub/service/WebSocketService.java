package com.eventhub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class WebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send event updates to all subscribers of a specific event
     */
    public void sendEventUpdate(String eventId, Object eventData) {
        try {
            System.out.println("📡 WebSocketService: Sending event update for event " + eventId);
            messagingTemplate.convertAndSend("/topic/events/" + eventId, eventData);
            System.out.println("✅ WebSocketService: Event update sent successfully");
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send event update - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send global event notifications to all subscribers
     */
    public void sendGlobalEventNotification(Object notification) {
        try {
            System.out.println("🌍 WebSocketService: Sending global event notification");
            messagingTemplate.convertAndSend("/topic/events", notification);
            System.out.println("✅ WebSocketService: Global notification sent successfully");
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send global notification - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send booking updates to specific user
     */
    public void sendBookingUpdate(String userId, Object bookingData) {
        try {
            System.out.println("📋 WebSocketService: Sending booking update to user " + userId);
            messagingTemplate.convertAndSendToUser(userId, "/queue/bookings", bookingData);
            System.out.println("✅ WebSocketService: Booking update sent successfully");
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send booking update - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send seat availability updates for specific event
     */
    public void sendSeatUpdate(String eventId, Map<String, Object> seatData) {
        try {
            System.out.println("💺 WebSocketService: Sending seat update for event " + eventId);
            messagingTemplate.convertAndSend("/topic/seats/" + eventId, seatData);
            System.out.println("✅ WebSocketService: Seat update sent successfully");
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send seat update - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send dashboard updates to admin users
     */
    public void sendDashboardUpdate(Object dashboardData) {
        try {
            System.out.println("👑 WebSocketService: Sending admin dashboard update");
            messagingTemplate.convertAndSend("/topic/admin/dashboard", dashboardData);
            System.out.println("✅ WebSocketService: Admin dashboard update sent successfully");
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send dashboard update - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send payment updates to specific user
     */
    public void sendPaymentUpdate(String userId, Object paymentData) {
        try {
            System.out.println("💳 WebSocketService: Sending payment update to user " + userId);
            messagingTemplate.convertAndSendToUser(userId, "/queue/payments", paymentData);
            System.out.println("✅ WebSocketService: Payment update sent successfully");
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send payment update - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send review updates for specific event
     */
    public void sendReviewUpdate(String eventId, Object reviewData) {
        try {
            System.out.println("⭐ WebSocketService: Sending review update for event " + eventId);
            messagingTemplate.convertAndSend("/topic/reviews/" + eventId, reviewData);
            System.out.println("✅ WebSocketService: Review update sent successfully");
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send review update - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send system-wide notifications
     */
    public void sendSystemNotification(Object systemData) {
        try {
            System.out.println("🔔 WebSocketService: Sending system notification");
            messagingTemplate.convertAndSend("/topic/system", systemData);
            System.out.println("✅ WebSocketService: System notification sent successfully");
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send system notification - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send notification to specific user
     */
    public void sendUserNotification(String userId, Object notificationData) {
        try {
            System.out.println("👤 WebSocketService: Sending user notification to " + userId);
            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notificationData);
            System.out.println("✅ WebSocketService: User notification sent successfully");
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send user notification - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test WebSocket connectivity
     */
    public void sendTestMessage(String message) {
        try {
            System.out.println("🧪 WebSocketService: Sending test message");
            Map<String, Object> testData = Map.of(
                "type", "TEST",
                "message", message,
                "timestamp", LocalDateTime.now()
            );
            messagingTemplate.convertAndSend("/topic/test", testData);
            System.out.println("✅ WebSocketService: Test message sent successfully");
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send test message - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send heartbeat/ping message
     */
    public void sendHeartbeat() {
        try {
            Map<String, Object> heartbeatData = Map.of(
                "type", "HEARTBEAT",
                "timestamp", LocalDateTime.now(),
                "status", "alive"
            );
            messagingTemplate.convertAndSend("/topic/heartbeat", heartbeatData);
        } catch (Exception e) {
            System.err.println("❌ WebSocketService: Failed to send heartbeat - " + e.getMessage());
        }
    }
}