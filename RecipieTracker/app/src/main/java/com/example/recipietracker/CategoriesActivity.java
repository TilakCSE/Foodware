package com.example.recipietracker;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CategoriesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeActionListener {
    private RecipeAdapter adapter;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        Spinner spinner = findViewById(R.id.spinnerCuisine);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Indian", "Italian", "Chinese"});
        spinner.setAdapter(spinnerAdapter);

        RecyclerView rv = findViewById(R.id.recyclerRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        db = new DBHelper(this);
        adapter = new RecipeAdapter(db.getAllRecipes(), this);
        rv.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String cuisine = (String) parent.getItemAtPosition(position);
                if ("All".equals(cuisine)) {
                    adapter.updateData(db.getAllRecipes());
                } else {
                    adapter.updateData(db.filterByCuisine(cuisine));
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    @Override
    public void onToggleFavorite(Recipe recipe, boolean favorite) {
        db.toggleFavorite(recipe.getId(), favorite);
    }
}







