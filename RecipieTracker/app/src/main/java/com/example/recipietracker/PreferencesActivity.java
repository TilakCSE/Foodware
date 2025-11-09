package com.example.recipietracker;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferencesActivity extends AppCompatActivity {

    private static final String TAG = "PreferencesActivity";

    // UI Views
    private MaterialToolbar toolbar;
    private Spinner spinnerGoal, spinnerDiet, spinnerSkill;
    private CheckBox checkboxLactose, checkboxGluten, checkboxNuts;
    private Button saveButton;

    // Firebase
    private FirebaseFirestore db;
    private DocumentReference userDocRef;
    private FirebaseUser currentUser;

    // Data for Spinners
    private final String[] goals = {"Lose weight", "Eat healthy", "Gain muscle", "Other"};
    private final String[] diets = {"Flexitarian", "Vegetarian", "Vegan", "Keto", "None"};
    private final String[] skills = {"Beginner", "Middle", "Advanced"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userDocRef = db.collection("users").document(currentUser.getUid());

        // Find views
        findViews();
        setupSpinners();

        // Set up click listeners
        toolbar.setNavigationOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> savePreferences());

        // Load data
        loadUserPreferences();
    }

    private void findViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerGoal = findViewById(R.id.spinnerGoal);
        spinnerDiet = findViewById(R.id.spinnerDiet);
        spinnerSkill = findViewById(R.id.spinnerSkill);
        checkboxLactose = findViewById(R.id.checkboxLactose);
        checkboxGluten = findViewById(R.id.checkboxGluten);
        checkboxNuts = findViewById(R.id.checkboxNuts);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupSpinners() {
        // Goal Spinner
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, goals);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(goalAdapter);

        // Diet Spinner
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiet.setAdapter(dietAdapter);

        // Skill Spinner
        ArrayAdapter<String> skillAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, skills);
        skillAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSkill.setAdapter(skillAdapter);
    }

    private void loadUserPreferences() {
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Set Spinner values
                String goal = documentSnapshot.getString("goal");
                String diet = documentSnapshot.getString("diet");
                String skill = documentSnapshot.getString("skill");

                setSpinnerSelection(spinnerGoal, goals, goal);
                setSpinnerSelection(spinnerDiet, diets, diet);
                setSpinnerSelection(spinnerSkill, skills, skill);

                // Set CheckBox values
                List<String> allergies = (List<String>) documentSnapshot.get("Allergies");
                if (allergies != null) {
                    checkboxLactose.setChecked(allergies.contains("Lactose"));
                    checkboxGluten.setChecked(allergies.contains("Gluten"));
                    checkboxNuts.setChecked(allergies.contains("Nuts"));
                }

            } else {
                Log.w(TAG, "User document does not exist.");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error loading user preferences", e));
    }

    private void savePreferences() {
        // Get values from Spinners
        String newGoal = spinnerGoal.getSelectedItem().toString();
        String newDiet = spinnerDiet.getSelectedItem().toString();
        String newSkill = spinnerSkill.getSelectedItem().toString();

        // Get values from CheckBoxes
        List<String> newAllergies = new ArrayList<>();
        if (checkboxLactose.isChecked()) newAllergies.add("Lactose");
        if (checkboxGluten.isChecked()) newAllergies.add("Gluten");
        if (checkboxNuts.isChecked()) newAllergies.add("Nuts");

        // Create a map to update Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("goal", newGoal);
        updates.put("diet", newDiet);
        updates.put("skill", newSkill);
        updates.put("Allergies", newAllergies); // Overwrite the old array with the new one

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving preferences", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Helper method to find the index of a value in an array and set the spinner.
     */
    private void setSpinnerSelection(Spinner spinner, String[] array, String value) {
        if (value != null) {
            int index = Arrays.asList(array).indexOf(value);
            if (index >= 0) {
                spinner.setSelection(index);
            }
        }
    }
}