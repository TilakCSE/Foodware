package com.example.recipietracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView themeButton;
    private SwitchMaterial notificationsSwitch;
    private Button logoutButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        // Find views
        toolbar = findViewById(R.id.toolbar);
        themeButton = findViewById(R.id.themeButton);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        logoutButton = findViewById(R.id.logoutButton);

        // --- Toolbar ---
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- Logout Button ---
        logoutButton.setOnClickListener(v -> showLogoutDialog());

        // --- Theme Button ---
        themeButton.setOnClickListener(v -> showThemeDialog());

        // --- Notifications (Example) ---
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // TODO: Add logic to subscribe to a Firebase topic
                Toast.makeText(this, "Notifications ON (coming soon)", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Add logic to unsubscribe
                Toast.makeText(this, "Notifications OFF (coming soon)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a confirmation dialog before logging out.
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    // Sign out from Firebase
                    mAuth.signOut();

                    // Create an Intent to go back to the WelcomeActivity
                    Intent intent = new Intent(SettingsActivity.this, WelcomeActivity.class);
                    // Add flags to clear the back stack
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Close this activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Shows a dialog to select the app theme.
     */
    private void showThemeDialog() {
        final String[] themes = {"Light", "Dark", "System Default"};

        // Find which theme is currently active
        int currentTheme = AppCompatDelegate.getDefaultNightMode();
        int checkedItem;
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            checkedItem = 0; // Light
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            checkedItem = 1; // Dark
        } else {
            checkedItem = 2; // System Default
        }

        new AlertDialog.Builder(this)
                .setTitle("Choose theme")
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    if (which == 0) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (which == 1) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}