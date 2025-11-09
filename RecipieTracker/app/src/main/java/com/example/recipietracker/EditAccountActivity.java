package com.example.recipietracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditAccountActivity extends AppCompatActivity {

    private static final String TAG = "EditAccountActivity";

    // UI Views
    private MaterialToolbar toolbar;
    private ImageView profileImageView;
    private TextView changePictureButton;
    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText;
    private TextView changePasswordButton, deleteAccountButton;
    private Button saveButton;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DocumentReference userDocRef;

    // Activity Launcher for Image Picker
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadProfilePicture(imageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no user is logged in
            return;
        }
        userDocRef = db.collection("users").document(currentUser.getUid());

        // Find all views
        findViews();

        // Set up click listeners
        toolbar.setNavigationOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveProfileChanges());
        changePictureButton.setOnClickListener(v -> openGallery());
        changePasswordButton.setOnClickListener(v -> showPasswordResetDialog());
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());

        // Load the user's data
        loadUserData();
    }

    private void findViews() {
        toolbar = findViewById(R.id.toolbar);
        profileImageView = findViewById(R.id.profileImageView);
        changePictureButton = findViewById(R.id.changePictureButton);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        saveButton = findViewById(R.id.saveButton);
    }

    private void loadUserData() {
        // Load email from Auth
        emailEditText.setText(currentUser.getEmail());

        // Load profile picture from Auth
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImageView);
        }

        // Load first and last name from Firestore
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                firstNameEditText.setText(firstName);
                lastNameEditText.setText(lastName);
            } else {
                Log.w(TAG, "User document does not exist in Firestore.");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading user data from Firestore", e);
            Toast.makeText(this, "Error loading profile.", Toast.LENGTH_SHORT).show();
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void uploadProfilePicture(Uri imageUri) {
        Toast.makeText(this, "Uploading picture...", Toast.LENGTH_SHORT).show();

        // Create a unique path for the image in Firebase Storage
        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_pictures/" + currentUser.getUid() + "/" + filename);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    Log.d(TAG, "Image uploaded successfully: " + downloadUri.toString());
                    updateProfilePicture(downloadUri);
                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed", e);
                    Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfilePicture(Uri downloadUri) {
        // Update Firebase Auth profile
        currentUser.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUri)
                        .build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase Auth profile picture updated.");
                        // Also save to Firestore (optional, but good practice)
                        userDocRef.update("photoUrl", downloadUri.toString());

                        // Update the ImageView
                        Glide.with(this).load(downloadUri).circleCrop().into(profileImageView);
                        Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to update Auth profile picture", task.getException());
                        Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveProfileChanges() {
        String newFirstName = firstNameEditText.getText().toString().trim();
        String newLastName = lastNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newFirstName) || TextUtils.isEmpty(newLastName)) {
            Toast.makeText(this, "First and Last name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map of the data to update in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", newFirstName);
        updates.put("lastName", newLastName);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating profile", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showPasswordResetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setMessage("Do you want to send a password reset link to your email?")
                .setPositiveButton("Send Email", (dialog, which) -> {
                    mAuth.sendPasswordResetEmail(currentUser.getEmail())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        // This is a highly destructive action, so we use multiple confirmations.
        // For now, we'll show a simple "Are you sure?"
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you absolutely sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Implement proper re-authentication before deleting
                    // For now, we'll just show a toast
                    Toast.makeText(this, "Account deletion feature coming soon.", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "User attempted to delete account. Implement re-authentication!");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}