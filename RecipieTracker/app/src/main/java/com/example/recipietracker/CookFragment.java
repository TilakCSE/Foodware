package com.example.recipietracker;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CookFragment extends Fragment {

    private static final String TAG = "CookFragment"; // For logging

    private RecyclerView recyclerView;
    private CookAdapter adapter;
    private List<Object> items = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cook, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.cookRecyclerView);

        setupRecyclerView();
        loadAllData(); // Load data from Firestore
    }

    /**
     * Sets up the RecyclerView with the GridLayoutManager and the CookAdapter.
     */
    private void setupRecyclerView() {
        adapter = new CookAdapter(items); // Initialize adapter with the (currently empty) list
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2); // 2 columns

        // Configure SpanSizeLookup to make categories span 1 column, others span 2
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Defensive check in case adapter/items are not ready
                if (adapter == null || position < 0 || position >= adapter.getItemCount()) {
                    return 2; // Default to full width
                }
                // Ask the adapter for the view type at this position
                switch (adapter.getItemViewType(position)) {
                    case CookAdapter.VIEW_TYPE_CATEGORY_GRID:
                        return 1; // Category items take 1 span (half width)
                    default:
                        return 2; // All other items take 2 spans (full width)
                }
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Fetches data sequentially from Firestore to ensure the correct order in the list.
     */
    private void loadAllData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // If user is not logged in, don't attempt to load personalized data
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user found.");
            // Optionally show a message or load non-personalized data
            items.clear();
            items.add("SEARCH"); // Still show search
            // Maybe fetch public recipes/categories here if needed
            adapter.notifyDataSetChanged();
            return;
        }

        // Clear the list before starting fetches
        items.clear();
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Starting data load...");

        // Chain the Firestore calls using Task continuations or nested listeners
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    // --- 1. User Greeting is fetched ---
                    Log.d(TAG, "User document fetched successfully.");
                    String name = "User"; // Default name
                    if (userDoc.exists()) {
                        String fetchedName = userDoc.getString("firstName");
                        if (fetchedName != null && !fetchedName.isEmpty()) {
                            name = fetchedName;
                        }
                    }
                    String greeting = getGreeting() + ", " + name + "!";
                    items.add(new GreetingItem(greeting));
                    items.add("SEARCH");
                    // Notify immediately for the first two items
                    adapter.notifyItemRangeInserted(0, 2);

                    // --- 2. Now, fetch New Recipes ---
                    fetchNewRecipes();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user document", e);
                    // Handle failure: Add default greeting and continue
                    String greeting = getGreeting() + "!";
                    items.add(new GreetingItem(greeting));
                    items.add("SEARCH");
                    adapter.notifyItemRangeInserted(0, 2);
                    // Still try to fetch the rest of the data
                    fetchNewRecipes();
                });
    }

    /** Fetches the "New Recipes" list */
    private void fetchNewRecipes() {
        items.add("New recipes");
        int headerPos = items.size() - 1; // Position of the header
        adapter.notifyItemInserted(headerPos);

        db.collection("recipes").limit(5).get()
                .addOnSuccessListener(recipesSnaps -> {
                    Log.d(TAG, "New Recipes fetched successfully: " + recipesSnaps.size() + " items.");
                    List<RecipeItem> newRecipes = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : recipesSnaps) {
                        newRecipes.add(doc.toObject(RecipeItem.class));
                    }
                    items.add(newRecipes); // Add the list of recipes
                    adapter.notifyItemInserted(headerPos + 1); // Notify for the list row

                    // --- 3. Now, fetch Community Posts ---
                    fetchCommunityPosts();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching New Recipes", e);
                    // Continue to next section even if this fails
                    fetchCommunityPosts();
                });
    }

    /** Fetches the "Community" posts list */
    private void fetchCommunityPosts() {
        items.add("Community");
        int headerPos = items.size() - 1;
        adapter.notifyItemInserted(headerPos);

        db.collection("community_posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(5).get()
                .addOnSuccessListener(communitySnaps -> {
                    Log.d(TAG, "Community Posts fetched successfully: " + communitySnaps.size() + " items.");
                    List<CommunityPostItem> communityItems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : communitySnaps) {
                        communityItems.add(doc.toObject(CommunityPostItem.class));
                    }
                    items.add(communityItems);
                    adapter.notifyItemInserted(headerPos + 1);

                    // --- 4. Finally, fetch Categories ---
                    fetchCategories();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching Community Posts", e);
                    fetchCategories(); // Continue anyway
                });
    }

    /** Fetches the "Categories" grid items */
    private void fetchCategories() {
        items.add("Categories");
        int headerPos = items.size() - 1;
        adapter.notifyItemInserted(headerPos);

        db.collection("categories").get()
                .addOnSuccessListener(categorySnaps -> {
                    Log.d(TAG, "Categories fetched successfully: " + categorySnaps.size() + " items.");
                    int categoryCount = 0;
                    for (QueryDocumentSnapshot doc : categorySnaps) {
                        items.add(doc.toObject(CategoryItem.class));
                        categoryCount++;
                    }
                    adapter.notifyItemRangeInserted(headerPos + 1, categoryCount);
                    Log.d(TAG, "Data load complete. Total items: " + items.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching Categories", e);
                    // Data load finished, even if categories failed
                });
    }

    /**
     * Determines the appropriate greeting based on the current time of day.
     */
    private String getGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 5 && timeOfDay < 12) { // 5 AM to 11:59 AM
            return "Good morning";
        } else if (timeOfDay >= 12 && timeOfDay < 17) { // 12 PM to 4:59 PM
            return "Good afternoon";
        } else if (timeOfDay >= 17 && timeOfDay < 21) { // 5 PM to 8:59 PM
            return "Good evening";
        } else { // 9 PM to 4:59 AM
            return "Good night";
        }
    }
}