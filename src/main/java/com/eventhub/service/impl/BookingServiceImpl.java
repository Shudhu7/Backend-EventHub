package com.eventhub.service.impl;

import com.eventhub.dto.BookingDTO;
import com.eventhub.dto.CreateBookingRequest;
import com.eventhub.model.entity.Booking;
import com.eventhub.model.entity.Event;
import com.eventhub.model.entity.User;
import com.eventhub.repository.BookingRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.service.BookingService;
import com.eventhub.util.PdfGenerator;
import com.eventhub.util.QrCodeUtil;
import com.eventhub.util.ServiceFeeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ServiceFeeCalculator serviceFeeCalculator;
    
    @Autowired
    private PdfGenerator pdfGenerator;
    
    @Autowired
    private QrCodeUtil qrCodeUtil;
    
    @Override
    public BookingDTO createBooking(CreateBookingRequest createBookingRequest) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get event
        Event event = eventRepository.findById(createBookingRequest.getEventId())
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        // Check if event is active and available
        if (!event.getIsActive()) {
            throw new RuntimeException("Event is not active");
        }
        
        if (event.getAvailableSeats() < createBookingRequest.getNumberOfTickets()) {
            throw new RuntimeException("Not enough available seats");
        }
        
        // Check if event date is in the future
        if (event.getDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new RuntimeException("Cannot book past events");
        }
        
        // Calculate fees
        BigDecimal subtotal = event.getPrice().multiply(new BigDecimal(createBookingRequest.getNumberOfTickets()));
        BigDecimal serviceFee = serviceFeeCalculator.calculateServiceFee(subtotal);
        BigDecimal totalAmount = subtotal.add(serviceFee);
        
        // Create booking
        Booking booking = new Booking();
        booking.setTicketId(generateTicketId());
        booking.setUser(user);
        booking.setEvent(event);
        booking.setNumberOfTickets(createBookingRequest.getNumberOfTickets());
        booking.setTotalAmount(totalAmount);
        booking.setServiceFee(serviceFee);
        booking.setStatus(Booking.BookingStatus.PENDING);
        
        // Update available seats
        event.setAvailableSeats(event.getAvailableSeats() - createBookingRequest.getNumberOfTickets());
        eventRepository.save(event);
        
        // Save booking
        Booking savedBooking = bookingRepository.save(booking);
        
        return convertToDTO(savedBooking);
    }
    
    @Override
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return convertToDTO(booking);
    }
    
    @Override
    public BookingDTO getBookingByTicketId(String ticketId) {
        Booking booking = bookingRepository.findByTicketId(ticketId)
            .orElseThrow(() -> new RuntimeException("Booking not found with ticket ID: " + ticketId));
        return convertToDTO(booking);
    }
    
    @Override
    public List<BookingDTO> getCurrentUserBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Booking> bookings = bookingRepository.findByUserOrderByCreatedAtDesc(user);
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<BookingDTO> getCurrentUserBookings(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Page<Booking> bookings = bookingRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return bookings.map(this::convertToDTO);
    }
    
    @Override
    public List<BookingDTO> getBookingsByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        List<Booking> bookings = bookingRepository.findByUserOrderByCreatedAtDesc(user);
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingDTO> getBookingsByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        List<Booking> bookings = bookingRepository.findByEventOrderByCreatedAtDesc(event);
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<BookingDTO> getAllBookings(Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findAllByOrderByCreatedAtDesc(pageable);
        return bookings.map(this::convertToDTO);
    }
    
    @Override
    public List<BookingDTO> getBookingsByStatus(Booking.BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatusOrderByCreatedAtDesc(status);
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingDTO> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public BookingDTO updateBookingStatus(Long id, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        Booking.BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(status);
        
        // Handle seat availability when status changes
        if (oldStatus == Booking.BookingStatus.CONFIRMED && status == Booking.BookingStatus.CANCELLED) {
            // Release seats back to event
            Event event = booking.getEvent();
            event.setAvailableSeats(event.getAvailableSeats() + booking.getNumberOfTickets());
            eventRepository.save(event);
        }
        
        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }
    
    @Override
    public BookingDTO confirmBooking(Long id) {
        return updateBookingStatus(id, Booking.BookingStatus.CONFIRMED);
    }
    
    @Override
    public BookingDTO cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Check if booking can be cancelled (e.g., not past event date)
        if (booking.getEvent().getDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new RuntimeException("Cannot cancel booking for past events");
        }
        
        return updateBookingStatus(id, Booking.BookingStatus.CANCELLED);
    }
    
    @Override
    public BookingStatistics getBookingStatistics() {
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED);
        long pendingBookings = bookingRepository.countByStatus(Booking.BookingStatus.PENDING);
        long cancelledBookings = bookingRepository.countByStatus(Booking.BookingStatus.CANCELLED);
        
        BigDecimal totalRevenue = bookingRepository.getTotalRevenue();
        BigDecimal totalServiceFees = bookingRepository.getTotalServiceFees();
        
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        if (totalServiceFees == null) totalServiceFees = BigDecimal.ZERO;
        
        return new BookingStatistics(totalBookings, confirmedBookings, pendingBookings, 
            cancelledBookings, totalRevenue, totalServiceFees);
    }
    
    @Override
    public byte[] generateTicketPdf(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Ticket can only be generated for confirmed bookings");
        }
        
        try {
            // Generate QR code for ticket
            String qrData = generateQrCodeData(booking);
            byte[] qrCode = qrCodeUtil.generateQrCode(qrData);
            
            // Generate PDF ticket
            return pdfGenerator.generateTicket(booking, qrCode);
        } catch (Exception e) {
            throw new RuntimeException("Error generating ticket PDF: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] generateTicketQRCode(String ticketId) {
        Booking booking = bookingRepository.findByTicketId(ticketId)
            .orElseThrow(() -> new RuntimeException("Booking not found with ticket ID: " + ticketId));
        
        try {
            String qrData = generateQrCodeData(booking);
            return qrCodeUtil.generateQrCode(qrData);
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR code: " + e.getMessage());
        }
    }
    
    @Override
    public BigDecimal calculateTotalAmount(Long eventId, Integer numberOfTickets) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        BigDecimal subtotal = event.getPrice().multiply(new BigDecimal(numberOfTickets));
        BigDecimal serviceFee = serviceFeeCalculator.calculateServiceFee(subtotal);
        return subtotal.add(serviceFee);
    }
    
    @Override
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Only allow deletion of pending or cancelled bookings
        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Cannot delete confirmed booking. Cancel it first.");
        }
        
        // If pending, release the seats
        if (booking.getStatus() == Booking.BookingStatus.PENDING) {
            Event event = booking.getEvent();
            event.setAvailableSeats(event.getAvailableSeats() + booking.getNumberOfTickets());
            eventRepository.save(event);
        }
        
        bookingRepository.delete(booking);
    }
    
    // Helper methods
    private String generateTicketId() {
        return "TKT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateQrCodeData(Booking booking) {
        return String.format("TICKET:%s|EVENT:%d|USER:%d|TICKETS:%d|DATE:%s",
            booking.getTicketId(),
            booking.getEvent().getId(),
            booking.getUser().getId(),
            booking.getNumberOfTickets(),
            booking.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
    
    @Override
    public BookingDTO convertToDTO(Booking booking) {
        if (booking == null) return null;
        
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setTicketId(booking.getTicketId());
        dto.setEventId(booking.getEvent().getId());
        dto.setNumberOfTickets(booking.getNumberOfTickets());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setServiceFee(booking.getServiceFee());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        
        // Event details
        dto.setEventTitle(booking.getEvent().getTitle());
        dto.setEventLocation(booking.getEvent().getLocation());
        dto.setEventImage(booking.getEvent().getImage());
        dto.setEventCategory(booking.getEvent().getCategory().name());
        
        // User details
        dto.setUserName(booking.getUser().getName());
        dto.setUserEmail(booking.getUser().getEmail());
        
        // Payment details if payment exists
        if (booking.getPayment() != null) {
            dto.setTransactionId(booking.getPayment().getTransactionId());
            dto.setPaymentMethod(booking.getPayment().getPaymentMethod());
            dto.setPaymentStatus(booking.getPayment().getStatus());
        }
        
        return dto;
    }
}