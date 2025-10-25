package com.example.recipietracker;

import android.Manifest; // Import Manifest
import android.content.Intent;
import android.content.pm.PackageManager; // Import PackageManager
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText; // Using EditText as TextInputEditText isn't strictly needed here
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import ContextCompat
import androidx.core.content.FileProvider; // Import FileProvider

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File; // Import File
import java.io.IOException; // Import IOException
import java.text.SimpleDateFormat; // Import SimpleDateFormat
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date; // Import Date
import java.util.HashMap;
import java.util.List;
import java.util.Locale; // Import Locale
import java.util.Map;

public class AddOwnRecipeActivity extends AppCompatActivity {

    private static final String TAG = "AddOwnRecipeActivity";

    // UI Elements
    private EditText titleEditText, descriptionEditText, ingredientsEditText, instructionsEditText, tipsEditText;
    private ImageView recipeImageView;
    private TextView servingsTextView, timeTextView;
    private Button servingsMinus, servingsPlus, timeMinus, timePlus, saveButton;
    private MaterialButtonToggleGroup difficultyGroup;
    private RadioGroup occasionGroup;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // State variables
    private int currentServings = 2;
    private int currentTime = 0;
    private Uri selectedImageUri = null; // URI of the selected/captured image
    private Uri cameraImageUri = null; // Temp URI for the camera to write to

