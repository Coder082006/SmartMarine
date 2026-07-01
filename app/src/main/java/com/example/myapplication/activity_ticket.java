package com.example.myapplication;

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

public class activity_ticket extends AppCompatActivity {

    ImageButton btnBack;
    Button btnDone;
    TextView tvPassenger;
    TextView tvRoute;
    TextView tvDate;
    TextView tvDeparture;
    TextView tvBoat;

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

        btnBack = findViewById(R.id.btnBack);
        btnDone = findViewById(R.id.btnDone);
        tvPassenger = findViewById(R.id.tvPassenger);
        tvRoute = findViewById(R.id.tvRoute);
        tvDate = findViewById(R.id.tvDate);
        tvDeparture = findViewById(R.id.tvDeparture);
        tvBoat = findViewById(R.id.tvBoat);

        String passenger = getIntent().getStringExtra("passenger");
        String route = getIntent().getStringExtra("route");
        String date = getIntent().getStringExtra("date");
        String departure = getIntent().getStringExtra("departure");
        String boat = getIntent().getStringExtra("boat");

        if (passenger != null) tvPassenger.setText(passenger);
        if (route != null) tvRoute.setText(route);
        if (date != null) tvDate.setText(date);
        if (departure != null) tvDeparture.setText(departure);
        if (boat != null) tvBoat.setText(boat);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
