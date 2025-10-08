package com.example.recipietracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EmailSignUpActivity extends AppCompatActivity {

    private static final String TAG = "EmailSignUpActivity";
    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText;
    private Button createAccountButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        createAccountButton = findViewById(R.id.buttonCreateAccount);

        createAccountButton.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        saveUserInfo(firebaseUser, firstName, lastName);
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(EmailSignUpActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserInfo(FirebaseUser firebaseUser, String firstName, String lastName) {
        String userId = firebaseUser.getUid();
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("firstName", firstName);
        userProfile.put("lastName", lastName);
        userProfile.put("email", firebaseUser.getEmail());

        // Get the onboarding data passed from the previous activity and add it to the profile
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            userProfile.put("goal", intent.getStringExtra("USER_GOAL"));
            userProfile.put("gender", intent.getStringExtra("USER_GENDER"));
            userProfile.put("birthdate", intent.getStringExtra("USER_BIRTHDATE"));
            userProfile.put("skill", intent.getStringExtra("USER_SKILL"));
            userProfile.put("diet", intent.getStringExtra("USER_DIET"));
            userProfile.put("allergies", intent.getStringArrayListExtra("USER_ALLERGIES"));
        }

        db.collection("users").document(userId)
                .set(userProfile)
                .addOnSuccessListener(aVoid -> navigateToDashboard())
                .addOnFailureListener(e -> Toast.makeText(EmailSignUpActivity.this, "Failed to save user info.", Toast.LENGTH_SHORT).show());
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(EmailSignUpActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}