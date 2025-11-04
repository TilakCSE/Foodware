package com.example.recipietracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition FIRST
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // --- CHECK IF USER IS ALREADY LOGGED IN ---
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, go directly to MainActivity
            Log.d("WelcomeActivity", "User already logged in: " + currentUser.getUid());
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close WelcomeActivity so the user can't go back to it
            return; // Important: Stop executing the rest of onCreate
        }
        // ------------------------------------------

        // --- User is NOT logged in, show the Welcome screen ---
        Log.d("WelcomeActivity", "No user logged in, showing Welcome screen.");
        setContentView(R.layout.activity_welcome);

        Button beginButton = findViewById(R.id.buttonBegin);
        TextView signInText = findViewById(R.id.textViewSignIn);

        // Navigate to Onboarding
        beginButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, OnboardingActivity.class);
            startActivity(intent);
        });

        // Navigate to Sign In
        signInText.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
        });
    }
}