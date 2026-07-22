package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {

    Button buttonLogout, buttonSearchBoats, buttonViewBookings;

    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainHome), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);

        buttonLogout = findViewById(R.id.btnLogout);
        buttonSearchBoats = findViewById(R.id.btnSearchBoats);
        buttonViewBookings = findViewById(R.id.btnViewBookings);

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the session so the next user starts fresh.
                session.logout();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonSearchBoats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, activity_search_boat.class);
                startActivity(intent);
            }
        });

        buttonViewBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, activity_my_booking.class);
                startActivity(intent);
            }
        });

        loadWeather();
    }

    // Fetch live weather for the main port and update the existing weather
    // card. If no API key is set we simply keep the default text.
    private void loadWeather() {
        TextView weatherTitle = findViewById(R.id.weatherTitle);
        TextView weatherSubtitle = findViewById(R.id.weatherSubtitle);

        if (!WeatherService.hasApiKey()) {
            return;
        }

        WeatherService.fetchByCity(this, "Dar es Salaam", new WeatherService.WeatherCallback() {
            @Override
            public void onSuccess(double tempCelsius, String condition, boolean goodForTravel) {
                weatherTitle.setText(String.format(Locale.US,
                        "Today's Weather: %.0f°C, %s", tempCelsius, condition));
                weatherSubtitle.setText(goodForTravel
                        ? "Good conditions for travel"
                        : "Caution: check conditions before travel");
            }

            @Override
            public void onError(String message) {
                // Keep the default text on failure.
            }
        });
    }

}
