// src/main/java/com/eventhub/controller/HealthController.java
package com.eventhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "http://localhost:8081")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check database connection
            boolean dbHealthy = checkDatabaseHealth();
            
            response.put("status", "UP");
            response.put("timestamp", System.currentTimeMillis());
            response.put("database", dbHealthy ? "UP" : "DOWN");
            response.put("application", "EventHub Backend");
            response.put("version", "1.0.0");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("timestamp", System.currentTimeMillis());
            response.put("error", e.getMessage());
            response.put("application", "EventHub Backend");
            
            return ResponseEntity.status(503).body(response);
        }
    }
    
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        
        try {
            // Database health
            boolean dbHealthy = checkDatabaseHealth();
            components.put("database", Map.of(
                "status", dbHealthy ? "UP" : "DOWN",
                "details", dbHealthy ? "Connection successful" : "Connection failed"
            ));
            
            // Memory health
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            components.put("memory", Map.of(
                "status", "UP",
                "max", maxMemory,
                "total", totalMemory,
                "used", usedMemory,
                "free", freeMemory
            ));
            
            // Disk space (simplified)
            components.put("diskSpace", Map.of(
                "status", "UP",
                "details", "Disk space check not implemented"
            ));
            
            response.put("status", dbHealthy ? "UP" : "DOWN");
            response.put("components", components);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(503).body(response);
        }
    }
    
    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 second timeout
        } catch (Exception e) {
            return false;
        }
    }
}