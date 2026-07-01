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

public class activity_available extends AppCompatActivity {

    ImageButton btnBack;
    Button btnSelectAzam;
    Button btnSelectKilimanjaro;
    Button btnSelectSeaStar;
    Button btnCheckWeather;
    Button btnViewMap;
    TextView tvRouteHeader;

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

        btnBack = findViewById(R.id.btnBack);
        btnSelectAzam = findViewById(R.id.btnSelectAzam);
        btnSelectKilimanjaro = findViewById(R.id.btnSelectKilimanjaro);
        btnSelectSeaStar = findViewById(R.id.btnSelectSeaStar);
        btnCheckWeather = findViewById(R.id.btnCheckWeather);
        btnViewMap = findViewById(R.id.btnViewMap);
        tvRouteHeader = findViewById(R.id.tvRouteHeader);

        String from = getIntent().getStringExtra("from");
        String to = getIntent().getStringExtra("to");
        String date = getIntent().getStringExtra("date");

        if (from != null && to != null && date != null) {
            tvRouteHeader.setText(
                    from + " -> " + to + "  |  " + date);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSelectAzam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        activity_available.this,
                        activity_ticket.class);
                intent.putExtra("passenger", "John Doe");
                intent.putExtra("route",
                        from + " -> " + to);
                intent.putExtra("date",
                        date != null ? date : "22 May 2026");
                intent.putExtra("departure", "08:00AM");
                intent.putExtra("boat", "Azam Marine Ferry");
                intent.putExtra("reference", "SMB-2026-0042");
                intent.putExtra("status", "CONFIRMED");
                startActivity(intent);
            }
        });

        btnSelectKilimanjaro.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        activity_available.this,
                        activity_ticket.class);
                intent.putExtra("passenger", "John Doe");
                intent.putExtra("route",
                        from + " -> " + to);
                intent.putExtra("date",
                        date != null ? date : "22 May 2026");
                intent.putExtra("departure", "10:30AM");
                intent.putExtra("boat",
                        "Kilimanjaro Fast Ferry");
                intent.putExtra("reference", "SMB-2026-0043");
                intent.putExtra("status", "CONFIRMED");
                startActivity(intent);
            }
        });

        btnSelectSeaStar.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        activity_available.this,
                        activity_ticket.class);
                intent.putExtra("passenger", "John Doe");
                intent.putExtra("route",
                        from + " -> " + to);
                intent.putExtra("date",
                        date != null ? date : "22 May 2026");
                intent.putExtra("departure", "02:00PM");
                intent.putExtra("boat", "Sea Star Express");
                intent.putExtra("reference", "SMB-2026-0044");
                intent.putExtra("status", "CONFIRMED");
                startActivity(intent);
            }
        });

        btnCheckWeather.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity_available.this,
                        "Today's Weather: 28°C, Sunny"
                                + " - Good conditions for travel",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnViewMap.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity_available.this,
                        "Map feature coming soon",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
