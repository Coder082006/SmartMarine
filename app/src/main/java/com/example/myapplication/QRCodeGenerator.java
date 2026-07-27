package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    public static Bitmap generate(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String buildTicketContent(String reference, String passenger,
                                             String route, String date,
                                             String departure, String boat,
                                             int price) {
        return "SMART MARINE BOOKING\n"
                + "====================\n"
                + "Reference: " + reference + "\n"
                + "Passenger: " + passenger + "\n"
                + "Route: " + route + "\n"
                + "Date: " + date + "\n"
                + "Departure: " + departure + "\n"
                + "Boat: " + boat + "\n"
                + "Price: TZS " + String.format("%,d", price) + "\n"
                + "Status: CONFIRMED\n"
                + "====================\n"
                + "Show this QR code at the boarding point.";
    }
}
