package com.example.recipietracker;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private static final String TAG = "CategoriesActivity";

    private RecyclerView recyclerView;
    private MaterialToolbar toolbar;

    private FirebaseFirestore db;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        db = FirebaseFirestore.getInstance();

        // Get the category name from the intent (from CookFragment)
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.categoriesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Check which mode we are in
        if (categoryName != null) {
            // MODE 1: Show recipes for a specific category
            toolbar.setTitle(categoryName);
            loadRecipesForCategory(categoryName);
        } else {
            // MODE 2: "See All" was clicked, show all categories (Option A)
            toolbar.setTitle("All Categories");
            loadAllCategories();
        }
    }

    /**
     * Mode 2: Fetches all documents from the 'categories' collection
     * and displays them using CategoryListAdapter.
     */
    private void loadAllCategories() {
        List<CategoryItem> categoryList = new ArrayList<>();
        CategoryListAdapter adapter = new CategoryListAdapter(this, categoryList);
        recyclerView.setAdapter(adapter);

        db.collection("categories").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No categories found.");
                        Toast.makeText(this, "No categories found.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CategoryItem item = doc.toObject(CategoryItem.class);
                        categoryList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading categories", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Mode 1: Fetches all recipes from the 'recipes' collection
     * that match the given categoryName.
     */
    private void loadRecipesForCategory(String categoryName) {
        List<RecipeItem> recipeList = new ArrayList<>();
        CategoryRecipeAdapter adapter = new CategoryRecipeAdapter(this, recipeList);
        recyclerView.setAdapter(adapter);

        db.collection("recipes")
                .whereEqualTo("category", categoryName) // The query!
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No recipes found for category: " + categoryName);
                        Toast.makeText(this, "No recipes found in " + categoryName, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Use our updated RecipeItem.java mold
                        RecipeItem item = doc.toObject(RecipeItem.class);
                        recipeList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading recipes for category: " + categoryName, e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}