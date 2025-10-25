package com.example.recipietracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateListActivity extends AppCompatActivity {

    private TextInputEditText listNameEditText;
    private Button createButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        listNameEditText = findViewById(R.id.listNameEditText);
        createButton = findViewById(R.id.buttonCreateListAction);

        createButton.setOnClickListener(v -> createListInFirestore());
    }

    private void createListInFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String listName = listNameEditText.getText().toString().trim();
        if (listName.isEmpty()) {
            listNameEditText.setError("List name cannot be empty");
            return;
        }

        Map<String, Object> listData = new HashMap<>();
        listData.put("listName", listName);
        listData.put("recipeIds", new ArrayList<String>()); // Start with empty recipe list
        listData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("users").document(currentUser.getUid())
                .collection("my_lists")
                .add(listData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateListActivity.this, "List created!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to profile
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateListActivity.this, "Error creating list: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}