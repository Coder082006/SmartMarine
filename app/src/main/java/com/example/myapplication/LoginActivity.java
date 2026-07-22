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

// This is the Login screen
// It checks if the user exists in the database
// If yes it takes them to the Home screen
public class LoginActivity extends AppCompatActivity {

    // Declare the input fields
    EditText editTextEmail;
    EditText editTextPassword;

    // Declare the login button
    Button buttonLogin;

    // Declare the text that takes user to register screen
    TextView textViewRegister;

    // Declare the database helper
    DatabaseHelper databaseHelper;

    // Remembers which user is logged in
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell Android which XML layout to show for this screen
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create the database helper object
        // This connects our activity to the database
        databaseHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Connect each variable to its view in the XML
        // The ID must match exactly what you wrote in your XML
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        // This runs when the user clicks the LOGIN button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the text the user typed in each field
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Check if email field is empty
                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this,
                            "Please enter your email",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!email.contains("@") || !email.contains(".")) {
                    Toast.makeText(LoginActivity.this,
                "Please enter a valid email address",
                    Toast.LENGTH_SHORT).show();
                    return;
                    }
                // Check if password field is empty
                if (password.isEmpty()) {
                    Toast.makeText(LoginActivity.this,
                            "Please enter your password",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() < 4) {
                    Toast.makeText(LoginActivity.this,
                "Password must be at least 4 characters",
                    Toast.LENGTH_SHORT).show();
                    return;
                    }


                // First check whether an account with this email even exists,
                // so we can tell the user exactly what's wrong.
                if (!databaseHelper.emailExists(email)) {
                    Toast.makeText(LoginActivity.this,
                            "No account found for this email. Please register first.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // The email exists — now check the password.
                boolean success = databaseHelper.loginUser(email, password);

                if (success) {

                    // Remember who is logged in so other screens
                    // (ticket, bookings) know the real user.
                    session.login(email);

                    // Show success message
                    Toast.makeText(LoginActivity.this,
                            "Login successful! Welcome back.",
                            Toast.LENGTH_SHORT).show();

                    // Go to the Home screen
                    Intent intent = new Intent(
                            LoginActivity.this, HomeActivity.class);
                    startActivity(intent);

                    // Close the login screen so user cannot go back to it
                    finish();

                } else {

                    // Email is right but the password doesn't match.
                    Toast.makeText(LoginActivity.this,
                            "Incorrect password. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // This runs when user clicks "Don't have an account? Register"
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Go to the Register screen
                Intent intent = new Intent(
                        LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}