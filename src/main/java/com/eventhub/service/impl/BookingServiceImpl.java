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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.eventhub.dto.PaymentRequest;
import com.eventhub.dto.PaymentResponse;
import com.eventhub.dto.RefundRequest;

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
        
        // Check if event is active and has available seats
        if (!event.getIsActive()) {
            throw new RuntimeException("Event is not active");
        }
        
        if (event.getAvailableSeats() < createBookingRequest.getNumberOfTickets()) {
            throw new RuntimeException("Not enough available seats");
        }
        
        // Check if user already has a booking for this event
        if (bookingRepository.existsByUserAndEvent(user, event)) {
            throw new RuntimeException("You already have a booking for this event");
        }
        
        // Calculate amounts
        ServiceFeeCalculator.ServiceFeeBreakdown breakdown = serviceFeeCalculator
            .getServiceFeeBreakdown(event.getPrice(), createBookingRequest.getNumberOfTickets());
        
        // Create booking
        Booking booking = new Booking();
        booking.setTicketId(generateTicketId());
        booking.setUser(user);
        booking.setEvent(event);
        booking.setNumberOfTickets(createBookingRequest.getNumberOfTickets());
        booking.setTotalAmount(breakdown.getTotalAmount());
        booking.setServiceFee(breakdown.getServiceFee());
        booking.setStatus(Booking.BookingStatus.PENDING);
        
        // Save booking
        Booking savedBooking = bookingRepository.save(booking);
        
        // Update available seats
        event.setAvailableSeats(event.getAvailableSeats() - createBookingRequest.getNumberOfTickets());
        eventRepository.save(event);
        
        return convertToDTO(savedBooking);
    }
    
    @Override
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findByIdWithEventAndUser(id)
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
        
        List<Booking> bookings = bookingRepository.findByUserIdWithEventAndUser(user.getId());
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<BookingDTO> getCurrentUserBookings(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Page<Booking> bookings = bookingRepository.findByUser(user, pageable);
        return bookings.map(this::convertToDTO);
    }
    
    @Override
    public List<BookingDTO> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdWithEventAndUser(userId);
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingDTO> getBookingsByEventId(Long eventId) {
        List<Booking> bookings = bookingRepository.findByEventIdOrderByCreatedAtDesc(eventId);
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<BookingDTO> getAllBookings(Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findAll(pageable);
        return bookings.map(this::convertToDTO);
    }
    
    @Override
    public List<BookingDTO> getBookingsByStatus(Booking.BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatus(status);
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingDTO> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findBookingsCreatedBetween(startDate, endDate);
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public BookingDTO cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithEventAndUser(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Check if booking can be cancelled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }
        
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed booking");
        }
        
        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);
        
        // Restore available seats
        Event event = booking.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + booking.getNumberOfTickets());
        eventRepository.save(event);
        
        return convertToDTO(updatedBooking);
    }
    
    @Override
    public BookingDTO confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        Booking updatedBooking = bookingRepository.save(booking);
        
        return convertToDTO(updatedBooking);
    }
    
    @Override
    public BookingDTO updateBookingStatus(Long bookingId, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);
        
        return convertToDTO(updatedBooking);
    }
    
    @Override
    public byte[] generateTicketPdf(Long bookingId) {
        try {
            Booking booking = bookingRepository.findByIdWithEventAndUser(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            return pdfGenerator.generateTicketPdf(booking);
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF ticket: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] generateTicketQRCode(String ticketId) {
        try {
            return qrCodeUtil.generateTicketQRCode(ticketId);
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR code: " + e.getMessage());
        }
    }
    
    @Override
    public BookingStatistics getBookingStatistics() {
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.getConfirmedBookingsCount();
        long pendingBookings = bookingRepository.getPendingBookingsCount();
        long cancelledBookings = bookingRepository.getCancelledBookingsCount();
        
        BigDecimal totalRevenue = bookingRepository.getTotalRevenue();
        BigDecimal totalServiceFees = bookingRepository.getTotalServiceFees();
        
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        if (totalServiceFees == null) totalServiceFees = BigDecimal.ZERO;
        
        return new BookingStatistics(totalBookings, confirmedBookings, pendingBookings,
            cancelledBookings, totalRevenue, totalServiceFees);
    }
    
    @Override
    public BigDecimal calculateTotalAmount(Long eventId, Integer numberOfTickets) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        return serviceFeeCalculator.calculateTotalAmount(event.getPrice(), numberOfTickets);
    }
    
    @Override
    public String generateTicketId() {
        String ticketId;
        do {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            ticketId = "TKT-" + timestamp + "-" + randomPart;
        } while (bookingRepository.existsByTicketId(ticketId));
        
        return ticketId;
    }
    
    @Override
    public BookingDTO convertToDTO(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setTicketId(booking.getTicketId());
        bookingDTO.setEventId(booking.getEvent().getId());
        bookingDTO.setNumberOfTickets(booking.getNumberOfTickets());
        bookingDTO.setTotalAmount(booking.getTotalAmount());
        bookingDTO.setServiceFee(booking.getServiceFee());
        bookingDTO.setStatus(booking.getStatus());
        bookingDTO.setCreatedAt(booking.getCreatedAt());
        bookingDTO.setUpdatedAt(booking.getUpdatedAt());
        
        // Event details
        bookingDTO.setEventTitle(booking.getEvent().getTitle());
        bookingDTO.setEventLocation(booking.getEvent().getLocation());
        bookingDTO.setEventImage(booking.getEvent().getImage());
        bookingDTO.setEventCategory(booking.getEvent().getCategory().toString());
        
        // User details
        bookingDTO.setUserName(booking.getUser().getName());
        bookingDTO.setUserEmail(booking.getUser().getEmail());
        
        // Payment details (if payment exists)
        if (booking.getPayment() != null) {
            bookingDTO.setTransactionId(booking.getPayment().getTransactionId());
            bookingDTO.setPaymentMethod(booking.getPayment().getPaymentMethod());
            bookingDTO.setPaymentStatus(booking.getPayment().getStatus());
        }
        
        return bookingDTO;
    }
    
    @Override
    public Booking convertToEntity(BookingDTO bookingDTO) {
        Booking booking = new Booking();
        booking.setId(bookingDTO.getId());
        booking.setTicketId(bookingDTO.getTicketId());
        booking.setNumberOfTickets(bookingDTO.getNumberOfTickets());
        booking.setTotalAmount(bookingDTO.getTotalAmount());
        booking.setServiceFee(bookingDTO.getServiceFee());
        booking.setStatus(bookingDTO.getStatus());
        
        return booking;
    }
}