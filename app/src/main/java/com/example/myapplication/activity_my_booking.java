package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

import java.util.List;

// Shows the logged-in user's bookings. The data now comes from the
// SQLite "bookings" table instead of being hardcoded. The design is
// unchanged: we fill the three existing cards and hide any extra ones.
public class activity_my_booking extends AppCompatActivity {

    ImageButton btnBack;

    CardView card1, card2, card3;
    TextView route1, dateInfo1, badge1;
    TextView route2, dateInfo2, badge2;
    TextView route3, dateInfo3, badge3;
    Button btnViewTicket1, btnViewTicket2, btnViewTicket3;

    DatabaseHelper databaseHelper;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);

        card1 = findViewById(R.id.card1);
        route1 = findViewById(R.id.route1);
        dateInfo1 = findViewById(R.id.dateInfo1);
        badge1 = findViewById(R.id.badge1);
        btnViewTicket1 = findViewById(R.id.btnViewTicket1);

        card2 = findViewById(R.id.card2);
        route2 = findViewById(R.id.route2);
        dateInfo2 = findViewById(R.id.dateInfo2);
        badge2 = findViewById(R.id.badge2);
        btnViewTicket2 = findViewById(R.id.btnViewTicket2);

        card3 = findViewById(R.id.card3);
        route3 = findViewById(R.id.route3);
        dateInfo3 = findViewById(R.id.dateInfo3);
        badge3 = findViewById(R.id.badge3);
        btnViewTicket3 = findViewById(R.id.btnViewTicket3);

        btnBack.setOnClickListener(v -> finish());

        loadBookings();
    }

    // Reload every time the screen comes back to the front so a newly
    // made booking shows up immediately.
    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }

    private void loadBookings() {
        List<Booking> bookings = databaseHelper.getBookingsForUser(session.getEmail());

        card1.setVisibility(View.GONE);
        card2.setVisibility(View.GONE);
        card3.setVisibility(View.GONE);

        if (bookings.size() >= 1) {
            bindCard(bookings.get(0), card1, route1, dateInfo1, badge1, btnViewTicket1);
        }
        if (bookings.size() >= 2) {
            bindCard(bookings.get(1), card2, route2, dateInfo2, badge2, btnViewTicket2);
        }
        if (bookings.size() >= 3) {
            bindCard(bookings.get(2), card3, route3, dateInfo3, badge3, btnViewTicket3);
        }
    }

    private void bindCard(Booking booking, CardView card, TextView route,
                          TextView detail, TextView badge, Button viewTicket) {
        card.setVisibility(View.VISIBLE);
        route.setText(booking.getRoute());
        detail.setText(booking.getDetail());
        badge.setText(booking.status);

        // Green badge for CONFIRMED, grey for anything else.
        if ("CONFIRMED".equalsIgnoreCase(booking.status)) {
            badge.setBackgroundResource(R.drawable.badge_green);
            badge.setTextColor(0xFF1B8A3E);
        } else {
            badge.setBackgroundResource(R.drawable.badge_grey);
            badge.setTextColor(0xFF777777);
        }

        viewTicket.setOnClickListener(v -> openTicket(booking));
    }

    // Open the ticket screen in "view" mode so it just displays the
    // saved booking without creating a new one.
    private void openTicket(Booking booking) {
        Intent intent = new Intent(this, activity_ticket.class);
        intent.putExtra("mode", "view");
        intent.putExtra("passenger", booking.passengerName);
        intent.putExtra("route", booking.getRoute());
        intent.putExtra("origin", booking.origin);
        intent.putExtra("destination", booking.destination);
        intent.putExtra("date", booking.travelDate);
        intent.putExtra("departure", booking.departureTime);
        intent.putExtra("boat", booking.boatName);
        intent.putExtra("price", booking.price);
        intent.putExtra("reference", booking.reference);
        intent.putExtra("status", booking.status);
        startActivity(intent);
    }
}