package com.example.recipietracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment"; // For logging

    private ImageView profileImageView;
    private TextView nameTextView, emailTextView;
    private TabLayout tabLayout;
    private FrameLayout tabContentContainer;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views
        profileImageView = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        tabLayout = view.findViewById(R.id.tabLayout);
        tabContentContainer = view.findViewById(R.id.tabContentContainer);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);

        loadUserProfile();
        setupTabs();

        // Handle Toolbar menu item clicks (optional)
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                Toast.makeText(getContext(), "Edit clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_settings) {
                // TODO: Add Logout functionality here
                FirebaseAuth.getInstance().signOut();
                // Navigate back to WelcomeActivity or SignInActivity
                Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    /** Fetches user info from Firebase Auth and Firestore */
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());

            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this).load(currentUser.getPhotoUrl()).circleCrop().into(profileImageView);
            } else {
                Glide.with(this).load(R.drawable.ic_profile_placeholder).circleCrop().into(profileImageView);
            }

            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            if (firstName != null && lastName != null) {
                                nameTextView.setText(firstName + " " + lastName);
                            } else { // Fallback if names missing in Firestore
                                nameTextView.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
                            }
                        } else {
                            nameTextView.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user name", e);
                        nameTextView.setText("User"); // Handle error
                    });
        } else {
            // Handle case where user is somehow null (shouldn't happen if they reached this screen)
            Log.w(TAG, "Current user is null in ProfileFragment");
        }
    }

    /** Sets up the TabLayout and its listener */
    private void setupTabs() {
        // Load the initial content (My Lists) when the fragment first loads
        inflateTabContent(R.layout.layout_profile_my_lists);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) { // My Lists selected
                    inflateTabContent(R.layout.layout_profile_my_lists);
                } else { // My Recipes selected
                    inflateTabContent(R.layout.layout_profile_my_recipes);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /** Inflates the correct layout into the FrameLayout based on the selected tab */
    private void inflateTabContent(int layoutResId) {
        tabContentContainer.removeAllViews(); // Clear previous content
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View contentView = inflater.inflate(layoutResId, tabContentContainer, false);
        tabContentContainer.addView(contentView);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return; // Exit if no user

        // Add click listeners and fetch data based on the inflated layout
        if (layoutResId == R.layout.layout_profile_my_lists) {
            contentView.findViewById(R.id.buttonCreateList).setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), CreateListActivity.class))
            );
            // Fetch and display lists from Firestore
            fetchAndDisplayLists(contentView, currentUser.getUid());

        } else if (layoutResId == R.layout.layout_profile_my_recipes) {
            contentView.findViewById(R.id.buttonCreateRecipe).setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), AddOwnRecipeActivity.class))
            );
            // Fetch and display recipes, passing the user ID for context
            fetchAndDisplayRecipes(contentView, currentUser.getUid());
        }
    }

    /** Fetches user's lists from Firestore and adds TextViews */
    private void fetchAndDisplayLists(View contentView, String userId) {
        LinearLayout listContainer = contentView.findViewById(R.id.listContainer);
        if (listContainer == null || getContext() == null) return;
        listContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext()); // Get inflater

        db.collection("users").document(userId).collection("my_lists")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            // Inflate the card layout
                            View cardView = inflater.inflate(R.layout.item_profile_list, listContainer, false);
                            TextView listName = cardView.findViewById(R.id.listNameTextView);
                            TextView recipeCount = cardView.findViewById(R.id.recipeCountTextView);

                            listName.setText(doc.getString("listName"));
                            // Get recipe count (defaults to 0 if field doesn't exist)
                            List<String> recipeIds = (List<String>) doc.get("recipeIds");
                            int count = (recipeIds != null) ? recipeIds.size() : 0;
                            recipeCount.setText(count + (count == 1 ? " recipe" : " recipes"));

                            listContainer.addView(cardView);
                        }
                    } else {
                        TextView tv = new TextView(getContext());
                        tv.setText("No lists created yet.");
                        listContainer.addView(tv);
                    }
                }) // Add failure listener if needed
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching lists", e));
    }

    /** Fetches user's recipes from Firestore and adds TextViews */
    private void fetchAndDisplayRecipes(View contentView, String userId) {
        LinearLayout recipeContainer = contentView.findViewById(R.id.recipeContainer);
        if (recipeContainer == null || getContext() == null) return;
        recipeContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        db.collection("users").document(userId).collection("my_recipes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            View cardView = inflater.inflate(R.layout.item_profile_recipe, recipeContainer, false);
                            TextView recipeTitle = cardView.findViewById(R.id.recipeTitleTextView);
                            ImageView recipeImage = cardView.findViewById(R.id.recipeImageView);

                            String title = doc.getString("title");
                            String imageUrl = doc.getString("imageUrl");
                            String recipeDocId = doc.getId(); // Get the document ID

                            recipeTitle.setText(title);
                            Glide.with(getContext())
                                    .load(imageUrl) // Load image URL
                                    .placeholder(R.drawable.placeholder_food)
                                    .error(R.drawable.placeholder_food)
                                    .into(recipeImage);

                            // --- Add Click Listener ---
                            cardView.setOnClickListener(v -> {
                                Intent intent = new Intent(getActivity(), RecipeDetailsActivity.class);
                                intent.putExtra("RECIPE_ID", recipeDocId);
                                intent.putExtra("USER_ID", userId); // Pass user ID to indicate it's a user recipe
                                startActivity(intent);
                            });
                            // -------------------------

                            recipeContainer.addView(cardView);
                        }
                    } else {
                        TextView tv = new TextView(getContext());
                        tv.setText("No recipes created yet.");
                        recipeContainer.addView(tv);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user recipes", e));
    }
}