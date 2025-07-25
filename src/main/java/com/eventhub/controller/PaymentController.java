package com.eventhub.controller;

import com.eventhub.dto.PaymentRequest;
import com.eventhub.dto.PaymentResponse;
import com.eventhub.dto.RefundRequest;
import com.eventhub.model.entity.Payment;
import com.eventhub.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/process")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment processed successfully");
            response.put("data", paymentResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentByTransactionId(@PathVariable String transactionId) {
        try {
            PaymentResponse paymentResponse = paymentService.getPaymentByTransactionId(transactionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", paymentResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentByBookingId(@PathVariable Long bookingId) {
        try {
            PaymentResponse paymentResponse = paymentService.getPaymentByBookingId(bookingId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", paymentResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentsByUserId(@PathVariable Long userId) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", payments);
            response.put("count", payments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllPayments() {
        try {
            List<PaymentResponse> payments = paymentService.getAllPayments();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", payments);
            response.put("count", payments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentsByStatus(@PathVariable Payment.PaymentStatus status) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", payments);
            response.put("count", payments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/method/{method}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentsByMethod(@PathVariable Payment.PaymentMethod method) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByPaymentMethod(method);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", payments);
            response.put("count", payments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByDateRange(startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", payments);
            response.put("count", payments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> processRefund(@Valid @RequestBody RefundRequest refundRequest) {
        try {
            PaymentResponse refundResponse = paymentService.processRefund(refundRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Refund processed successfully");
            response.put("data", refundResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/verify")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> verifyPayment(@RequestParam String transactionId) {
        try {
            boolean isValid = paymentService.verifyPayment(transactionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("transactionId", transactionId);
            response.put("valid", isValid);
            response.put("message", isValid ? "Payment verified successfully" : "Payment verification failed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PutMapping("/{transactionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable String transactionId, 
                                               @RequestParam Payment.PaymentStatus status) {
        try {
            PaymentResponse paymentResponse = paymentService.updatePaymentStatus(transactionId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment status updated successfully");
            response.put("data", paymentResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/card")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> processCardPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            paymentRequest.setPaymentMethod(Payment.PaymentMethod.CARD);
            PaymentResponse paymentResponse = paymentService.processCardPayment(paymentRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Card payment processed successfully");
            response.put("data", paymentResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/upi")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> processUpiPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            paymentRequest.setPaymentMethod(Payment.PaymentMethod.UPI);
            PaymentResponse paymentResponse = paymentService.processUpiPayment(paymentRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "UPI payment processed successfully");
            response.put("data", paymentResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/netbanking")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> processNetBankingPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            paymentRequest.setPaymentMethod(Payment.PaymentMethod.NET_BANKING);
            PaymentResponse paymentResponse = paymentService.processNetBankingPayment(paymentRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Net banking payment processed successfully");
            response.put("data", paymentResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentStatistics() {
        try {
            PaymentService.PaymentStatistics statistics = paymentService.getPaymentStatistics();
            
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
}