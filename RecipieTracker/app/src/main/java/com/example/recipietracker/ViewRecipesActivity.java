package com.example.recipietracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViewRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeActionListener {

    private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_recipes);

        RecyclerView rv = findViewById(R.id.recyclerRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        DBHelper db = new DBHelper(this);
        List<Recipe> recipes = db.getAllRecipes();
        adapter = new RecipeAdapter(recipes, this);
        rv.setAdapter(adapter);
    }

    @Override
    public void onToggleFavorite(Recipe recipe, boolean favorite) {
        DBHelper db = new DBHelper(this);
        db.toggleFavorite(recipe.getId(), favorite);
    }
}







