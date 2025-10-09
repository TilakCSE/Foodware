package com.example.recipietracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

// Facebook SDK imports
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

// Google and Firebase imports
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager; // For Facebook
    private Map<String, Object> onboardingData = new HashMap<>();

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign in failed", e);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Receive onboarding data from intent
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            onboardingData.put("goal", intent.getStringExtra("USER_GOAL"));
            onboardingData.put("gender", intent.getStringExtra("USER_GENDER"));
            onboardingData.put("birthdate", intent.getStringExtra("USER_BIRTHDATE"));
            onboardingData.put("skill", intent.getStringExtra("USER_SKILL"));
            onboardingData.put("diet", intent.getStringExtra("USER_DIET"));
            onboardingData.put("allergies", intent.getStringArrayListExtra("USER_ALLERGIES"));
        }

        // --- Configure Google Sign-In ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- Configure Facebook Sign-In ---
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult);
            }
            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                Toast.makeText(SignUpActivity.this, "Facebook login canceled.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                Toast.makeText(SignUpActivity.this, "Facebook login error.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Set Click Listeners ---
        findViewById(R.id.buttonSignUpGoogle).setOnClickListener(v -> signInWithGoogle());
        findViewById(R.id.buttonSignUpFacebook).setOnClickListener(v -> signInWithFacebook());
        findViewById(R.id.buttonSignUpEmail).setOnClickListener(v -> {
            Intent emailIntent = new Intent(this, EmailSignUpActivity.class);
            if (intent.getExtras() != null) {
                emailIntent.putExtras(intent.getExtras());
            }
            startActivity(emailIntent);
        });

        // --- ADD THIS BLOCK FOR NAVIGATION ---
        findViewById(R.id.textViewSignIn).setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        });
    }

    // This method is required to pass the result back to the Facebook SDK
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                saveUserInfo(mAuth.getCurrentUser());
            } else {
                Toast.makeText(this, "Google Auth Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(LoginResult loginResult) {
        AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                saveUserInfo(mAuth.getCurrentUser());
            } else {
                Toast.makeText(this, "Facebook Auth Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            String name = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();

            Map<String, Object> userProfile = new HashMap<>(onboardingData);
            userProfile.put("name", name);
            userProfile.put("email", email);

            db.collection("users").document(userId)
                    .set(userProfile)
                    .addOnSuccessListener(aVoid -> navigateToDashboard())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save profile.", Toast.LENGTH_SHORT).show());
        }
    }

    // In SignUpActivity.java

    private void navigateToDashboard() {
        // Change DashboardActivity.class to MainActivity.class
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}