package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class activity_search_boat extends AppCompatActivity {

    Spinner spinnerFrom;
    Spinner spinnerTo;
    LinearLayout layoutDatePicker;
    TextView tvSelectedDate;
    Button btnSearch;

    String selectedFrom = "";
    String selectedTo = "";
    String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_boat);

        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        layoutDatePicker = findViewById(R.id.layoutDatePicker);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnSearch = findViewById(R.id.btnSearch);

        String[] locations = {"Dar es Salaam", "Zanzibar", "Mafia",
                "Bagamoyo", "Mtwara", "Tanga"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        spinnerFrom.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        selectedFrom = locations[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedFrom = "";
                    }
                });

        spinnerTo.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        selectedTo = locations[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedTo = "";
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
                if (selectedFrom.isEmpty()
                        || selectedTo.isEmpty()) {
                    Toast.makeText(activity_search_boat.this,
                            "Please select both departure "
                                    + "and destination",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedFrom.equals(selectedTo)) {
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

