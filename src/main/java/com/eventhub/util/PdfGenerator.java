package com.eventhub.util;

import com.eventhub.model.entity.Booking;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Component
public class PdfGenerator {
    
    /**
     * Generate ticket PDF for a booking (simplified version without external dependencies)
     * @param booking The booking to generate ticket for
     * @param qrCode QR code as byte array
     * @return PDF as byte array (currently returns text content)
     * @throws Exception if PDF generation fails
     */
    public byte[] generateTicket(Booking booking, byte[] qrCode) throws Exception {
        try {
            // For now, generate a simple text-based ticket
            StringBuilder ticketContent = new StringBuilder();
            ticketContent.append("=".repeat(50)).append("\n");
            ticketContent.append("         EVENTHUB TICKET\n");
            ticketContent.append("=".repeat(50)).append("\n\n");
            
            ticketContent.append("EVENT DETAILS:\n");
            ticketContent.append("-".repeat(20)).append("\n");
            ticketContent.append("Event: ").append(booking.getEvent().getTitle()).append("\n");
            ticketContent.append("Date: ").append(booking.getEvent().getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))).append("\n");
            ticketContent.append("Time: ").append(booking.getEvent().getTime().format(DateTimeFormatter.ofPattern("hh:mm a"))).append("\n");
            ticketContent.append("Venue: ").append(booking.getEvent().getLocation()).append("\n");
            ticketContent.append("Category: ").append(booking.getEvent().getCategory()).append("\n\n");
            
            ticketContent.append("BOOKING DETAILS:\n");
            ticketContent.append("-".repeat(20)).append("\n");
            ticketContent.append("Ticket ID: ").append(booking.getTicketId()).append("\n");
            ticketContent.append("Tickets: ").append(booking.getNumberOfTickets()).append("\n");
            ticketContent.append("Subtotal: ₹").append(booking.getTotalAmount().subtract(booking.getServiceFee())).append("\n");
            ticketContent.append("Service Fee: ₹").append(booking.getServiceFee()).append("\n");
            ticketContent.append("Total Amount: ₹").append(booking.getTotalAmount()).append("\n");
            ticketContent.append("Status: ").append(booking.getStatus()).append("\n");
            ticketContent.append("Booked On: ").append(booking.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))).append("\n\n");
            
            ticketContent.append("CUSTOMER DETAILS:\n");
            ticketContent.append("-".repeat(20)).append("\n");
            ticketContent.append("Name: ").append(booking.getUser().getName()).append("\n");
            ticketContent.append("Email: ").append(booking.getUser().getEmail()).append("\n");
            if (booking.getUser().getPhone() != null) {
                ticketContent.append("Phone: ").append(booking.getUser().getPhone()).append("\n");
            }
            ticketContent.append("\n");
            
            ticketContent.append("TERMS & CONDITIONS:\n");
            ticketContent.append("-".repeat(20)).append("\n");
            ticketContent.append("• This ticket is non-transferable and must be presented along with a valid ID.\n");
            ticketContent.append("• Entry is subject to security checks and event organizer's terms.\n");
            ticketContent.append("• No refunds will be provided unless the event is cancelled.\n");
            ticketContent.append("• Please arrive at least 30 minutes before the event start time.\n");
            ticketContent.append("• EventHub is not responsible for any loss, damage, or injury during the event.\n\n");
            
            ticketContent.append("For support: support@eventhub.com | +91-1234567890\n");
            ticketContent.append("=".repeat(50)).append("\n");
            
            return ticketContent.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new Exception("Failed to generate ticket: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate booking summary PDF (simplified version)
     * @param booking The booking to generate summary for
     * @return PDF as byte array (currently returns text content)
     * @throws Exception if PDF generation fails
     */
    public byte[] generateBookingSummary(Booking booking) throws Exception {
        try {
            StringBuilder summary = new StringBuilder();
            summary.append("BOOKING SUMMARY\n");
            summary.append("=".repeat(30)).append("\n\n");
            
            summary.append("Booking ID: ").append(booking.getId()).append("\n");
            summary.append("Ticket ID: ").append(booking.getTicketId()).append("\n");
            summary.append("Event: ").append(booking.getEvent().getTitle()).append("\n");
            summary.append("Customer: ").append(booking.getUser().getName()).append("\n");
            summary.append("Email: ").append(booking.getUser().getEmail()).append("\n");
            summary.append("Tickets: ").append(booking.getNumberOfTickets()).append("\n");
            summary.append("Total Amount: ₹").append(booking.getTotalAmount()).append("\n");
            summary.append("Status: ").append(booking.getStatus()).append("\n");
            summary.append("Booking Date: ").append(booking.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))).append("\n");
            
            return summary.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new Exception("Failed to generate booking summary: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate invoice PDF (simplified version)
     * @param booking The booking to generate invoice for
     * @return PDF as byte array (currently returns text content)
     * @throws Exception if PDF generation fails
     */
    public byte[] generateInvoice(Booking booking) throws Exception {
        try {
            StringBuilder invoice = new StringBuilder();
            invoice.append("EVENTHUB INVOICE\n");
            invoice.append("=".repeat(30)).append("\n\n");
            
            invoice.append("Invoice Date: ").append(booking.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))).append("\n");
            invoice.append("Transaction ID: ").append(booking.getTicketId()).append("\n\n");
            
            invoice.append("BILLING INFORMATION:\n");
            invoice.append("-".repeat(20)).append("\n");
            invoice.append("Customer: ").append(booking.getUser().getName()).append("\n");
            invoice.append("Email: ").append(booking.getUser().getEmail()).append("\n\n");
            
            invoice.append("ITEMS:\n");
            invoice.append("-".repeat(20)).append("\n");
            invoice.append("Description: ").append(booking.getEvent().getTitle()).append("\n");
            invoice.append("Quantity: ").append(booking.getNumberOfTickets()).append("\n");
            invoice.append("Rate: ₹").append(booking.getEvent().getPrice()).append("\n");
            invoice.append("Amount: ₹").append(booking.getTotalAmount().subtract(booking.getServiceFee())).append("\n");
            invoice.append("Service Fee: ₹").append(booking.getServiceFee()).append("\n");
            invoice.append("TOTAL: ₹").append(booking.getTotalAmount()).append("\n");
            
            return invoice.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new Exception("Failed to generate invoice: " + e.getMessage(), e);
        }
    }
}