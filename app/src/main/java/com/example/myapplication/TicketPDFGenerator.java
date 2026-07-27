package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TicketPDFGenerator {

    public static File generate(Context context, String reference, String passenger,
                                 String route, String date, String departure,
                                 String boat, int price) throws IOException {
        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();

        // Header background
        paint.setColor(Color.parseColor("#1A237E"));
        canvas.drawRect(0, 0, 595, 100, paint);

        // Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setFakeBoldText(true);
        canvas.drawText("SMART MARINE BOOKING", 40, 45, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("Digital Ticket", 40, 70, paint);

        // Reference
        paint.setTextSize(11);
        canvas.drawText("Ref: " + reference, 40, 90, paint);

        // Divider
        paint.setColor(Color.parseColor("#E0E0E0"));
        canvas.drawRect(40, 115, 555, 116, paint);

        // Passenger
        paint.setColor(Color.parseColor("#AAAAAA"));
        paint.setTextSize(12);
        canvas.drawText("PASSENGER", 40, 145, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(passenger, 40, 168, paint);

        // Divider
        paint.setColor(Color.parseColor("#E0E0E0"));
        canvas.drawRect(40, 185, 555, 186, paint);

        // Route
        paint.setColor(Color.parseColor("#AAAAAA"));
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("ROUTE", 40, 215, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(route, 40, 238, paint);

        // Divider
        paint.setColor(Color.parseColor("#E0E0E0"));
        canvas.drawRect(40, 255, 555, 256, paint);

        // Date
        paint.setColor(Color.parseColor("#AAAAAA"));
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("DATE", 40, 285, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(date, 40, 308, paint);

        // Divider
        paint.setColor(Color.parseColor("#E0E0E0"));
        canvas.drawRect(40, 325, 555, 326, paint);

        // Departure
        paint.setColor(Color.parseColor("#AAAAAA"));
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("DEPARTURE", 40, 355, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(departure, 40, 378, paint);

        // Divider
        paint.setColor(Color.parseColor("#E0E0E0"));
        canvas.drawRect(40, 395, 555, 396, paint);

        // Boat
        paint.setColor(Color.parseColor("#AAAAAA"));
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("BOAT", 40, 425, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(boat, 40, 448, paint);

        // Divider
        paint.setColor(Color.parseColor("#E0E0E0"));
        canvas.drawRect(40, 465, 555, 466, paint);

        // Price
        paint.setColor(Color.parseColor("#AAAAAA"));
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("PRICE", 40, 495, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("TZS " + String.format("%,d", price), 40, 520, paint);

        // Divider
        paint.setColor(Color.parseColor("#E0E0E0"));
        canvas.drawRect(40, 540, 555, 541, paint);

        // QR Code
        String qrContent = QRCodeGenerator.buildTicketContent(
                reference, passenger, route, date, departure, boat, price);
        android.graphics.Bitmap qrBitmap = QRCodeGenerator.generate(qrContent, 250);
        if (qrBitmap != null) {
            canvas.drawBitmap(qrBitmap, null,
                    new android.graphics.Rect(198, 560, 397, 759), null);
        }

        // Footer
        paint.setColor(Color.parseColor("#AAAAAA"));
        paint.setTextSize(10);
        paint.setFakeBoldText(false);
        canvas.drawText("Show this QR code at the boarding point.", 40, 790, paint);
        canvas.drawText("Status: CONFIRMED", 40, 805, paint);

        document.finishPage(page);

        File outputDir = context.getFilesDir();
        File outputFile = new File(outputDir, reference + "_ticket.pdf");
        FileOutputStream fos = new FileOutputStream(outputFile);
        document.writeTo(fos);
        fos.close();
        document.close();

        return outputFile;
    }
}
