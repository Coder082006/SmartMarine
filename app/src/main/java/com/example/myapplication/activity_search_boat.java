package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
}
