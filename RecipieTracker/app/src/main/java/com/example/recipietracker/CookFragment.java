package com.example.recipietracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast; // Import Toast
 // Import Query

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Arrays; // Import Arrays
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Fragment for the main "Cook" screen, displaying various sections like
 * greeting, search, new recipes, community posts, and categories.
 * Implements OnRecipeClickListener to handle clicks on recipe cards.
 * Implements OnSectionClickListener for "See all" clicks.
 * Implements OnSearchQueryListener to handle live search.
 */
public class CookFragment extends Fragment
        implements HorizontalRecipeAdapter.OnRecipeClickListener,
        CookAdapter.OnSectionClickListener,
        CookAdapter.OnSearchQueryListener { // Make sure this is implemented

    private static final String TAG = "CookFragment"; // Tag for logging

    // UI Elements
    private RecyclerView recyclerView;

    // Adapters and Data
    private CookAdapter adapter;
    private List<Object> items = new ArrayList<>(); // List holding all row data (mixed types)

    // Firebase
    private FirebaseFirestore db;

    private List<Object> dashboardCache = new ArrayList<>();
    private boolean isSearchActive = false;
    private String lastQuery = "";
    private String userSkill; // e.g., "Middle"
    private String userGoal;


    /**
     * Called to have the fragment instantiate its user interface view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cook, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored into the view.
     * Initializes views, sets up RecyclerView, and starts data loading.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.cookRecyclerView);
        setupRecyclerView();
        loadAllData();
    }

    // --- THIS IS THE NEW, CORRECTED SEARCH LISTENER ---
    /**
     * Called by the CookAdapter whenever the text in the search bar changes.
     * @param query The current text in the search bar.
     */
    @Override
    public void onSearchQueryChanged(String query) {
        final String trimmedQuery = query.trim();
        lastQuery = trimmedQuery;
        if (trimmedQuery.isEmpty()) {
            restoreDashboard();
        } else {
            isSearchActive = true;
            performSearch(trimmedQuery);
        }
    }
    // --- END NEW SEARCH LISTENER ---

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
        intent.putExtra("RECIPE_ID", recipeId);
        intent.putExtra("USER_ID", (String) null); // Public recipe
        startActivity(intent);
    }

    // --- (This section is for "See All" clicks) ---
    @Override
    public void onCommunitySeeAllClick() {
        Log.d(TAG, "Community 'See all' clicked!");
        Intent intent = new Intent(getActivity(), CommunityFeedActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCategoriesSeeAllClick() {
        Log.d(TAG, "Categories 'See all' clicked!");
        // This launches "All Categories" mode
        Intent intent = new Intent(getActivity(), CategoriesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCategoryItemClick(CategoryItem category) {
        Log.d(TAG, "Category item clicked: " + category.getTitle());
        // This launches "Specific Category" mode
        Intent intent = new Intent(getActivity(), CategoriesActivity.class);
        intent.putExtra("CATEGORY_NAME", category.getTitle());
        startActivity(intent);
    }
    // --- END "See All" section ---

    /**
     * Sets up the RecyclerView with a GridLayoutManager configured for multiple span sizes
     * and initializes the CookAdapter.
     */
    private void setupRecyclerView() {
        // Initialize the adapter with all four listeners
        adapter = new CookAdapter(items, this, this, this);

        // Use a GridLayoutManager with 2 columns as the base layout
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter == null || position < 0 || position >= adapter.getItemCount()) {
                    return 2;
                }
                switch (adapter.getItemViewType(position)) {
                    case CookAdapter.VIEW_TYPE_CATEGORY_GRID:
                        return 1;
                    default:
                        return 2;
                }
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Orchestrates the fetching of all data sections from Firestore sequentially
     * to maintain the correct display order. Starts by fetching the user's name.
     */
    private void loadAllData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user found.");
            items.clear();
            items.add("SEARCH"); // Still show search bar
            adapter.notifyDataSetChanged();
            return;
        }

        items.clear();
        dashboardCache.clear(); // Clear cache
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Starting data load for user: " + currentUser.getUid());

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    Log.d(TAG, "User document fetched successfully.");
                    String name = "User";
                    if (userDoc.exists()) {
                        String fetchedName = userDoc.getString("firstName");
                        if (fetchedName != null && !fetchedName.isEmpty()) {
                            name = fetchedName;
                        }

                        // --- 1. LOAD PREFERENCES ---
                        userSkill = userDoc.getString("skill"); // e.g., "Middle"
                        userGoal = userDoc.getString("goal");   // e.g., "Lose weight"
                        Log.d(TAG, "User prefs loaded: Skill=" + userSkill + ", Goal=" + userGoal);
                        // --- END LOAD PREFERENCES ---

                    } else {
                        Log.w(TAG, "User document does not exist");
                    }
                    String greeting = getGreeting() + ", " + name + "!";
                    items.add(new GreetingItem(greeting));
                    items.add("SEARCH");

                    dashboardCache.add(items.get(0));
                    dashboardCache.add(items.get(1));

                    adapter.notifyItemRangeInserted(0, 2);
                    fetchNewRecipes(true); // Start chain, store results in cache
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user document for greeting", e);
                    String greeting = getGreeting() + "!";
                    items.add(new GreetingItem(greeting));
                    items.add("SEARCH");
                    dashboardCache.add(items.get(0));
                    dashboardCache.add(items.get(1));
                    adapter.notifyItemRangeInserted(0, 2);
                    fetchNewRecipes(true); // Start chain anyway
                });
    }

    /** Fetches the "New Recipes" list. */
    private void fetchNewRecipes(boolean cacheData) {
        int headerPos = items.size();
        items.add("New recipes");
        if (cacheData) dashboardCache.add(items.get(headerPos));
        adapter.notifyItemInserted(headerPos);

        db.collection("recipes").orderBy("timestamp", Query.Direction.DESCENDING).limit(5).get()
                .addOnSuccessListener(recipesSnaps -> {
                    List<RecipeItem> newRecipes = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : recipesSnaps) {
                        newRecipes.add(doc.toObject(RecipeItem.class));
                    }
                    items.add(newRecipes);
                    if (cacheData) dashboardCache.add(items.get(headerPos + 1));
                    adapter.notifyItemInserted(headerPos + 1);
                    fetchCommunityPosts(cacheData); // Chain next fetch
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching New Recipes", e);
                    items.add(new ArrayList<RecipeItem>());
                    if (cacheData) dashboardCache.add(items.get(headerPos + 1));
                    adapter.notifyItemInserted(headerPos + 1);
                    fetchCommunityPosts(cacheData); // Chain next fetch
                });
    }

    /** Fetches the "Community" posts list. */
    private void fetchCommunityPosts(boolean cacheData) {
        int headerPos = items.size();
        items.add("Community");
        if (cacheData) dashboardCache.add(items.get(headerPos));
        adapter.notifyItemInserted(headerPos);

        db.collection("community_posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(5).get()
                .addOnSuccessListener(communitySnaps -> {
                    List<CommunityPostItem> communityItems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : communitySnaps) {
                        communityItems.add(doc.toObject(CommunityPostItem.class));
                    }
                    items.add(communityItems);
                    if (cacheData) dashboardCache.add(items.get(headerPos + 1));
                    adapter.notifyItemInserted(headerPos + 1);
                    fetchCategories(cacheData); // Chain next fetch
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching Community Posts", e);
                    items.add(new ArrayList<CommunityPostItem>());
                    if (cacheData) dashboardCache.add(items.get(headerPos + 1));
                    adapter.notifyItemInserted(headerPos + 1);
                    fetchCategories(cacheData); // Chain next fetch
                });
    }

    /** Fetches the "Categories" grid items. */
    private void fetchCategories(boolean cacheData) {
        int headerPos = items.size();
        items.add("Categories");
        if (cacheData) dashboardCache.add(items.get(headerPos));
        adapter.notifyItemInserted(headerPos);

        db.collection("categories").get()
                .addOnSuccessListener(categorySnaps -> {
                    int categoryStartPos = headerPos + 1;
                    int categoryCount = 0;
                    for (QueryDocumentSnapshot doc : categorySnaps) {
                        CategoryItem catItem = doc.toObject(CategoryItem.class);
                        items.add(catItem);
                        if (cacheData) dashboardCache.add(catItem);
                        categoryCount++;
                    }
                    adapter.notifyItemRangeInserted(categoryStartPos, categoryCount);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching Categories", e));
    }

    // --- THIS IS THE NEW, CORRECTED SEARCH METHOD ---
    /**
     * Clears the list and performs a Firestore search for recipes
     * using the 'search_prefixes' array.
     * @param query The text to search for (e.g., "e", "eg", "egg")
     */
    private void performSearch(final String query) {
        // Clear everything *except* the Greeting and Search Bar
        int oldSize = items.size();
        if (oldSize > 2) {
            items.subList(2, oldSize).clear();
            adapter.notifyItemRangeRemoved(2, oldSize - 2);
        }

        // 1. Convert the query to lowercase (it's already trimmed)
        final String lowercaseQuery = query.toLowerCase();

        // 2. Use the 'whereArrayContains' query on the 'search_prefixes' field
        Query searchQuery = db.collection("recipes")
                .whereArrayContains("search_prefixes", lowercaseQuery);

        // 2. Add Skill filter
        if (userSkill != null && !userSkill.isEmpty()) {
            if (userSkill.equals("Beginner")) {
                searchQuery = searchQuery.whereEqualTo("difficulty", "Easy");
            } else if (userSkill.equals("Middle")) {
                searchQuery = searchQuery.whereIn("difficulty", Arrays.asList("Easy", "Medium"));
            }
            // If "Advanced", we don't filter by difficulty
        }

        // 3. Add Goal (Calorie) filter
        if (userGoal != null && userGoal.equals("Lose weight")) {
            // We set an upper bound on calories
            searchQuery = searchQuery.whereLessThanOrEqualTo("calories", 500);
        }
        // You could add more else-if blocks here, e.g.:
        // else if (userGoal.equals("Gain muscle")) {
        //     searchQuery = searchQuery.whereGreaterThanOrEqualTo("calories", 600);
        // }

        // 4. Set the limit and execute the query
        searchQuery.limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isSearchActive || !lowercaseQuery.equals(lastQuery.toLowerCase())) {
                        return; // Old results, ignore
                    }

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No search results found with filters.");
                        // Optional: show a "no results" message
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        RecipeItem item = doc.toObject(RecipeItem.class);
                        items.add(item);
                        adapter.notifyItemInserted(items.size() - 1);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error performing filtered search", e);
                    // This error is special. It's likely a missing index.
                    Toast.makeText(getContext(), "Search failed. Check Firestore index.", Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Restores the main dashboard from the cache when search is cleared.
     */
    private void restoreDashboard() {
        if (!isSearchActive && !items.isEmpty()) return; // Already restored
        isSearchActive = false;
        lastQuery = "";
        items.clear();
        items.addAll(dashboardCache);
        adapter.notifyDataSetChanged();
    }
    // --- END NEW SEARCH METHODS ---


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