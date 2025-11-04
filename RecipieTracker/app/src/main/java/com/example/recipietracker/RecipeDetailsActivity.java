package com.example.recipietracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar; // Import RatingBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.DialogInterface;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Locale; // Import Locale

public class RecipeDetailsActivity extends AppCompatActivity {

    private static final String TAG = "RecipeDetailsActivity";

    // UI Views
    private ImageView recipeImageView;
    private TextView recipeTitleTextView, recipeDescriptionTextView, tipsTextView;
    private TextView ingredientCountTextView, difficultyTextView, timeTextView, caloriesTextView;
    private RatingBar ratingBar; // Add RatingBar
    private TextView authorTextView; // Add Author TextView
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private String recipeAuthorId;
    private Button registerMealButton;
    private TabLayout tabLayout;
    private LinearLayout ingredientsInstructionsContainer;

    // Firebase
    private FirebaseFirestore db;
    private String recipeId;
    private DocumentSnapshot currentRecipeDoc; // Store the fetched document

    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Handle case where user is not logged in, though they shouldn't be here
            Log.e(TAG, "User is not logged in!");
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recipeId = getIntent().getStringExtra("RECIPE_ID");

        findViews(); // Find all UI elements

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
        }

        if (recipeId != null) {
            loadRecipeDetails();
        } else {
            Toast.makeText(this, "Error: Recipe ID not found.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Recipe ID is null.");
            finish();
        }

        setupTabs(); // Set up the Ingredients/Instructions tabs
    }

    private void findViews() {
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        recipeImageView = findViewById(R.id.detailRecipeImageView);
        recipeTitleTextView = findViewById(R.id.detailRecipeTitle);
        ratingBar = findViewById(R.id.detailRatingBar); // Find RatingBar
        authorTextView = findViewById(R.id.detailAuthorTextView); // Find Author TextView
        recipeDescriptionTextView = findViewById(R.id.detailRecipeDescription);
        ingredientCountTextView = findViewById(R.id.detailIngredientCount);
        difficultyTextView = findViewById(R.id.detailDifficulty);
        timeTextView = findViewById(R.id.detailTime);
        caloriesTextView = findViewById(R.id.detailCalories);
        tabLayout = findViewById(R.id.detailTabLayout);
        ingredientsInstructionsContainer = findViewById(R.id.ingredientsInstructionsContainer);
        tipsTextView = findViewById(R.id.detailTipsText);
        registerMealButton = findViewById(R.id.registerMealButton);
        registerMealButton.setOnClickListener(v -> {
            if (currentUserId != null && !currentUserId.isEmpty()) {
                showListSelectionDialog();
            } else {
                Toast.makeText(this, "Error: Could not find user.", Toast.LENGTH_SHORT).show();
            }
        });
        // Find other views like buttons if needed
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle Toolbar item clicks (back arrow, share, save etc.)
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to the previous screen
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            Toast.makeText(this, "Share clicked (implement later)", Toast.LENGTH_SHORT).show();
            return true;
        } // Add cases for other menu items
        return super.onOptionsItemSelected(item);
    }


    // Inside RecipeDetailsActivity.java

    private void loadRecipeDetails() {
        // Get recipe ID and potentially user ID from the intent
        recipeId = getIntent().getStringExtra("RECIPE_ID");
        String userId = getIntent().getStringExtra("USER_ID"); // Check if USER_ID was passed

        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "Error: Recipe ID missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading recipe with ID: " + recipeId + (userId != null ? " for user: " + userId : " (public)"));

        // Determine the correct Firestore path based on whether userId exists
        com.google.firebase.firestore.DocumentReference recipeRef;
        if (userId != null && !userId.isEmpty()) {
            // It's a user's recipe, query the subcollection
            recipeRef = db.collection("users").document(userId).collection("my_recipes").document(recipeId);
        } else {
            // It's a public recipe from the main collection
            recipeRef = db.collection("recipes").document(recipeId);
        }

        // Fetch the document from the determined reference
        recipeRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Recipe data fetched successfully.");
                        currentRecipeDoc = documentSnapshot;
                        this.recipeAuthorId = currentRecipeDoc.getString("userId");
                        populateUI(currentRecipeDoc);
                    } else {
                        Log.w(TAG, "No such document found at the specified path.");
                        Toast.makeText(this, "Recipe not found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching recipe", e);
                    Toast.makeText(this, "Error loading recipe details.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void showListSelectionDialog() {
        // We already have currentUserId from onCreate

        db.collection("users").document(currentUserId).collection("my_lists")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "You have no lists. Create one in your profile!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Create lists to hold the names and their corresponding IDs
                    final List<String> listNames = new ArrayList<>();
                    final List<String> listIds = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String listName = doc.getString("listName");
                        if (listName != null && !listName.isEmpty()) {
                            listNames.add(listName);
                            listIds.add(doc.getId()); // Store the document ID
                        }
                    }

                    // Create an array adapter for the dialog
                    CharSequence[] items = listNames.toArray(new CharSequence[0]);

                    // Create the dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Add to which list?")
                            .setItems(items, (dialog, which) -> {
                                // 'which' is the index of the item clicked
                                String selectedListId = listIds.get(which);
                                String selectedListName = listNames.get(which);

                                // Call the method to save the recipe to this list
                                saveRecipeToList(selectedListId, selectedListName);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user lists", e);
                    Toast.makeText(this, "Error fetching lists: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveRecipeToList(String selectedListId, String selectedListName) {
        if (recipeId == null) {
            Toast.makeText(this, "Error: Recipe ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the reference to the specific list document
        DocumentReference listRef = db.collection("users").document(currentUserId)
                .collection("my_lists").document(selectedListId);

        Map<String, String> recipeReference = new HashMap<>();
        recipeReference.put("recipeId", recipeId);
        recipeReference.put("authorId", this.recipeAuthorId);

        // Use FieldValue.arrayUnion() to add the recipe ID to the 'recipeIds' array
        // This automatically prevents duplicates
        listRef.update("recipes", FieldValue.arrayUnion(recipeReference))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recipe " + recipeId + " added to list " + selectedListId);
                    Toast.makeText(this, "Saved to " + selectedListName, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // ... (failure listener)
                });
    }

// populateUI, setupTabs, displayIngredients, displayInstructions methods remain the same

    private void populateUI(DocumentSnapshot doc) {
        if (doc == null) return;

        String title = doc.getString("title");
        String imageUrl = doc.getString("imageUrl");
        String description = doc.getString("description");
        String tips = doc.getString("tips");
        String difficulty = doc.getString("difficulty");
        String author = doc.getString("authorName");
        Long timeLong = doc.getLong("timeMinutes"); // Get as Long
        int time = (timeLong != null) ? timeLong.intValue() : 0; // Convert to int
        List<Map<String, String>> ingredients = (List<Map<String, String>>) doc.get("ingredients");
        int ingredientCount = (ingredients != null) ? ingredients.size() : 0;

        Long caloriesLong = doc.getLong("calories"); // Get as Long
        int calories = (caloriesLong != null) ? caloriesLong.intValue() : 0;

        // Populate basic info
        collapsingToolbarLayout.setTitle(title);
        recipeTitleTextView.setText(title);
        recipeDescriptionTextView.setText(description != null ? description : "");
        tipsTextView.setText(tips != null ? tips : "No tips provided.");
        authorTextView.setText(author != null ? "by " + author : "");

        // Populate stats
        ingredientCountTextView.setText(String.valueOf(ingredientCount));
        difficultyTextView.setText(difficulty != null ? difficulty : "--");
        timeTextView.setText(time > 0 ? time + "'" : "--");
        caloriesTextView.setText(calories > 0 ? String.valueOf(calories) : "--");
        // TODO: Populate RatingBar with actual rating data

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.placeholder_food)
                .into(recipeImageView);

        // Load ingredients initially into the tab container
        displayIngredients();
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (currentRecipeDoc != null) { // Make sure we have the data
                    if (tab.getPosition() == 0) { // Ingredients tab
                        displayIngredients();
                    } else { // Instructions tab
                        displayInstructions();
                    }
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void displayIngredients() {
        if (currentRecipeDoc == null) return;
        ingredientsInstructionsContainer.removeAllViews();
        List<Map<String, String>> ingredients = (List<Map<String, String>>) currentRecipeDoc.get("ingredients");
        LayoutInflater inflater = LayoutInflater.from(this);

        if (ingredients != null && !ingredients.isEmpty()) {
            for (Map<String, String> ingredient : ingredients) {
                View ingredientView = inflater.inflate(R.layout.item_ingredient, ingredientsInstructionsContainer, false);
                TextView name = ingredientView.findViewById(R.id.ingredientName);
                TextView quantity = ingredientView.findViewById(R.id.ingredientQuantity);

                String quantityStr = ingredient.getOrDefault("quantity", "");
                String unitStr = ingredient.getOrDefault("unit", "");
                String combinedQuantity = (quantityStr + " " + unitStr).trim();

                name.setText(ingredient.getOrDefault("name", "Unknown Ingredient"));
                quantity.setText(combinedQuantity.isEmpty() ? "" : combinedQuantity);
                ingredientsInstructionsContainer.addView(ingredientView);
            }
        } else {
            TextView tv = new TextView(this);
            tv.setText("No ingredients listed.");
            ingredientsInstructionsContainer.addView(tv);
        }
    }

    private void displayInstructions() {
        if (currentRecipeDoc == null) return;
        ingredientsInstructionsContainer.removeAllViews();
        List<String> instructions = (List<String>) currentRecipeDoc.get("instructions");
        LayoutInflater inflater = LayoutInflater.from(this);

        if (instructions != null && !instructions.isEmpty()) {
            for (int i = 0; i < instructions.size(); i++) {
                View instructionView = inflater.inflate(R.layout.item_instruction, ingredientsInstructionsContainer, false);
                TextView stepNumber = instructionView.findViewById(R.id.stepNumber);
                TextView stepText = instructionView.findViewById(R.id.stepText);

                stepNumber.setText(String.valueOf(i + 1));
                stepText.setText(instructions.get(i));
                ingredientsInstructionsContainer.addView(instructionView);
            }
        } else {
            TextView tv = new TextView(this);
            tv.setText("No instructions provided.");
            ingredientsInstructionsContainer.addView(tv);
        }
    }
}