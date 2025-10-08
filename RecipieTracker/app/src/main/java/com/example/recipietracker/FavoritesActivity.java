package com.example.recipietracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FavoritesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeActionListener {
    private RecipeAdapter adapter;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_recipes);
        RecyclerView rv = findViewById(R.id.recyclerRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        db = new DBHelper(this);
        adapter = new RecipeAdapter(db.getFavorites(), this);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.updateData(db.getFavorites());
    }

    @Override
    public void onToggleFavorite(Recipe recipe, boolean favorite) {
        db.toggleFavorite(recipe.getId(), favorite);
    }
}







