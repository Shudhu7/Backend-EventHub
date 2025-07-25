package com.eventhub.controller;

import com.eventhub.service.BookingService;
import com.eventhub.service.EventService;
import com.eventhub.service.PaymentService;
import com.eventhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private PaymentService paymentService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            // Get all statistics
            UserService.UserStatistics userStats = userService.getUserStatistics();
            EventService.EventStatistics eventStats = eventService.getEventStatistics();
            BookingService.BookingStatistics bookingStats = bookingService.getBookingStatistics();
            PaymentService.PaymentStatistics paymentStats = paymentService.getPaymentStatistics();
            
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("users", userStats);
            dashboard.put("events", eventStats);
            dashboard.put("bookings", bookingStats);
            dashboard.put("payments", paymentStats);
            
            // Summary statistics
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRevenue", bookingStats.getTotalRevenue());
            summary.put("totalServiceFees", bookingStats.getTotalServiceFees());
            summary.put("totalUsers", userStats.getTotalUsers());
            summary.put("totalEvents", eventStats.getTotalEvents());
            summary.put("totalBookings", bookingStats.getTotalBookings());
            summary.put("successfulPayments", paymentStats.getSuccessfulPayments());
            
            dashboard.put("summary", summary);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", dashboard);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Map<String, Object> analytics = new HashMap<>();
            
            // Set default date range if not provided (last 30 days)
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            // Revenue analytics
            Map<String, Object> revenueAnalytics = new HashMap<>();
            revenueAnalytics.put("totalRevenue", bookingService.getBookingStatistics().getTotalRevenue());
            revenueAnalytics.put("totalServiceFees", bookingService.getBookingStatistics().getTotalServiceFees());
            revenueAnalytics.put("periodRevenue", "0.00"); // Would need date-range specific query
            
            // Booking analytics
            Map<String, Object> bookingAnalytics = new HashMap<>();
            BookingService.BookingStatistics bookingStats = bookingService.getBookingStatistics();
            bookingAnalytics.put("totalBookings", bookingStats.getTotalBookings());
            bookingAnalytics.put("confirmedBookings", bookingStats.getConfirmedBookings());
            bookingAnalytics.put("pendingBookings", bookingStats.getPendingBookings());
            bookingAnalytics.put("cancelledBookings", bookingStats.getCancelledBookings());
            bookingAnalytics.put("conversionRate", calculateConversionRate(bookingStats));
            
            // Event analytics
            Map<String, Object> eventAnalytics = new HashMap<>();
            EventService.EventStatistics eventStats = eventService.getEventStatistics();
            eventAnalytics.put("totalEvents", eventStats.getTotalEvents());
            eventAnalytics.put("activeEvents", eventStats.getActiveEvents());
            eventAnalytics.put("upcomingEvents", eventStats.getUpcomingEvents());
            eventAnalytics.put("pastEvents", eventStats.getPastEvents());
            eventAnalytics.put("availableEvents", eventStats.getAvailableEvents());
            
            // User analytics
            Map<String, Object> userAnalytics = new HashMap<>();
            UserService.UserStatistics userStats = userService.getUserStatistics();
            userAnalytics.put("totalUsers", userStats.getTotalUsers());
            userAnalytics.put("activeUsers", userStats.getActiveUsers());
            userAnalytics.put("inactiveUsers", userStats.getInactiveUsers());
            userAnalytics.put("totalAdmins", userStats.getTotalAdmins());
            
            // Payment analytics
            Map<String, Object> paymentAnalytics = new HashMap<>();
            PaymentService.PaymentStatistics paymentStats = paymentService.getPaymentStatistics();
            paymentAnalytics.put("totalPayments", paymentStats.getTotalPayments());
            paymentAnalytics.put("successfulPayments", paymentStats.getSuccessfulPayments());
            paymentAnalytics.put("failedPayments", paymentStats.getFailedPayments());
            paymentAnalytics.put("pendingPayments", paymentStats.getPendingPayments());
            paymentAnalytics.put("refundedPayments", paymentStats.getRefundedPayments());
            paymentAnalytics.put("successRate", calculatePaymentSuccessRate(paymentStats));
            paymentAnalytics.put("methodStats", paymentStats.getMethodStats());
            
            analytics.put("revenue", revenueAnalytics);
            analytics.put("bookings", bookingAnalytics);
            analytics.put("events", eventAnalytics);
            analytics.put("users", userAnalytics);
            analytics.put("payments", paymentAnalytics);
            analytics.put("dateRange", Map.of("startDate", startDate, "endDate", endDate));
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", analytics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/revenue-report")
    public ResponseEntity<?> getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Set default date range if not provided
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            Map<String, Object> revenueReport = new HashMap<>();
            BookingService.BookingStatistics bookingStats = bookingService.getBookingStatistics();
            
            revenueReport.put("totalRevenue", bookingStats.getTotalRevenue());
            revenueReport.put("totalServiceFees", bookingStats.getTotalServiceFees());
            revenueReport.put("netRevenue", calculateNetRevenue(bookingStats));
            revenueReport.put("averageBookingValue", calculateAverageBookingValue(bookingStats));
            revenueReport.put("totalBookings", bookingStats.getTotalBookings());
            revenueReport.put("confirmedBookings", bookingStats.getConfirmedBookings());
            revenueReport.put("dateRange", Map.of("startDate", startDate, "endDate", endDate));
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", revenueReport);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/user-analytics")
    public ResponseEntity<?> getUserAnalytics() {
        try {
            UserService.UserStatistics userStats = userService.getUserStatistics();
            
            Map<String, Object> userAnalytics = new HashMap<>();
            userAnalytics.put("totalUsers", userStats.getTotalUsers());
            userAnalytics.put("activeUsers", userStats.getActiveUsers());
            userAnalytics.put("inactiveUsers", userStats.getInactiveUsers());
            userAnalytics.put("totalAdmins", userStats.getTotalAdmins());
            userAnalytics.put("userGrowthRate", calculateUserGrowthRate(userStats));
            userAnalytics.put("activeUserPercentage", calculateActiveUserPercentage(userStats));
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", userAnalytics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/event-analytics")
    public ResponseEntity<?> getEventAnalytics() {
        try {
            EventService.EventStatistics eventStats = eventService.getEventStatistics();
            
            Map<String, Object> eventAnalytics = new HashMap<>();
            eventAnalytics.put("totalEvents", eventStats.getTotalEvents());
            eventAnalytics.put("activeEvents", eventStats.getActiveEvents());
            eventAnalytics.put("upcomingEvents", eventStats.getUpcomingEvents());
            eventAnalytics.put("pastEvents", eventStats.getPastEvents());
            eventAnalytics.put("availableEvents", eventStats.getAvailableEvents());
            eventAnalytics.put("eventUtilizationRate", calculateEventUtilizationRate(eventStats));
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", eventAnalytics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/booking-analytics")
    public ResponseEntity<?> getBookingAnalytics() {
        try {
            BookingService.BookingStatistics bookingStats = bookingService.getBookingStatistics();
            
            Map<String, Object> bookingAnalytics = new HashMap<>();
            bookingAnalytics.put("totalBookings", bookingStats.getTotalBookings());
            bookingAnalytics.put("confirmedBookings", bookingStats.getConfirmedBookings());
            bookingAnalytics.put("pendingBookings", bookingStats.getPendingBookings());
            bookingAnalytics.put("cancelledBookings", bookingStats.getCancelledBookings());
            bookingAnalytics.put("conversionRate", calculateConversionRate(bookingStats));
            bookingAnalytics.put("cancellationRate", calculateCancellationRate(bookingStats));
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", bookingAnalytics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/payment-analytics")
    public ResponseEntity<?> getPaymentAnalytics() {
        try {
            PaymentService.PaymentStatistics paymentStats = paymentService.getPaymentStatistics();
            
            Map<String, Object> paymentAnalytics = new HashMap<>();
            paymentAnalytics.put("totalPayments", paymentStats.getTotalPayments());
            paymentAnalytics.put("successfulPayments", paymentStats.getSuccessfulPayments());
            paymentAnalytics.put("failedPayments", paymentStats.getFailedPayments());
            paymentAnalytics.put("pendingPayments", paymentStats.getPendingPayments());
            paymentAnalytics.put("refundedPayments", paymentStats.getRefundedPayments());
            paymentAnalytics.put("totalAmount", paymentStats.getTotalAmount());
            paymentAnalytics.put("successfulAmount", paymentStats.getSuccessfulAmount());
            paymentAnalytics.put("successRate", calculatePaymentSuccessRate(paymentStats));
            paymentAnalytics.put("methodStats", paymentStats.getMethodStats());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", paymentAnalytics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Helper methods for calculations
    private double calculateConversionRate(BookingService.BookingStatistics stats) {
        if (stats.getTotalBookings() == 0) return 0.0;
        return (double) stats.getConfirmedBookings() / stats.getTotalBookings() * 100;
    }
    
    private double calculateCancellationRate(BookingService.BookingStatistics stats) {
        if (stats.getTotalBookings() == 0) return 0.0;
        return (double) stats.getCancelledBookings() / stats.getTotalBookings() * 100;
    }
    
    private double calculatePaymentSuccessRate(PaymentService.PaymentStatistics stats) {
        if (stats.getTotalPayments() == 0) return 0.0;
        return (double) stats.getSuccessfulPayments() / stats.getTotalPayments() * 100;
    }
    
    private BigDecimal calculateNetRevenue(BookingService.BookingStatistics stats) {
        return stats.getTotalRevenue().subtract(stats.getTotalServiceFees());
    }
    
    private BigDecimal calculateAverageBookingValue(BookingService.BookingStatistics stats) {
        if (stats.getConfirmedBookings() == 0) return BigDecimal.ZERO;
        return stats.getTotalRevenue().divide(new BigDecimal(stats.getConfirmedBookings()), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    private double calculateUserGrowthRate(UserService.UserStatistics stats) {
        // This would require historical data to calculate properly
        // For now, return a placeholder
        return 0.0;
    }
    
    private double calculateActiveUserPercentage(UserService.UserStatistics stats) {
        if (stats.getTotalUsers() == 0) return 0.0;
        return (double) stats.getActiveUsers() / stats.getTotalUsers() * 100;
    }
    
    private double calculateEventUtilizationRate(EventService.EventStatistics stats) {
        if (stats.getTotalEvents() == 0) return 0.0;
        return (double) stats.getActiveEvents() / stats.getTotalEvents() * 100;
    }
}