    // --- ActivityResultLaunchers ---
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Log.d(TAG, "Gallery image selected: " + selectedImageUri);
                    Glide.with(this).load(selectedImageUri).into(recipeImageView); // Display selected image
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Image captured and saved to cameraImageUri
                    selectedImageUri = cameraImageUri; // Use the URI we provided
                    Log.d(TAG, "Camera image captured: " + selectedImageUri);
                    Glide.with(this).load(selectedImageUri).into(recipeImageView);
                } else {
                    Log.d(TAG, "Camera capture cancelled or failed");
                    // Optionally delete the temporary file if capture failed/cancelled
                    if (cameraImageUri != null) {
                        // File fdelete = new File(cameraImageUri.getPath()); // This path might not work directly
                        // if (fdelete.exists()) fdelete.delete(); // Needs careful path handling
                    }
                }
            });

    // Launcher for Camera Permission request
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted, launch camera
                    launchCameraIntent();
                } else {
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_own_recipe);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        findViews();
        setupIncrementDecrementButtons();
        updateServingsText();
        updateTimeText();
        recipeImageView.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> saveRecipeToFirestore());
    }

    /**
     * Finds and assigns all the UI elements from the layout file.
     */
    private void findViews() {
        titleEditText = findViewById(R.id.recipeTitleEditText);
        recipeImageView = findViewById(R.id.recipeImageView);
        descriptionEditText = findViewById(R.id.recipeDescriptionEditText);
        servingsTextView = findViewById(R.id.servingsTextView);
        servingsMinus = findViewById(R.id.buttonServingsMinus);
        servingsPlus = findViewById(R.id.buttonServingsPlus);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        instructionsEditText = findViewById(R.id.instructionsEditText);
        tipsEditText = findViewById(R.id.tipsEditText);
        difficultyGroup = findViewById(R.id.toggleGroupDifficulty);
        timeTextView = findViewById(R.id.timeTextView);
        timeMinus = findViewById(R.id.buttonTimeMinus);
        timePlus = findViewById(R.id.buttonTimePlus);
        occasionGroup = findViewById(R.id.radioGroupOccasion);
        saveButton = findViewById(R.id.buttonSaveRecipe);
    }

    /**
     * Sets up the click listeners for the +/- buttons for servings and time.
     */
    private void setupIncrementDecrementButtons() {
        servingsMinus.setOnClickListener(v -> {
            if (currentServings > 1) {
                currentServings--;
                updateServingsText();
            }
        });
        servingsPlus.setOnClickListener(v -> {
            currentServings++;
            updateServingsText();
        });
        timeMinus.setOnClickListener(v -> {
            if (currentTime >= 5) {
                currentTime -= 5;
                updateTimeText();
            } else {
                currentTime = 0;
                updateTimeText();
            }
        });
        timePlus.setOnClickListener(v -> {
            currentTime += 5;
            updateTimeText();
        });
    }

    /**
     * Updates the TextView displaying the number of servings.
     */
    private void updateServingsText() {
        servingsTextView.setText(currentServings + (currentServings == 1 ? " serving" : " servings"));
    }

    /**
     * Updates the TextView displaying the cooking time in minutes.
     */
    private void updateTimeText() {
        timeTextView.setText(currentTime + " minutes");
    }

    /**
     * Creates and launches an Intent chooser for selecting an image from the gallery or camera.
     */
    private void openImageChooser() {
        // Create intents for Gallery and Camera
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*"); // Ensure only images are selectable

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile(); // Create a temporary file
        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file for camera", ex);
            Toast.makeText(this, "Could not prepare camera.", Toast.LENGTH_SHORT).show();
        }

        Uri tempCameraUri = null;
        if (photoFile != null) {
            // Get a content URI using FileProvider
            tempCameraUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider", // Matches authorities in manifest
                    photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri); // Tell camera where to save
        }

        // Store the temporary URI for the camera result callback
        cameraImageUri = tempCameraUri;

        // --- Create Chooser ---
        Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Recipe Picture");

        // Add camera intent to the chooser only if it's available and file was created
        if (cameraIntent.resolveActivity(getPackageManager()) != null && cameraImageUri != null) {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });
        }

        // Use the gallery launcher for the chooser result
        galleryLauncher.launch(chooserIntent);
    }


    /**
     * Creates a temporary, uniquely named image file in the app's cache directory.
     * @return The created File object.
     * @throws IOException If file creation fails.
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // Use the cache directory for temporary files
        File storageDir = getCacheDir(); // Use internal cache
        File imageDir = new File(storageDir, "images"); // Subdirectory
        if (!imageDir.exists()) {
            imageDir.mkdirs(); // Create the directory if it doesn't exist
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                imageDir        /* directory */
        );
        return image;
    }

    /**
     * Launches the camera intent directly, used after permission is granted.
     */
    private void launchCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) { Log.e(TAG, "Error creating image file", ex); return; }

        if (photoFile != null) {
            cameraImageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            // Check if there's an app to handle the camera intent
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(cameraIntent); // Use the dedicated camera launcher
            } else {
                Toast.makeText(this, "No camera app found.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Gathers all recipe data from the UI, validates it, and saves it to Firestore.
     */
    private void saveRecipeToFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to save recipes.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Get Text Data ---
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String ingredientsRaw = ingredientsEditText.getText().toString().trim();
        String instructionsRaw = instructionsEditText.getText().toString().trim();
        String tips = tipsEditText.getText().toString().trim();

        // --- Basic Validation ---
        if (TextUtils.isEmpty(title)) {
            titleEditText.setError("Title is required"); titleEditText.requestFocus(); return;
        }
        if (TextUtils.isEmpty(ingredientsRaw)) {
            ingredientsEditText.setError("Ingredients are required"); ingredientsEditText.requestFocus(); return;
        }

        // --- Get Structured Data ---
        String difficulty = getSelectedDifficulty();
        String occasion = getSelectedOccasion();
        List<Map<String, String>> ingredientsList = parseIngredients(ingredientsRaw);
        List<String> instructionsList = parseInstructions(instructionsRaw);

        // --- Create Map for Firestore ---
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("userId", currentUser.getUid());
        recipeData.put("authorName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous");
        recipeData.put("title", title);
        recipeData.put("description", description);
        recipeData.put("servings", currentServings);
        recipeData.put("ingredients", ingredientsList);
        recipeData.put("instructions", instructionsList);
        recipeData.put("tips", tips);
        recipeData.put("difficulty", difficulty);
        recipeData.put("timeMinutes", currentTime);
        recipeData.put("occasion", occasion);
        recipeData.put("imageUrl", selectedImageUri != null ? selectedImageUri.toString() : null); // Store URI string
        recipeData.put("timestamp", FieldValue.serverTimestamp());

        // --- Save to Firestore Subcollection ---
        db.collection("users").document(currentUser.getUid())
                .collection("my_recipes")
                .add(recipeData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Recipe saved with ID: " + documentReference.getId());
                    Toast.makeText(AddOwnRecipeActivity.this, "Recipe saved!", Toast.LENGTH_SHORT).show();
                    // TODO: Here you would start the image upload process using the documentReference.getId() and selectedImageUri
                    finish(); // Go back for now
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving recipe document", e);
                    Toast.makeText(AddOwnRecipeActivity.this, "Error saving recipe details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // --- Helper Methods to Get Selections ---
    private String getSelectedDifficulty() {
        int checkedId = difficultyGroup.getCheckedButtonId();
        if (checkedId == R.id.buttonEasy) return "Easy";
        if (checkedId == R.id.buttonMedium) return "Medium";
        if (checkedId == R.id.buttonHigh) return "High";
        return null;
    }

    private String getSelectedOccasion() {
        int checkedId = occasionGroup.getCheckedRadioButtonId();
        // Use the text from the RadioButton directly if available
        if (checkedId != -1) {
            RadioButton selectedRadioButton = findViewById(checkedId);
            return selectedRadioButton.getText().toString();
        }
        return null;
    }

    /** Parses ingredients text (one per line) into a list of maps. */
    private List<Map<String, String>> parseIngredients(String rawIngredients) {
        List<Map<String, String>> ingredientList = new ArrayList<>();
        if (TextUtils.isEmpty(rawIngredients)) return ingredientList;

        String[] lines = rawIngredients.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String quantity = "";
            String unit = "";
            String name = line; // Default to whole line as name

            // Try to split into quantity, unit, name (simple approach)
            String[] parts = line.split("\\s+", 3);
            if (parts.length > 0) {
                // Check if first part is numeric (simplistic check)
                if (parts[0].matches("^[0-9./]+$")) {
                    quantity = parts[0];
                    if (parts.length > 1) {
                        // Check if second part is a common unit (can be expanded)
                        String potentialUnit = parts[1].toLowerCase();
                        List<String> commonUnits = Arrays.asList("cup", "cups", "tsp", "tbsp", "oz", "g", "kg", "ml", "l", "unit", "units", "pinch", "dash");
                        if (commonUnits.contains(potentialUnit)) {
                            unit = parts[1];
                            name = (parts.length > 2) ? parts[2] : ""; // Rest is name
                        } else {
                            // Assume unit is part of the name
                            name = (parts.length > 1) ? parts[1] : "";
                            if(parts.length > 2) name += " " + parts[2];
                        }
                    } else {
                        name = ""; // Only quantity? Treat as error or just quantity
                    }
                }
                // If first part is not numeric, the whole line remains the name
            }

            Map<String, String> ingredientMap = new HashMap<>();
            ingredientMap.put("name", name.trim());
            ingredientMap.put("quantity", quantity.trim());
            ingredientMap.put("unit", unit.trim());
            ingredientList.add(ingredientMap);
        }
        return ingredientList;
    }

    /** Parses instructions text (one per line) into a list of strings. */
    private List<String> parseInstructions(String rawInstructions) {
        List<String> instructionList = new ArrayList<>();
        if (TextUtils.isEmpty(rawInstructions)) return instructionList;
        String[] lines = rawInstructions.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            // Optional: remove leading numbers/bullets if desired
            // line = line.replaceFirst("^[0-9]+\\.\\s*", "");
            if (!line.isEmpty()) {
                instructionList.add(line);
            }
        }
        return instructionList;
    }
}