package com.example.recipietracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

public class WhatCanICookActivity extends AppCompatActivity {

    private static final String TAG = "WhatCanICookActivity";

    // UI Views
    private MaterialToolbar toolbar;
    private EditText ingredientsEditText;
    private Button searchApiButton;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    // TODO: Add adapter and list for results

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_what_can_i_cook);

        // Find all the views
        toolbar = findViewById(R.id.toolbar);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        searchApiButton = findViewById(R.id.searchApiButton);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.apiRecipesRecyclerView);

        // Set up the toolbar's back button
        toolbar.setNavigationOnClickListener(v -> finish());

        // Set up the search button
        searchApiButton.setOnClickListener(v -> {
            String ingredients = ingredientsEditText.getText().toString().trim();
            if (ingredients.isEmpty()) {
                Toast.makeText(this, "Please enter some ingredients.", Toast.LENGTH_SHORT).show();
            } else {
                // We will add the API call logic here
                Toast.makeText(this, "Searching for recipes with: " + ingredients, Toast.LENGTH_SHORT).show();
                // We'll call a method like:
                // searchRecipesByIngredients(ingredients);
            }
        });

        // TODO: Set up the RecyclerView
    }

    // TODO: Add searchRecipesByIngredients(String ingredients) method
}