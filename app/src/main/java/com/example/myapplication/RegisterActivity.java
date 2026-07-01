package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// This is the Register screen
// It takes the user details and saves them to the database
public class RegisterActivity extends AppCompatActivity {

    // Declare the input fields that the user will type in
    EditText editTextName;
    EditText editTextEmail;
    EditText editTextPhone;
    EditText editTextPassword;

    // Declare the register button
    Button buttonRegister;

    // Declare the text that takes user to login screen
    TextView textViewLogin;

    // Declare the database helper
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell Android which XML layout to show for this screen
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create the database helper object
        // This connects our activity to the database
        databaseHelper = new DatabaseHelper(this);

        // Connect each variable to its view in the XML
        // The ID must match exactly what you wrote in your XML
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);

        // This runs when the user clicks the REGISTER button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the text the user typed in each field
                String name = editTextName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String phone = editTextPhone.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Check if any field is left empty
                // If empty show a message and stop
                if (name.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,
                            "Please enter your full name",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (name.length() < 3) {
                    Toast.makeText(RegisterActivity.this,
                            "Name must be at least 3 characters",
                            Toast.LENGTH_SHORT).show();
                     return;
                }


                if (email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,
                            "Please enter your email",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!email.contains("@") || !email.contains(".")) {
                    Toast.makeText(RegisterActivity.this,
                            "Please enter a valid email address",
                            Toast.LENGTH_SHORT).show();
                     return;
                }


                if (phone.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,
                            "Please enter your phone number",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (phone.length() < 10) {
                    Toast.makeText(RegisterActivity.this,
                            "Please enter a valid phone number",
                            Toast.LENGTH_SHORT).show();
                    	    return;
                    	}


                if (password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,
                            "Please enter your password",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this,
                            "Password must be at least 6 characters",
                            Toast.LENGTH_SHORT).show();
                    return;
                }


                    // All fields are filled
                // Now save the user to the database
                boolean success = databaseHelper.registerUser(
                        name, email, phone, password);

                // Check if saving was successful
                if (success) {

                    // Show success message
                    Toast.makeText(RegisterActivity.this,
                            "Account created successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Go to the Login screen
                    Intent intent = new Intent(
                            RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);

                    // Close the register screen
                    // so user cannot go back to it
                    finish();

                } else {

                    // Saving failed
                    // This usually means email is already registered
                    Toast.makeText(RegisterActivity.this,
                            "Email already exists. Try another email.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // This runs when the user clicks "Already have an account? Login"
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Go to the Login screen
                Intent intent = new Intent(
                        RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}