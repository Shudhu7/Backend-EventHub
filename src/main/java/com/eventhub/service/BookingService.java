package com.eventhub.service;

import com.eventhub.dto.BookingDTO;
import com.eventhub.dto.CreateBookingRequest;
import com.eventhub.model.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    
    /**
     * Create new booking
     */
    BookingDTO createBooking(CreateBookingRequest createBookingRequest);
    
    /**
     * Get booking by ID
     */
    BookingDTO getBookingById(Long id);
    
    /**
     * Get booking by ticket ID
     */
    BookingDTO getBookingByTicketId(String ticketId);
    
    /**
     * Get all bookings for current user
     */
    List<BookingDTO> getCurrentUserBookings();
    
    /**
     * Get bookings for current user with pagination
     */
    Page<BookingDTO> getCurrentUserBookings(Pageable pageable);
    
    /**
     * Get bookings by user ID (Admin only)
     */
    List<BookingDTO> getBookingsByUserId(Long userId);
    
    /**
     * Get bookings by event ID (Admin only)
     */
    List<BookingDTO> getBookingsByEventId(Long eventId);
    
    /**
     * Get all bookings (Admin only)
     */
    List<BookingDTO> getAllBookings();
    
    /**
     * Get all bookings with pagination (Admin only)
     */
    Page<BookingDTO> getAllBookings(Pageable pageable);
    
    /**
     * Get bookings by status
     */
    List<BookingDTO> getBookingsByStatus(Booking.BookingStatus status);
    
    /**
     * Get bookings by date range
     */
    List<BookingDTO> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Cancel booking
     */
    BookingDTO cancelBooking(Long bookingId);
    
    /**
     * Confirm booking (after successful payment)
     */
    BookingDTO confirmBooking(Long bookingId);
    
    /**
     * Update booking status
     */
    BookingDTO updateBookingStatus(Long bookingId, Booking.BookingStatus status);
    
    /**
     * Generate ticket PDF
     */
    byte[] generateTicketPdf(Long bookingId);
    
    /**
     * Generate QR code for ticket
     */
    byte[] generateTicketQRCode(String ticketId);
    
    /**
     * Get booking statistics (Admin only)
     */
    BookingStatistics getBookingStatistics();
    
    /**
     * Calculate total amount including service fee
     */
    BigDecimal calculateTotalAmount(Long eventId, Integer numberOfTickets);
    
    /**
     * Generate unique ticket ID
     */
    String generateTicketId();
    
    /**
     * Convert entity to DTO
     */
    BookingDTO convertToDTO(Booking booking);
    
    /**
     * Convert DTO to entity
     */
    Booking convertToEntity(BookingDTO bookingDTO);
    
    /**
     * Inner class for booking statistics
     */
    class BookingStatistics {
        private long totalBookings;
        private long confirmedBookings;
        private long pendingBookings;
        private long cancelledBookings;
        private BigDecimal totalRevenue;
        private BigDecimal totalServiceFees;
        
        // Constructors
        public BookingStatistics() {}
        
        public BookingStatistics(long totalBookings, long confirmedBookings, long pendingBookings,
                               long cancelledBookings, BigDecimal totalRevenue, BigDecimal totalServiceFees) {
            this.totalBookings = totalBookings;
            this.confirmedBookings = confirmedBookings;
            this.pendingBookings = pendingBookings;
            this.cancelledBookings = cancelledBookings;
            this.totalRevenue = totalRevenue;
            this.totalServiceFees = totalServiceFees;
        }
        
        // Getters and Setters
        public long getTotalBookings() { return totalBookings; }
        public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }
        
        public long getConfirmedBookings() { return confirmedBookings; }
        public void setConfirmedBookings(long confirmedBookings) { this.confirmedBookings = confirmedBookings; }
        
        public long getPendingBookings() { return pendingBookings; }
        public void setPendingBookings(long pendingBookings) { this.pendingBookings = pendingBookings; }
        
        public long getCancelledBookings() { return cancelledBookings; }
        public void setCancelledBookings(long cancelledBookings) { this.cancelledBookings = cancelledBookings; }
        
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public BigDecimal getTotalServiceFees() { return totalServiceFees; }
        public void setTotalServiceFees(BigDecimal totalServiceFees) { this.totalServiceFees = totalServiceFees; }
    }
}