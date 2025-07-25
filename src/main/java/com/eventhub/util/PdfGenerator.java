package com.eventhub.util;

import com.eventhub.model.entity.Booking;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Component
public class PdfGenerator {
    
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLUE);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    
    public byte[] generateTicketPdf(Booking booking) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        PdfWriter.getInstance(document, out);
        document.open();
        
        // Header
        addHeader(document);
        
        // Ticket Information
        addTicketInfo(document, booking);
        
        // Event Details
        addEventDetails(document, booking);
        
        // Booking Details
        addBookingDetails(document, booking);
        
        // Important Information
        addImportantInfo(document);
        
        // Footer
        addFooter(document);
        
        document.close();
        return out.toByteArray();
    }
    
    private void addHeader(Document document) throws DocumentException {
        Paragraph header = new Paragraph("🎫 EventHub", TITLE_FONT);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(20);
        document.add(header);
        
        // Add a line
        Paragraph line = new Paragraph("_".repeat(60), NORMAL_FONT);
        line.setAlignment(Element.ALIGN_CENTER);
        document.add(line);
        document.add(Chunk.NEWLINE);
    }
    
    private void addTicketInfo(Document document, Booking booking) throws DocumentException {
        Paragraph ticketTitle = new Paragraph("EVENT TICKET", HEADER_FONT);
        ticketTitle.setAlignment(Element.ALIGN_CENTER);
        ticketTitle.setSpacingAfter(10);
        document.add(ticketTitle);
        
        Paragraph ticketId = new Paragraph("Ticket ID: " + booking.getTicketId(), NORMAL_FONT);
        ticketId.setAlignment(Element.ALIGN_CENTER);
        ticketId.setSpacingAfter(20);
        document.add(ticketId);
    }
    
    private void addEventDetails(Document document, Booking booking) throws DocumentException {
        Paragraph eventHeader = new Paragraph("EVENT DETAILS", HEADER_FONT);
        eventHeader.setSpacingAfter(10);
        document.add(eventHeader);
        
        PdfPTable eventTable = new PdfPTable(2);
        eventTable.setWidthPercentage(100);
        eventTable.setSpacingAfter(20);
        
        addTableRow(eventTable, "Event:", booking.getEvent().getTitle());
        addTableRow(eventTable, "Date:", booking.getEvent().getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        addTableRow(eventTable, "Time:", booking.getEvent().getTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
        addTableRow(eventTable, "Location:", booking.getEvent().getLocation());
        addTableRow(eventTable, "Category:", booking.getEvent().getCategory().toString());
        
        document.add(eventTable);
    }
    
    private void addBookingDetails(Document document, Booking booking) throws DocumentException {
        Paragraph bookingHeader = new Paragraph("BOOKING DETAILS", HEADER_FONT);
        bookingHeader.setSpacingAfter(10);
        document.add(bookingHeader);
        
        PdfPTable bookingTable = new PdfPTable(2);
        bookingTable.setWidthPercentage(100);
        bookingTable.setSpacingAfter(20);
        
        addTableRow(bookingTable, "Booked By:", booking.getUser().getName());
        addTableRow(bookingTable, "Email:", booking.getUser().getEmail());
        addTableRow(bookingTable, "Number of Tickets:", booking.getNumberOfTickets().toString());
        addTableRow(bookingTable, "Total Amount:", "₹" + booking.getTotalAmount());
        addTableRow(bookingTable, "Service Fee:", "₹" + booking.getServiceFee());
        addTableRow(bookingTable, "Booking Date:", booking.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
        addTableRow(bookingTable, "Status:", booking.getStatus().toString());
        
        document.add(bookingTable);
    }
    
    private void addImportantInfo(Document document) throws DocumentException {
        Paragraph importantHeader = new Paragraph("IMPORTANT INFORMATION", HEADER_FONT);
        importantHeader.setSpacingAfter(10);
        document.add(importantHeader);
        
        String[] importantPoints = {
            "• Present this ticket at the venue entrance",
            "• Arrive 30 minutes before event start time",
            "• This ticket is non-transferable and non-refundable",
            "• Keep this ticket safe and accessible",
            "• Contact support for issues: support@eventhub.com"
        };
        
        for (String point : importantPoints) {
            Paragraph p = new Paragraph(point, SMALL_FONT);
            p.setSpacingAfter(5);
            document.add(p);
        }
        
        document.add(Chunk.NEWLINE);
    }
    
    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph("🌐 EventHub - Your Gateway to Amazing Experiences", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
        
        Paragraph contact = new Paragraph("📧 support@eventhub.com | 🌍 www.eventhub.com", SMALL_FONT);
        contact.setAlignment(Element.ALIGN_CENTER);
        document.add(contact);
        
        Paragraph generated = new Paragraph("Generated: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")), SMALL_FONT);
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingBefore(10);
        document.add(generated);
    }
    
    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(8);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8);
        table.addCell(valueCell);
    }
}