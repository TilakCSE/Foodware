package com.example.recipietracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddRecipeActivity extends AppCompatActivity {
    private static final int REQ_PICK_IMAGE = 101;
    private Uri pickedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etIngredients = findViewById(R.id.etIngredients);
        EditText etSteps = findViewById(R.id.etSteps);
        EditText etCuisine = findViewById(R.id.etCuisine);
        EditText etPrep = findViewById(R.id.etPrep);
        EditText etCook = findViewById(R.id.etCook);
        EditText etServings = findViewById(R.id.etServings);
        EditText etDifficulty = findViewById(R.id.etDifficulty);
        ImageView imgPreview = findViewById(R.id.imgPreview);
        Button btnPickImage = findViewById(R.id.btnPickImage);
        Button btnSave = findViewById(R.id.btnSave);

        DBHelper db = new DBHelper(this);

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Image"), REQ_PICK_IMAGE);
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String ingredients = etIngredients.getText().toString().trim();
            String steps = etSteps.getText().toString().trim();
            String cuisine = etCuisine.getText().toString().trim();
            int prep = safeParseInt(etPrep.getText().toString());
            int cook = safeParseInt(etCook.getText().toString());
            int servings = Math.max(1, safeParseInt(etServings.getText().toString()));
            String difficulty = TextUtils.isEmpty(etDifficulty.getText()) ? "Easy" : etDifficulty.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(ingredients) || TextUtils.isEmpty(steps)) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Recipe r = new Recipe(0, title, ingredients, steps, cuisine, false);
            if (pickedImageUri != null) r.setImageUri(pickedImageUri.toString());
            r.setPrepMinutes(prep);
            r.setCookMinutes(cook);
            r.setServings(servings);
            r.setDifficulty(difficulty);
            db.addRecipe(r);
            Toast.makeText(this, "Recipe saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            pickedImageUri = data.getData();
            ImageView imgPreview = findViewById(R.id.imgPreview);
            if (pickedImageUri != null) {
                imgPreview.setVisibility(ImageView.VISIBLE);
                imgPreview.setImageURI(pickedImageUri);
            }
        }
    }

    private int safeParseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
}



