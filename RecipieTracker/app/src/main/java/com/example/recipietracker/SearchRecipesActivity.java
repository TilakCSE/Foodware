package com.example.recipietracker;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeActionListener {
    private RecipeAdapter adapter;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipes);

        RecyclerView rv = findViewById(R.id.recyclerRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        db = new DBHelper(this);
        adapter = new RecipeAdapter(db.getAllRecipes(), this);
        rv.setAdapter(adapter);

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                adapter.updateData(db.searchRecipes(s.toString()));
            }
        });
    }

    @Override
    public void onToggleFavorite(Recipe recipe, boolean favorite) {
        db.toggleFavorite(recipe.getId(), favorite);
    }
}







