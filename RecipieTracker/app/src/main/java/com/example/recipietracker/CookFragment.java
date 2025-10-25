package com.example.recipietracker;

import android.content.Intent; // Import Intent
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Fragment for the main "Cook" screen, displaying various sections like
 * greeting, search, new recipes, community posts, and categories.
 * Implements OnRecipeClickListener to handle clicks on recipe cards.
 */
public class CookFragment extends Fragment implements HorizontalRecipeAdapter.OnRecipeClickListener {

    private static final String TAG = "CookFragment"; // Tag for logging

    // UI Elements
    private RecyclerView recyclerView;

    // Adapters and Data
    private CookAdapter adapter;
    private List<Object> items = new ArrayList<>(); // List holding all row data (mixed types)

    // Firebase
    private FirebaseFirestore db;

    /**
     * Called to have the fragment instantiate its user interface view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout defined in fragment_cook.xml
        return inflater.inflate(R.layout.fragment_cook, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored into the view.
     * Initializes views, sets up RecyclerView, and starts data loading.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance(); // Get Firestore instance
        recyclerView = view.findViewById(R.id.cookRecyclerView); // Find the RecyclerView in the layout

        setupRecyclerView(); // Configure the RecyclerView and its LayoutManager
        loadAllData(); // Start fetching data from Firestore
    }

    /**
     * Handles clicks on recipe items forwarded from the HorizontalRecipeAdapter.
     * Starts the RecipeDetailsActivity with the ID of the clicked recipe.
     * @param recipeId The unique ID of the clicked recipe document in Firestore.
     */
    @Override
    public void onRecipeClick(String recipeId) {
        Log.d(TAG, "Recipe clicked with ID: " + recipeId);
        if (getActivity() == null) {
            Log.e(TAG, "getActivity() returned null in onRecipeClick. Fragment might be detached.");
            return; // Prevent crash if fragment is detached
        }

        // Create an Intent to start the RecipeDetailsActivity
        Intent intent = new Intent(getActivity(), RecipeDetailsActivity.class);
        // Pass the clicked recipe's ID as an extra
        intent.putExtra("RECIPE_ID", recipeId);
        startActivity(intent);
    }

