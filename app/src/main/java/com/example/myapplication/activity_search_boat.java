package com.example.myapplication;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class activity_search_boat extends AppCompatActivity {

    ImageButton btnBack;
    EditText editTextFrom;
    EditText editTextTo;
    View layoutDatePicker;
    TextView tvSelectedDate;
    Button btnSearch;

    String selectedDate = "";

    private static final int REQUEST_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_boat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        editTextFrom = findViewById(R.id.editTextFrom);
        editTextTo = findViewById(R.id.editTextTo);
        layoutDatePicker = findViewById(R.id.layoutDatePicker);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnSearch = findViewById(R.id.btnSearch);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Try to auto-fill "From" with the user's nearest port using GPS.
        requestNearestPort();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        selectedDate = day + "/" + (month + 1) + "/" + year;
        tvSelectedDate.setText(selectedDate);

        layoutDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        activity_search_boat.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view,
                                                  int year, int month, int dayOfMonth) {
                                selectedDate = dayOfMonth + "/"
                                        + (month + 1) + "/" + year;
                                tvSelectedDate.setText(selectedDate);
                            }
                        }, year, month, day);
                datePickerDialog.getDatePicker()
                        .setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedFrom = editTextFrom.getText().toString().trim();
                String selectedTo = editTextTo.getText().toString().trim();

                if (selectedFrom.isEmpty()
                        || selectedTo.isEmpty()) {
                    Toast.makeText(activity_search_boat.this,
                            "Please enter both departure "
                                    + "and destination",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedFrom.equalsIgnoreCase(selectedTo)) {
                    Toast.makeText(activity_search_boat.this,
                            "Departure and destination "
                                    + "cannot be the same",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(
                        activity_search_boat.this,
                        activity_available.class);
                intent.putExtra("from", selectedFrom);
                intent.putExtra("to", selectedTo);
                intent.putExtra("date", selectedDate);
                startActivity(intent);
            }
        });
    }

    // Ask for location permission (if needed) then fill in the nearest port.
    private void requestNearestPort() {
        boolean fine = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fine || coarse) {
            fillNearestPort();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fillNearestPort();
        }
    }

    // Uses the device GPS (LocationManager) to find the last known position,
    // then puts the nearest port into the "From" field (only if it's empty,
    // so we never overwrite what the user typed).
    private void fillNearestPort() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (lm == null) {
            return;
        }

        Location location = null;
        try {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException e) {
            return;
        }

        if (location != null && editTextFrom.getText().toString().trim().isEmpty()) {
            String port = PortLocator.nearestPort(
                    location.getLatitude(), location.getLongitude());
            editTextFrom.setText(port);
            Toast.makeText(this, "Nearest port: " + port, Toast.LENGTH_SHORT).show();
        }
    }
}
