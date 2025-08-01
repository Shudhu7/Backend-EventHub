package com.eventhub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics and queues
        config.enableSimpleBroker(
            "/topic",    // For broadcasting to multiple subscribers
            "/queue"     // For point-to-point messaging
        );
        
        // Set application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint with CORS support
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                    "http://localhost:3000",
                    "http://localhost:5173", 
                    "http://localhost:8081",
                    "https://myeventhub.vercel.app",
                    "https://*.netlify.app"
                )
                .withSockJS(); // Enable SockJS fallback options
    }
}