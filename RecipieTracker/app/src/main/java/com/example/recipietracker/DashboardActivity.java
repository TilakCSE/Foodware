package com.example.recipietracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements DashboardAdapter.OnDashboardClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        RecyclerView recyclerView = findViewById(R.id.recyclerDashboard);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        DashboardAdapter adapter = new DashboardAdapter(buildItems(), this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddRecipeActivity.class)));
    }

    private List<DashboardItem> buildItems() {
        List<DashboardItem> items = new ArrayList<>();
        items.add(new DashboardItem("Add Recipe", R.drawable.ic_add_recipe));
        items.add(new DashboardItem("View Recipes", R.drawable.ic_view_recipes));
        items.add(new DashboardItem("Search Recipes", R.drawable.ic_search_recipes));
        items.add(new DashboardItem("Favorites", R.drawable.ic_favorites));
        items.add(new DashboardItem("Pantry Optimizer", R.drawable.ic_cook));
        items.add(new DashboardItem("Categories", R.drawable.ic_categories));
        return items;
    }

    @Override
    public void onDashboardItemClick(String label) {
        Intent intent = null;
        switch (label) {
            case "Add Recipe":
                intent = new Intent(this, AddRecipeActivity.class);
                break;
            case "View Recipes":
                intent = new Intent(this, ViewRecipesActivity.class);
                break;
            case "Search Recipes":
                intent = new Intent(this, SearchRecipesActivity.class);
                break;
            case "Favorites":
                intent = new Intent(this, FavoritesActivity.class);
                break;
            case "Pantry Optimizer":
                intent = new Intent(this, PantryOptimizerActivity.class);
                break;
            case "Categories":
                intent = new Intent(this, CategoriesActivity.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}



