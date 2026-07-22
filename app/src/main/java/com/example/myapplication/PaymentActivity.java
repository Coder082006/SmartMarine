package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

// Simulated mobile-money payment screen (proposal feature: "Payment
// integration"). The user must pay BEFORE the digital ticket is generated.
// This is a mock gateway — no real money moves. On "success" we forward the
// same booking details to the ticket screen in "new" mode, which then saves
// the CONFIRMED booking and shows the reference number.
public class PaymentActivity extends AppCompatActivity {

    ImageButton btnBack;
    Button btnPay;
    EditText editPhone;
    RadioGroup radioNetwork;
    TextView tvSummaryBoat, tvSummaryRoute, tvSummaryDate, tvSummaryAmount, tvStatus;

    int price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainPayment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        btnPay = findViewById(R.id.btnPay);
        editPhone = findViewById(R.id.editPhone);
        radioNetwork = findViewById(R.id.radioNetwork);
        tvSummaryBoat = findViewById(R.id.tvSummaryBoat);
        tvSummaryRoute = findViewById(R.id.tvSummaryRoute);
        tvSummaryDate = findViewById(R.id.tvSummaryDate);
        tvSummaryAmount = findViewById(R.id.tvSummaryAmount);
        tvStatus = findViewById(R.id.tvStatus);

        // Read the booking details passed from the Available Boats screen.
        String boat = getIntent().getStringExtra("boat");
        String origin = getIntent().getStringExtra("origin");
        String destination = getIntent().getStringExtra("destination");
        String date = getIntent().getStringExtra("date");
        String departure = getIntent().getStringExtra("departure");
        price = getIntent().getIntExtra("price", 0);

        // Fill the summary card.
        if (boat != null) tvSummaryBoat.setText(boat);
        if (origin != null && destination != null) {
            tvSummaryRoute.setText(origin + " → " + destination);
        }
        tvSummaryDate.setText((date == null ? "" : date)
                + (departure == null ? "" : " · " + departure));
        tvSummaryAmount.setText(String.format(Locale.US, "TZS %,d", price));
        btnPay.setText(String.format(Locale.US, "PAY TZS %,d", price));

        btnBack.setOnClickListener(v -> finish());
        btnPay.setOnClickListener(v -> startPayment());
    }

    private void startPayment() {
        String phone = editPhone.getText().toString().trim();

        // Basic mobile-money number check (Tanzanian numbers are ~10 digits).
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() < 10) {
            Toast.makeText(this, "Please enter a valid mobile number",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulate contacting the mobile-money network. Disable the button
        // and show a "processing" message, then succeed after a short delay.
        btnPay.setEnabled(false);
        editPhone.setEnabled(false);
        radioNetwork.setEnabled(false);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Processing payment…\nApprove the prompt on " + phone);

        new Handler(Looper.getMainLooper()).postDelayed(this::onPaymentSuccess, 2200);
    }

    private void onPaymentSuccess() {
        tvStatus.setText("✅ Payment successful");
        Toast.makeText(this, "Payment received. Generating your ticket…",
                Toast.LENGTH_SHORT).show();

        // Forward all the booking details to the ticket screen, which saves
        // the CONFIRMED booking and shows the reference number.
        Intent intent = new Intent(this, activity_ticket.class);
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.putExtra("mode", "new");
        startActivity(intent);

        // Close the payment screen so Back goes to the boat list, not here.
        finish();
    }
}