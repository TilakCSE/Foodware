package com.example.recipietracker;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewListActivity extends AppCompatActivity {

    private static final String TAG = "ViewListActivity";

    private RecyclerView recyclerView;
    private SavedRecipeAdapter adapter;
    private List<Map<String, Object>> recipeRefList = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);

        String listId = getIntent().getStringExtra("LIST_ID");
        String listName = getIntent().getStringExtra("LIST_NAME");

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(listName != null ? listName : "My List");
        toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();

        if (listId != null && currentUserId != null) {
            loadRecipesFromList(listId);
        } else {
            Log.e(TAG, "List ID or User ID is null.");
            Toast.makeText(this, "Could not load list.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.listRecipesRecyclerView);
        adapter = new SavedRecipeAdapter(this, recipeRefList, db);
        recyclerView.setAdapter(adapter);
    }

    private void loadRecipesFromList(String listId) {
        db.collection("users").document(currentUserId).collection("my_lists").document(listId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> fetchedList = (List<Map<String, Object>>) documentSnapshot.get("recipes");
                        if (fetchedList != null) {
                            Log.d(TAG, "Found " + fetchedList.size() + " recipes in list.");
                            recipeRefList.clear();
                            recipeRefList.addAll(fetchedList);
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading list document", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}