    /**
     * Sets up the RecyclerView with a GridLayoutManager configured for multiple span sizes
     * and initializes the CookAdapter.
     */
    private void setupRecyclerView() {
        // Initialize the adapter with the (currently empty) data list
        adapter = new CookAdapter(items, this);

        // Use a GridLayoutManager with 2 columns as the base layout
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        // Configure SpanSizeLookup to define how many columns each item type occupies
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Defensive check in case adapter/items are not ready or position is invalid
                if (adapter == null || position < 0 || position >= adapter.getItemCount()) {
                    return 2; // Default to full width to prevent crashes
                }
                // Ask the adapter for the view type at this position
                switch (adapter.getItemViewType(position)) {
                    case CookAdapter.VIEW_TYPE_CATEGORY_GRID:
                        return 1; // Category items take 1 span (half width)
                    default:
                        // All other types (Greeting, Search, Headers, Horizontal Lists) take 2 spans (full width)
                        return 2;
                }
            }
        });

        recyclerView.setLayoutManager(layoutManager); // Set the configured LayoutManager
        recyclerView.setAdapter(adapter); // Set the adapter
    }

    /**
     * Orchestrates the fetching of all data sections from Firestore sequentially
     * to maintain the correct display order. Starts by fetching the user's name.
     */
    private void loadAllData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // If user is not logged in, show minimal content (e.g., search bar only)
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user found. Cannot load personalized data.");
            items.clear();
            items.add("SEARCH"); // Still show search bar
            // Optionally: Fetch public recipes/categories if the app supports anonymous browsing
            adapter.notifyDataSetChanged();
            return;
        }

        // Clear the list before starting new fetches
        items.clear();
        adapter.notifyDataSetChanged(); // Important to clear the view first
        Log.d(TAG, "Starting data load for user: " + currentUser.getUid());

        // Chain the Firestore calls: Start with user data for the greeting
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    // --- 1. User Greeting is fetched ---
                    Log.d(TAG, "User document fetched successfully.");
                    String name = "User"; // Default name
                    if (userDoc.exists()) {
                        String fetchedName = userDoc.getString("firstName"); // Use "firstName"
                        if (fetchedName != null && !fetchedName.isEmpty()) {
                            name = fetchedName;
                        }
                        Log.d(TAG, "User name found: " + name);
                    } else {
                        Log.w(TAG, "User document does not exist for UID: " + currentUser.getUid());
                    }
                    String greeting = getGreeting() + ", " + name + "!";
                    items.add(new GreetingItem(greeting)); // Add GreetingItem object
                    items.add("SEARCH");                   // Add placeholder for search bar
                    // Notify adapter about the first two items added
                    adapter.notifyItemRangeInserted(0, 2);

                    // --- 2. Proceed to fetch New Recipes ---
                    fetchNewRecipes();
                })
                .addOnFailureListener(e -> {
                    // Handle failure to get user document (e.g., network error, permissions)
                    Log.e(TAG, "Error fetching user document for greeting", e);
                    String greeting = getGreeting() + "!"; // Use default greeting
                    items.add(new GreetingItem(greeting));
                    items.add("SEARCH");
                    adapter.notifyItemRangeInserted(0, 2);
                    // Still try to fetch the rest of the non-personalized data
                    fetchNewRecipes();
                });
    }

    /** Fetches the "New Recipes" list from the public 'recipes' collection. */
    private void fetchNewRecipes() {
        int headerPos = items.size(); // Position where the header will be inserted
        items.add("New recipes");
        adapter.notifyItemInserted(headerPos);

        db.collection("recipes").limit(5).get() // Fetch first 5 recipes
                .addOnSuccessListener(recipesSnaps -> {
                    Log.d(TAG, "New Recipes fetched successfully: " + recipesSnaps.size() + " items.");
                    List<RecipeItem> newRecipes = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : recipesSnaps) {
                        newRecipes.add(doc.toObject(RecipeItem.class));
                    }
                    items.add(newRecipes); // Add the List<RecipeItem> object
                    adapter.notifyItemInserted(headerPos + 1); // Notify adapter about the list row

                    // --- 3. Proceed to fetch Community Posts ---
                    fetchCommunityPosts();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching New Recipes", e);
                    // Continue to next section even if this fails, maybe add empty list?
                    items.add(new ArrayList<RecipeItem>()); // Add empty list on failure
                    adapter.notifyItemInserted(headerPos + 1);
                    fetchCommunityPosts();
                });
    }

    /** Fetches the "Community" posts list. */
    private void fetchCommunityPosts() {
        int headerPos = items.size();
        items.add("Community");
        adapter.notifyItemInserted(headerPos);

        db.collection("community_posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(5).get()
                .addOnSuccessListener(communitySnaps -> {
                    Log.d(TAG, "Community Posts fetched successfully: " + communitySnaps.size() + " items.");
                    List<CommunityPostItem> communityItems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : communitySnaps) {
                        communityItems.add(doc.toObject(CommunityPostItem.class));
                    }
                    items.add(communityItems); // Add the List<CommunityPostItem> object
                    adapter.notifyItemInserted(headerPos + 1);

                    // --- 4. Finally, fetch Categories ---
                    fetchCategories();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching Community Posts", e);
                    items.add(new ArrayList<CommunityPostItem>()); // Add empty list on failure
                    adapter.notifyItemInserted(headerPos + 1);
                    fetchCategories(); // Continue anyway
                });
    }

    /** Fetches the "Categories" grid items. */
    private void fetchCategories() {
        int headerPos = items.size();
        items.add("Categories");
        adapter.notifyItemInserted(headerPos);

        db.collection("categories").get()
                .addOnSuccessListener(categorySnaps -> {
                    Log.d(TAG, "Categories fetched successfully: " + categorySnaps.size() + " items.");
                    int categoryStartPos = headerPos + 1;
                    int categoryCount = 0;
                    for (QueryDocumentSnapshot doc : categorySnaps) {
                        items.add(doc.toObject(CategoryItem.class)); // Add each CategoryItem individually
                        categoryCount++;
                    }
                    adapter.notifyItemRangeInserted(categoryStartPos, categoryCount); // Notify for all category items added
                    Log.d(TAG, "Data load complete. Total items: " + items.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching Categories", e);
                    // Data load finished, even if categories failed
                });
    }

    /**
     * Determines the appropriate greeting ("Good morning", "Good afternoon", etc.)
     * based on the current hour of the day.
     * @return The greeting string.
     */
    private String getGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY); // Hour in 24-hour format

        if (timeOfDay >= 5 && timeOfDay < 12) {        // 5:00 AM to 11:59 AM
            return "Good morning";
        } else if (timeOfDay >= 12 && timeOfDay < 17) { // 12:00 PM to 4:59 PM
            return "Good afternoon";
        } else if (timeOfDay >= 17 && timeOfDay < 21) { // 5:00 PM to 8:59 PM
            return "Good evening";
        } else {                                        // 9:00 PM to 4:59 AM
            return "Good night";
        }
    }
}