package com.example.recipietracker;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommunityFeedActivity extends AppCompatActivity {

    private static final String TAG = "CommunityFeedActivity";

    private RecyclerView recyclerView;
    private CommunityFeedAdapter adapter;
    private List<CommunityPostItem> postList = new ArrayList<>();

    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener; // To stop listening later

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_feed);

        // TODO: Add a Toolbar/ActionBar here with a back button

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        loadCommunityPosts();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.communityRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommunityFeedAdapter(postList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Attaches a real-time snapshot listener to the 'community_posts' collection.
     * This will automatically update the UI whenever data is added, changed,
     * or removed in Firestore.
     */
    private void loadCommunityPosts() {
        // Query the collection, ordering by 'timestamp' in descending order (newest first)
        // This uses the 'timestamp' field from your screenshot
        firestoreListener = db.collection("community_posts")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value == null) {
                        Log.w(TAG, "Snapshot value is null.");
                        return;
                    }

                    Log.d(TAG, "New data received. " + value.size() + " documents.");

                    // Clear the old list before adding new data
                    postList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            // Convert the document to our CommunityPostItem object
                            CommunityPostItem post = doc.toObject(CommunityPostItem.class);
                            postList.add(post);
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting document to object", e);
                        }
                    }

                    // Tell the adapter that the data has changed
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Stop listening for updates when the Activity is destroyed
     * to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove(); // Detach the listener
        }
    }
}