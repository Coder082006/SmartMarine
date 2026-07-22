package com.example.myapplication;

import android.content.Intent;
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
import androidx.cardview.widget.CardView;

import java.util.List;

// Shows the boats that match the route the user searched for.
// The boats now come from the SQLite "boats" table (not hardcoded).
// The screen design is unchanged: we simply fill the three existing
// cards with real data and hide any card we don't have a boat for.
public class activity_available extends AppCompatActivity {

    ImageButton btnBack;
    Button btnCheckWeather;
    Button btnViewMap;
    TextView tvRouteHeader;

    // The three fixed cards from the layout.
    CardView cardAzam, cardKilimanjaro, cardSeaStar;
    TextView tvNoBoats;
    TextView boatName1, depLabel1, arrLabel1, priceLabel1;
    TextView boatName2, depLabel2, arrLabel2, priceLabel2;
    TextView boatName3, depLabel3, arrLabel3, priceLabel3;
    Button btnSelectAzam, btnSelectKilimanjaro, btnSelectSeaStar;

    DatabaseHelper databaseHelper;
    SessionManager session;

    String from;
    String to;
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_available);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainAvailable), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        btnCheckWeather = findViewById(R.id.btnCheckWeather);
        btnViewMap = findViewById(R.id.btnViewMap);
        tvRouteHeader = findViewById(R.id.tvRouteHeader);

        cardAzam = findViewById(R.id.cardAzam);
        cardKilimanjaro = findViewById(R.id.cardKilimanjaro);
        cardSeaStar = findViewById(R.id.cardSeaStar);
        tvNoBoats = findViewById(R.id.tvNoBoats);

        boatName1 = findViewById(R.id.boatName1);
        depLabel1 = findViewById(R.id.depLabel1);
        arrLabel1 = findViewById(R.id.arrLabel1);
        priceLabel1 = findViewById(R.id.priceLabel1);
        btnSelectAzam = findViewById(R.id.btnSelectAzam);

        boatName2 = findViewById(R.id.boatName2);
        depLabel2 = findViewById(R.id.depLabel2);
        arrLabel2 = findViewById(R.id.arrLabel2);
        priceLabel2 = findViewById(R.id.priceLabel2);
        btnSelectKilimanjaro = findViewById(R.id.btnSelectKilimanjaro);

        boatName3 = findViewById(R.id.boatName3);
        depLabel3 = findViewById(R.id.depLabel3);
        arrLabel3 = findViewById(R.id.arrLabel3);
        priceLabel3 = findViewById(R.id.priceLabel3);
        btnSelectSeaStar = findViewById(R.id.btnSelectSeaStar);

        from = getIntent().getStringExtra("from");
        to = getIntent().getStringExtra("to");
        date = getIntent().getStringExtra("date");

        if (from != null && to != null && date != null) {
            tvRouteHeader.setText(from + " → " + to + "  |  " + date);
        }

        btnBack.setOnClickListener(v -> finish());

        loadBoats();

        btnCheckWeather.setOnClickListener(v -> openWeather());

        btnViewMap.setOnClickListener(v -> openMap());
    }

    // Open the full Weather page for the departure port.
    private void openWeather() {
        String city = (from != null && !from.isEmpty()) ? from : "Dar es Salaam";
        Intent intent = new Intent(this, WeatherActivity.class);
        intent.putExtra("city", city);
        startActivity(intent);
    }

    // Open the Map page showing the route between the two ports.
    private void openMap() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("from", from);
        intent.putExtra("to", to);
        startActivity(intent);
    }

    // Load boats for this route from the CLOUD REST API (live schedules).
    // The cloud is the single source of truth: if it returns nothing we tell
    // the user "No boats available" rather than showing offline seed data.
    // If the cloud can't be reached we say so and offer to retry.
    private void loadBoats() {
        if (ApiClient.isConfigured()) {
            showLoading();
            ApiClient.searchBoats(this, from, to, new ApiClient.BoatsCallback() {
                @Override
                public void onSuccess(List<Boat> boats) {
                    showBoats(boats);
                }

                @Override
                public void onError(String message) {
                    // Could not reach the cloud (no internet, or the free
                    // Render server is waking up). We do NOT fall back to
                    // local seed data — the cloud is the source of truth.
                    showMessage("Couldn't reach the schedule server.\n"
                            + "Check your internet and tap the boat search again.");
                    Toast.makeText(activity_available.this,
                            "Cloud unavailable — please try again",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // No cloud URL configured -> use the local SQLite schedules.
            showBoats(databaseHelper.searchBoats(from, to));
        }
    }

    // Temporary "loading" state while the cloud request is in flight.
    private void showLoading() {
        cardAzam.setVisibility(View.GONE);
        cardKilimanjaro.setVisibility(View.GONE);
        cardSeaStar.setVisibility(View.GONE);
        tvNoBoats.setVisibility(View.VISIBLE);
        tvNoBoats.setText("Loading live schedules…");
    }

    // Show a centred message (no boats / network error) and hide all cards.
    private void showMessage(String message) {
        cardAzam.setVisibility(View.GONE);
        cardKilimanjaro.setVisibility(View.GONE);
        cardSeaStar.setVisibility(View.GONE);
        tvNoBoats.setVisibility(View.VISIBLE);
        tvNoBoats.setText(message);
    }

    // Fill the three existing cards with whatever list we were given.
    private void showBoats(List<Boat> boats) {
        // Start by hiding all three cards, then reveal only the ones we fill.
        cardAzam.setVisibility(View.GONE);
        cardKilimanjaro.setVisibility(View.GONE);
        cardSeaStar.setVisibility(View.GONE);

        if (boats == null || boats.isEmpty()) {
            showMessage("No boats available for " + from + " → " + to);
            return;
        }

        // We have boats -> hide the empty-state message.
        tvNoBoats.setVisibility(View.GONE);

        if (boats.size() >= 1) {
            bindCard(boats.get(0), cardAzam, boatName1, depLabel1, arrLabel1, priceLabel1, btnSelectAzam);
        }
        if (boats.size() >= 2) {
            bindCard(boats.get(1), cardKilimanjaro, boatName2, depLabel2, arrLabel2, priceLabel2, btnSelectKilimanjaro);
        }
        if (boats.size() >= 3) {
            bindCard(boats.get(2), cardSeaStar, boatName3, depLabel3, arrLabel3, priceLabel3, btnSelectSeaStar);
        }
    }

    // Fill one card with a boat's details and wire its SELECT button.
    private void bindCard(Boat boat, CardView card, TextView name, TextView dep,
                          TextView arr, TextView price, Button select) {
        card.setVisibility(View.VISIBLE);
        name.setText(boat.name);
        dep.setText("Dep: " + boat.departureTime);
        arr.setText("Arr: " + boat.arrivalTime);
        price.setText(boat.getPriceText());
        select.setOnClickListener(v -> openPayment(boat));
    }

    // When a boat is selected we first send the user to the Payment screen.
    // Only after payment succeeds does Payment open the ticket (in "new"
    // mode) which saves the CONFIRMED booking and generates a reference.
    private void openPayment(Boat boat) {
        String passenger = databaseHelper.getUserName(session.getEmail());
        if (passenger == null || passenger.isEmpty()) {
            passenger = "Guest";
        }

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("mode", "new");
        intent.putExtra("passenger", passenger);
        intent.putExtra("origin", boat.origin);
        intent.putExtra("destination", boat.destination);
        intent.putExtra("date", date != null ? date : "");
        intent.putExtra("departure", boat.departureTime);
        intent.putExtra("boat", boat.name);
        intent.putExtra("price", boat.price);
        startActivity(intent);
    }
}
