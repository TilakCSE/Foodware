package com.example.recipietracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This is the new entry point, so it handles the splash screen
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button beginButton = findViewById(R.id.buttonBegin);
        TextView signInText = findViewById(R.id.textViewSignIn);

        // Set up click listener for the "Begin" button
        beginButton.setOnClickListener(v -> {
            // Create an Intent to start DashboardActivity
            Intent intent = new Intent(WelcomeActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish(); // Optional: finish WelcomeActivity so user can't go back to it
        });

        // Set up click listener for the "Sign in" text
        // In WelcomeActivity.java and SignUpActivity.java

        signInText.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
        });
    }
}