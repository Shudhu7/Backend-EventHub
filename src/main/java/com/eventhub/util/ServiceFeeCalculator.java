package com.eventhub.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ServiceFeeCalculator {
    
    // Service fee configuration
    private static final BigDecimal SERVICE_FEE_PERCENTAGE = new BigDecimal("0.05"); // 5%
    private static final BigDecimal MINIMUM_SERVICE_FEE = new BigDecimal("10.00"); // Minimum ₹10
    private static final BigDecimal MAXIMUM_SERVICE_FEE = new BigDecimal("500.00"); // Maximum ₹500
    
    /**
     * Calculate service fee based on subtotal amount
     * @param subtotal The subtotal amount before service fee
     * @return The calculated service fee
     */
    public BigDecimal calculateServiceFee(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate percentage-based fee
        BigDecimal percentageFee = subtotal.multiply(SERVICE_FEE_PERCENTAGE);
        
        // Apply minimum and maximum limits
        if (percentageFee.compareTo(MINIMUM_SERVICE_FEE) < 0) {
            percentageFee = MINIMUM_SERVICE_FEE;
        } else if (percentageFee.compareTo(MAXIMUM_SERVICE_FEE) > 0) {
            percentageFee = MAXIMUM_SERVICE_FEE;
        }
        
        // Round to 2 decimal places
        return percentageFee.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate total amount including service fee
     * @param subtotal The subtotal amount before service fee
     * @return The total amount including service fee
     */
    public BigDecimal calculateTotalAmount(BigDecimal subtotal) {
        BigDecimal serviceFee = calculateServiceFee(subtotal);
        return subtotal.add(serviceFee);
    }
    
    /**
     * Get service fee percentage
     * @return The service fee percentage as decimal (e.g., 0.05 for 5%)
     */
    public BigDecimal getServiceFeePercentage() {
        return SERVICE_FEE_PERCENTAGE;
    }
    
    /**
     * Get minimum service fee
     * @return The minimum service fee amount
     */
    public BigDecimal getMinimumServiceFee() {
        return MINIMUM_SERVICE_FEE;
    }
    
    /**
     * Get maximum service fee
     * @return The maximum service fee amount
     */
    public BigDecimal getMaximumServiceFee() {
        return MAXIMUM_SERVICE_FEE;
    }
}