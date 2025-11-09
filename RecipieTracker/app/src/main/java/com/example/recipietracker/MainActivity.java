package com.example.recipietracker;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // --- THIS IS THE CHANGE ---

        // We set a custom listener to intercept clicks.
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_what_can_i_cook) {
                // Manually launch our new Activity
                Intent intent = new Intent(this, WhatCanICookActivity.class);
                startActivity(intent);

                // Return false: This tells the NavController NOT to handle this click
                // (which would cause a crash) and it keeps the previous tab
                // (e.g., "Cook") selected, which is the correct behavior.
                return false;

            } else {
                // Let the NavigationUI handle the other items (Cook and Profile)
                // This will correctly switch the fragments.
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });

        // We still link the NavController so that when the user
        // presses the "back" button, the selected tab updates correctly.
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            navView.getMenu().findItem(destination.getId()).setChecked(true);
        });

        // --- END OF CHANGE ---
    }
}