package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class activity_my_booking extends AppCompatActivity {

    ImageButton btnBack;
    Button btnViewTicket1, btnViewTicket2, btnViewTicket3;

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

        btnBack = findViewById(R.id.btnBack);
        btnViewTicket1 = findViewById(R.id.btnViewTicket1);
        btnViewTicket2 = findViewById(R.id.btnViewTicket2);
        btnViewTicket3 = findViewById(R.id.btnViewTicket3);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnViewTicket1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_my_booking.this, activity_ticket.class);
                intent.putExtra("passenger", "John Doe");
                intent.putExtra("route", "Dar es Salaam -> Zanzibar");
                intent.putExtra("date", "22 May 2026");
                intent.putExtra("departure", "08:00AM");
                intent.putExtra("boat", "Azam Marine Ferry");
                intent.putExtra("reference", "SMB-2026-0042");
                intent.putExtra("status", "CONFIRMED");
                startActivity(intent);
            }
        });

        btnViewTicket2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_my_booking.this, activity_ticket.class);
                intent.putExtra("passenger", "John Doe");
                intent.putExtra("route", "Zanzibar -> Dar es Salaam");
                intent.putExtra("date", "10 Apr 2026");
                intent.putExtra("departure", "10:30AM");
                intent.putExtra("boat", "Kilimanjaro Fast Ferry");
                intent.putExtra("reference", "SMB-2026-0043");
                intent.putExtra("status", "COMPLETED");
                startActivity(intent);
            }
        });

        btnViewTicket3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_my_booking.this, activity_ticket.class);
                intent.putExtra("passenger", "John Doe");
                intent.putExtra("route", "Dar es Salaam -> Mafia");
                intent.putExtra("date", "02 Mar 2026");
                intent.putExtra("departure", "02:00PM");
                intent.putExtra("boat", "Sea Star Express");
                intent.putExtra("reference", "SMB-2026-0044");
                intent.putExtra("status", "COMPLETED");
                startActivity(intent);
            }
        });
    }
}