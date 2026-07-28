package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;

public class activity_ticket extends AppCompatActivity {

    ImageButton btnBack;
    Button btnDone;
    Button btnSendTicket;
    TextView tvPassenger;
    TextView tvRoute;
    TextView tvDate;
    TextView tvDeparture;
    TextView tvBoat;
    TextView refNumber;
    TextView badgeConfirmed;
    ImageView imgQrCode;

    DatabaseHelper databaseHelper;
    SessionManager session;

    private final ActivityResultLauncher<String> notificationPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Permission result — notification already fired, this is best-effort
            });

    private String currentReference;
    private String currentPassenger;
    private String currentRoute;
    private String currentDate;
    private String currentDeparture;
    private String currentBoat;
    private int currentPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ticket);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainTicket), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        btnDone = findViewById(R.id.btnDone);
        btnSendTicket = findViewById(R.id.btnSendTicket);
        tvPassenger = findViewById(R.id.tvPassenger);
        tvRoute = findViewById(R.id.tvRoute);
        tvDate = findViewById(R.id.tvDate);
        tvDeparture = findViewById(R.id.tvDeparture);
        tvBoat = findViewById(R.id.tvBoat);
        refNumber = findViewById(R.id.refNumber);
        badgeConfirmed = findViewById(R.id.badgeConfirmed);
        imgQrCode = findViewById(R.id.imgQrCode);

        String mode = getIntent().getStringExtra("mode");
        String passenger = getIntent().getStringExtra("passenger");
        String origin = getIntent().getStringExtra("origin");
        String destination = getIntent().getStringExtra("destination");
        String date = getIntent().getStringExtra("date");
        String departure = getIntent().getStringExtra("departure");
        String boat = getIntent().getStringExtra("boat");
        int price = getIntent().getIntExtra("price", 0);

        String route = getIntent().getStringExtra("route");
        if (route == null && origin != null && destination != null) {
            route = origin + " → " + destination;
        }

        if (passenger != null) tvPassenger.setText(passenger);
        if (route != null) tvRoute.setText(route);
        if (date != null) tvDate.setText(date);
        if (departure != null) tvDeparture.setText(departure);
        if (boat != null) tvBoat.setText(boat);

        if ("new".equals(mode)) {
            String reference = databaseHelper.createBooking(
                    session.getEmail(), passenger, boat,
                    origin, destination, date, departure, price);

            if (reference != null) {
                currentReference = reference;
                currentPassenger = passenger;
                currentRoute = route;
                currentDate = date;
                currentDeparture = departure;
                currentBoat = boat;
                currentPrice = price;

                refNumber.setText("Ref: " + reference);
                badgeConfirmed.setText("CONFIRMED");
                Toast.makeText(this, "Booking confirmed! Ref: " + reference,
                        Toast.LENGTH_LONG).show();

                generateQRCode(reference, passenger, route, date, departure, boat, price);
                sendTicketEmail();
                showBookingNotification(reference, route, date);
            } else {
                Toast.makeText(this, "Could not save booking. Please try again.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            currentReference = getIntent().getStringExtra("reference");
            currentPassenger = passenger;
            currentRoute = route;
            currentDate = date;
            currentDeparture = departure;
            currentBoat = boat;
            currentPrice = price;

            String status = getIntent().getStringExtra("status");
            if (currentReference != null) refNumber.setText("Ref: " + currentReference);
            if (status != null) badgeConfirmed.setText(status);

            generateQRCode(currentReference, passenger, route, date, departure, boat, price);
        }

        btnBack.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> finish());

        btnSendTicket.setOnClickListener(v -> sendTicketEmail());
    }

    private void generateQRCode(String reference, String passenger, String route,
                                 String date, String departure, String boat, int price) {
        if (reference == null) return;

        String qrContent = QRCodeGenerator.buildTicketContent(
                reference, passenger != null ? passenger : "",
                route != null ? route : "",
                date != null ? date : "",
                departure != null ? departure : "",
                boat != null ? boat : "",
                price);

        Bitmap qrBitmap = QRCodeGenerator.generate(qrContent, 600);
        if (qrBitmap != null) {
            imgQrCode.setImageBitmap(qrBitmap);
        }
    }

    private void sendTicketEmail() {
        if (currentReference == null) {
            Toast.makeText(this, "No ticket to send", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = session.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "No email found for this account",
                    Toast.LENGTH_SHORT).show();
            // No login email to deliver to — offer a retry.
            btnSendTicket.setVisibility(View.VISIBLE);
            return;
        }

        try {
            File pdfFile = TicketPDFGenerator.generate(
                    this, currentReference, currentPassenger,
                    currentRoute, currentDate, currentDeparture,
                    currentBoat, currentPrice);

            String subject = "Your Ticket - " + currentReference;
            String body = "Dear " + currentPassenger + ",\n\n"
                    + "Your booking has been confirmed!\n\n"
                    + "Reference: " + currentReference + "\n"
                    + "Route: " + currentRoute + "\n"
                    + "Date: " + currentDate + "\n"
                    + "Departure: " + currentDeparture + "\n"
                    + "Boat: " + currentBoat + "\n\n"
                    + "Please find your ticket attached as a PDF.\n"
                    + "Show the QR code at the boarding point.\n\n"
                    + "Thank you for choosing Smart Marine Booking!";

            EmailService emailService = new EmailService(
                    this, EmailConfig.SENDER_EMAIL, EmailConfig.SENDER_PASSWORD);
            emailService.sendTicketEmail(userEmail, subject, body, pdfFile,
                    (success, error) ->
                            // Show the Resend button only if delivery to the
                            // login email failed; hide it once it succeeds.
                            btnSendTicket.setVisibility(success ? View.GONE : View.VISIBLE));

            Toast.makeText(this, "Sending ticket to " + userEmail + "...",
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(this, "Error generating ticket PDF",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            // PDF could not be built, so nothing was emailed — offer a retry.
            btnSendTicket.setVisibility(View.VISIBLE);
        }
    }

    private void showBookingNotification(String reference, String route, String date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        NotificationHelper.showBookingConfirmed(this, reference, route, date);
    }
}
