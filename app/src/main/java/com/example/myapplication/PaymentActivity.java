package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    ImageButton btnBack;
    Button btnPay;
    EditText editPhone;
    RadioGroup radioNetwork;
    TextView tvSummaryBoat, tvSummaryRoute, tvSummaryDate, tvSummaryAmount, tvStatus;

    DatabaseHelper databaseHelper;
    SessionManager session;

    String boat, origin, destination, date, departure, passenger;
    int price;

    private String currentTrackingId;
    private int pollCount;
    private static final int POLL_MAX_TRIES = 10;
    private static final long POLL_INTERVAL_MS = 3000;
    private final Handler pollHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> pesapalLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    startPolling();
                } else {
                    resetForm("Payment cancelled");
                }
            });

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

        databaseHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        btnPay = findViewById(R.id.btnPay);
        editPhone = findViewById(R.id.editPhone);
        radioNetwork = findViewById(R.id.radioNetwork);
        tvSummaryBoat = findViewById(R.id.tvSummaryBoat);
        tvSummaryRoute = findViewById(R.id.tvSummaryRoute);
        tvSummaryDate = findViewById(R.id.tvSummaryDate);
        tvSummaryAmount = findViewById(R.id.tvSummaryAmount);
        tvStatus = findViewById(R.id.tvStatus);

        boat = getIntent().getStringExtra("boat");
        origin = getIntent().getStringExtra("origin");
        destination = getIntent().getStringExtra("destination");
        date = getIntent().getStringExtra("date");
        departure = getIntent().getStringExtra("departure");
        price = getIntent().getIntExtra("price", 0);

        passenger = getIntent().getStringExtra("passenger");
        if (passenger == null || passenger.isEmpty()) {
            passenger = databaseHelper.getUserName(session.getEmail());
        }
        if (passenger == null || passenger.isEmpty()) {
            passenger = "Guest";
        }

        if (boat != null) tvSummaryBoat.setText(boat);
        if (origin != null && destination != null) {
            tvSummaryRoute.setText(origin + " \u2192 " + destination);
        }
        tvSummaryDate.setText((date == null ? "" : date)
                + (departure == null ? "" : " \u00b7 " + departure));
        tvSummaryAmount.setText(String.format(Locale.US, "TZS %,d", price));
        btnPay.setText(String.format(Locale.US, "PAY TZS %,d", price));

        btnBack.setOnClickListener(v -> finish());
        btnPay.setOnClickListener(v -> startPayment());
    }

    private void startPayment() {
        String phone = editPhone.getText().toString().trim();
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() < 10) {
            Toast.makeText(this, "Please enter a valid mobile number",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ApiClient.isConfigured()) {
            Toast.makeText(this, "Payment server not configured",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnPay.setEnabled(false);
        editPhone.setEnabled(false);
        radioNetwork.setEnabled(false);
        tvStatus.setVisibility(android.view.View.VISIBLE);
        tvStatus.setText("Starting secure payment\u2026");

        String userEmail = session.getEmail();
        if (userEmail == null) userEmail = "guest@smartmarine.com";

        String firstName = passenger;
        String desc = (origin != null ? origin : "") + " \u2192 "
                + (destination != null ? destination : "") + " (" + (boat != null ? boat : "") + ")";

        ApiClient.startPesaPalPayment(this, price, digits, userEmail, firstName, desc,
                new ApiClient.PesaPalStartCallback() {
                    @Override
                    public void onSuccess(String trackingId, String redirectUrl) {
                        currentTrackingId = trackingId;
                        Intent intent = new Intent(PaymentActivity.this, PesaPalWebViewActivity.class);
                        intent.putExtra(PesaPalWebViewActivity.EXTRA_REDIRECT_URL, redirectUrl);
                        pesapalLauncher.launch(intent);
                    }

                    @Override
                    public void onError(String message) {
                        resetForm("Could not start payment: " + message);
                    }
                });
    }

    private void startPolling() {
        pollCount = 0;
        tvStatus.setText("Verifying payment\u2026");
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    private void stopPolling() {
        pollHandler.removeCallbacks(pollRunnable);
    }

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentTrackingId == null) return;
            pollCount++;

            if (pollCount > POLL_MAX_TRIES) {
                resetForm("Payment verification timed out. Please try again.");
                return;
            }

            ApiClient.checkPesaPalStatus(PaymentActivity.this, currentTrackingId,
                    new ApiClient.PesaPalStatusCallback() {
                        @Override
                        public void onResult(String status, String description) {
                            if ("COMPLETED".equals(status)) {
                                stopPolling();
                                tvStatus.setText("\u2705 Payment successful!");
                                launchTicketScreen();
                            } else if ("FAILED".equals(status) || "INVALID".equals(status) || "REVERSED".equals(status)) {
                                stopPolling();
                                resetForm("Payment " + status.toLowerCase() + ": " + description);
                            } else {
                                pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                            }
                        }

                        @Override
                        public void onError(String message) {
                            if (pollCount < POLL_MAX_TRIES) {
                                pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                            } else {
                                resetForm("Could not verify payment. Please try again.");
                            }
                        }
                    });
        }
    };

    private void launchTicketScreen() {
        Intent intent = new Intent(this, activity_ticket.class);
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.putExtra("mode", "new");
        startActivity(intent);
        finish();
    }

    private void resetForm(String message) {
        stopPolling();
        btnPay.setEnabled(true);
        editPhone.setEnabled(true);
        radioNetwork.setEnabled(true);
        tvStatus.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        stopPolling();
        super.onDestroy();
    }
}
