package com.eventhub.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class QrCodeUtil {
    
    /**
     * Generate QR code as byte array (simplified version without external dependencies)
     * @param data The data to encode in QR code
     * @return QR code as byte array (currently returns the data as text)
     * @throws Exception if QR code generation fails
     */
    public byte[] generateQrCode(String data) throws Exception {
        try {
            // For now, return a simple text representation
            // In production, you would use a QR code library like ZXing
            String qrText = "QR CODE DATA:\n" + data + "\n\n[QR Code would be displayed here in actual implementation]";
            return qrText.getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new Exception("Failed to generate QR code: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate QR code with custom size (simplified version)
     * @param data The data to encode in QR code
     * @param size The size of the QR code (width and height)
     * @return QR code as byte array
     * @throws Exception if QR code generation fails
     */
    public byte[] generateQrCode(String data, int size) throws Exception {
        // For now, ignore the size parameter and use the basic method
        return generateQrCode(data);
    }
    
    /**
     * Generate QR code with logo in center (simplified version)
     * @param data The data to encode in QR code
     * @param logoBytes The logo image as byte array
     * @return QR code with logo as byte array
     * @throws Exception if QR code generation fails
     */
    public byte[] generateQrCodeWithLogo(String data, byte[] logoBytes) throws Exception {
        // For now, ignore the logo and use the basic method
        return generateQrCode(data);
    }
    
    /**
     * Validate QR code data format
     * @param data The data to validate
     * @return true if data is valid for QR code generation
     */
    public boolean isValidQrData(String data) {
        return data != null && !data.trim().isEmpty() && data.length() <= 4296; // QR code max capacity
    }
}