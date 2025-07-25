package com.eventhub.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class QrCodeUtil {
    
    public byte[] generateQRCode(String data, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        return outputStream.toByteArray();
    }
    
    public byte[] generateTicketQRCode(String ticketId) throws WriterException, IOException {
        String qrData = String.format("EVENTHUB_TICKET:%s", ticketId);
        return generateQRCode(qrData, 200, 200);
    }
    
    public String generateQRCodeDataURL(String data, int width, int height) throws WriterException, IOException {
        byte[] qrCodeBytes = generateQRCode(data, width, height);
        String base64 = java.util.Base64.getEncoder().encodeToString(qrCodeBytes);
        return "data:image/png;base64," + base64;
    }
}