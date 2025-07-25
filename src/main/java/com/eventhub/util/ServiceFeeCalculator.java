package com.eventhub.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ServiceFeeCalculator {
    
    private static final BigDecimal SERVICE_FEE_PERCENTAGE = new BigDecimal("0.05"); // 5%
    private static final BigDecimal MINIMUM_SERVICE_FEE = new BigDecimal("10.00");
    private static final BigDecimal MAXIMUM_SERVICE_FEE = new BigDecimal("500.00");
    
    public BigDecimal calculateServiceFee(BigDecimal ticketPrice, Integer numberOfTickets) {
        BigDecimal totalTicketAmount = ticketPrice.multiply(new BigDecimal(numberOfTickets));
        BigDecimal calculatedFee = totalTicketAmount.multiply(SERVICE_FEE_PERCENTAGE);
        
        // Apply minimum and maximum limits
        if (calculatedFee.compareTo(MINIMUM_SERVICE_FEE) < 0) {
            calculatedFee = MINIMUM_SERVICE_FEE;
        } else if (calculatedFee.compareTo(MAXIMUM_SERVICE_FEE) > 0) {
            calculatedFee = MAXIMUM_SERVICE_FEE;
        }
        
        return calculatedFee.setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal calculateTotalAmount(BigDecimal ticketPrice, Integer numberOfTickets) {
        BigDecimal ticketAmount = ticketPrice.multiply(new BigDecimal(numberOfTickets));
        BigDecimal serviceFee = calculateServiceFee(ticketPrice, numberOfTickets);
        return ticketAmount.add(serviceFee).setScale(2, RoundingMode.HALF_UP);
    }
    
    public ServiceFeeBreakdown getServiceFeeBreakdown(BigDecimal ticketPrice, Integer numberOfTickets) {
        BigDecimal ticketAmount = ticketPrice.multiply(new BigDecimal(numberOfTickets));
        BigDecimal serviceFee = calculateServiceFee(ticketPrice, numberOfTickets);
        BigDecimal totalAmount = ticketAmount.add(serviceFee);
        
        return new ServiceFeeBreakdown(ticketAmount, serviceFee, totalAmount);
    }
    
    public static class ServiceFeeBreakdown {
        private final BigDecimal ticketAmount;
        private final BigDecimal serviceFee;
        private final BigDecimal totalAmount;
        
        public ServiceFeeBreakdown(BigDecimal ticketAmount, BigDecimal serviceFee, BigDecimal totalAmount) {
            this.ticketAmount = ticketAmount;
            this.serviceFee = serviceFee;
            this.totalAmount = totalAmount;
        }
        
        public BigDecimal getTicketAmount() { return ticketAmount; }
        public BigDecimal getServiceFee() { return serviceFee; }
        public BigDecimal getTotalAmount() { return totalAmount; }
    }
}