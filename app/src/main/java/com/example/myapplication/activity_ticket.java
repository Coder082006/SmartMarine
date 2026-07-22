package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Shows a digital ticket. It works in two modes:
//   "new"  -> the user just selected a boat, so we SAVE the booking to
//             SQLite and show the generated reference number.
//   "view" -> the user opened an existing booking from My Bookings, so
//             we just display the details that were passed in.
public class activity_ticket extends AppCompatActivity {

    ImageButton btnBack;
    Button btnDone;
    TextView tvPassenger;
    TextView tvRoute;
    TextView tvDate;
    TextView tvDeparture;
    TextView tvBoat;
    TextView refNumber;
    TextView badgeConfirmed;

    DatabaseHelper databaseHelper;
    SessionManager session;

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
        tvPassenger = findViewById(R.id.tvPassenger);
        tvRoute = findViewById(R.id.tvRoute);
        tvDate = findViewById(R.id.tvDate);
        tvDeparture = findViewById(R.id.tvDeparture);
        tvBoat = findViewById(R.id.tvBoat);
        refNumber = findViewById(R.id.refNumber);
        badgeConfirmed = findViewById(R.id.badgeConfirmed);

        String mode = getIntent().getStringExtra("mode");
        String passenger = getIntent().getStringExtra("passenger");
        String origin = getIntent().getStringExtra("origin");
        String destination = getIntent().getStringExtra("destination");
        String date = getIntent().getStringExtra("date");
        String departure = getIntent().getStringExtra("departure");
        String boat = getIntent().getStringExtra("boat");
        int price = getIntent().getIntExtra("price", 0);

        // Build the route text ("A → B"). For "view" mode the caller may
        // instead pass a ready-made "route" string.
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
            // Save this booking to the database and show its reference.
            String reference = databaseHelper.createBooking(
                    session.getEmail(), passenger, boat,
                    origin, destination, date, departure, price);

            if (reference != null) {
                refNumber.setText("Ref: " + reference);
                badgeConfirmed.setText("CONFIRMED");
                Toast.makeText(this, "Booking confirmed! Ref: " + reference,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Could not save booking. Please try again.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            // Viewing an existing ticket — just show what was passed in.
            String reference = getIntent().getStringExtra("reference");
            String status = getIntent().getStringExtra("status");
            if (reference != null) refNumber.setText("Ref: " + reference);
            if (status != null) badgeConfirmed.setText(status);
        }

        btnBack.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> finish());
    }
}