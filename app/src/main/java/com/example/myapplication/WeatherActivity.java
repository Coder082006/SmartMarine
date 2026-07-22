package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

// Full-screen Weather page. Opened from the "Check Weather" button on the
// Available Boats screen. It fetches LIVE weather for the departure port
// from OpenWeatherMap (via WeatherService/Volley) and tells the user
// whether conditions are good for travelling by boat.
public class WeatherActivity extends AppCompatActivity {

    TextView tvCity, tvTemp, tvCondition, tvAdvice, tvIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainWeather), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton btnBack = findViewById(R.id.btnBack);
        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvCondition = findViewById(R.id.tvCondition);
        tvAdvice = findViewById(R.id.tvAdvice);
        tvIcon = findViewById(R.id.tvIcon);

        btnBack.setOnClickListener(v -> finish());

        String city = getIntent().getStringExtra("city");
        if (city == null || city.isEmpty()) {
            city = "Dar es Salaam";
        }
        tvCity.setText(city);

        loadWeather(city);
    }

    private void loadWeather(String city) {
        if (!WeatherService.hasApiKey()) {
            tvCondition.setText("Weather service not configured");
            showAdvice("Add an OpenWeatherMap API key to see live weather", false);
            return;
        }

        tvCondition.setText("Loading current conditions…");

        WeatherService.fetchByCity(this, city, new WeatherService.WeatherCallback() {
            @Override
            public void onSuccess(double tempCelsius, String condition, boolean goodForTravel) {
                tvTemp.setText(String.format(Locale.US, "%.0f°C", tempCelsius));
                tvCondition.setText(condition);
                tvIcon.setText(iconFor(condition));

                if (goodForTravel) {
                    showAdvice("✅ Good conditions for travel", true);
                } else {
                    showAdvice("⚠️ Rough conditions — check before you travel", false);
                }
            }

            @Override
            public void onError(String message) {
                tvCondition.setText("Could not load weather");
                showAdvice("Please check your internet connection and try again", false);
            }
        });
    }

    // Colour the advice banner green (good) or orange (caution).
    private void showAdvice(String text, boolean good) {
        tvAdvice.setText(text);
        if (good) {
            tvAdvice.setBackgroundColor(Color.parseColor("#E8F5E9"));
            tvAdvice.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            tvAdvice.setBackgroundColor(Color.parseColor("#FFF3E0"));
            tvAdvice.setTextColor(Color.parseColor("#E65100"));
        }
    }

    // Pick a simple emoji for the reported condition.
    private String iconFor(String condition) {
        String c = condition == null ? "" : condition.toLowerCase(Locale.US);
        if (c.contains("thunder") || c.contains("storm")) return "⛈️";
        if (c.contains("rain") || c.contains("drizzle")) return "🌧️";
        if (c.contains("cloud")) return "☁️";
        if (c.contains("clear")) return "☀️";
        if (c.contains("mist") || c.contains("fog") || c.contains("haze")) return "🌫️";
        return "🌤️";
    }
}