package com.example.recipietracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WhatCanICookActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeActionListener {
    private RecipeAdapter adapter;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_what_can_i_cook);

        RecyclerView rv = findViewById(R.id.recyclerRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        db = new DBHelper(this);
        adapter = new RecipeAdapter(db.getAllRecipes(), this);
        rv.setAdapter(adapter);

        EditText etIngredients = findViewById(R.id.etIngredients);
        Button btnSuggest = findViewById(R.id.btnSuggest);
        btnSuggest.setOnClickListener(v -> {
            String input = etIngredients.getText().toString();
            adapter.updateData(db.suggestByIngredients(input));
        });
    }

    @Override
    public void onToggleFavorite(Recipe recipe, boolean favorite) {
        db.toggleFavorite(recipe.getId(), favorite);
    }
}







