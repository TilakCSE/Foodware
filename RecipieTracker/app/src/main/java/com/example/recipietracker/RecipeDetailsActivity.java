package com.example.recipietracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailsActivity extends AppCompatActivity {
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        long id = getIntent().getLongExtra("id", -1);
        DBHelper db = new DBHelper(this);
        for (Recipe r : db.getAllRecipes()) {
            if (r.getId() == id) { recipe = r; break; }
        }
        if (recipe == null) { finish(); return; }

        ImageView img = findViewById(R.id.imgHero);
        TextView title = findViewById(R.id.txtTitle);
        TextView meta = findViewById(R.id.txtMeta);
        TextView ingredients = findViewById(R.id.txtIngredients);
        TextView steps = findViewById(R.id.txtSteps);
        CheckBox fav = findViewById(R.id.chkFavorite);
        Button share = findViewById(R.id.btnShare);
        Button delete = findViewById(R.id.btnDelete);

        if (recipe.getImageUri() != null) img.setImageURI(Uri.parse(recipe.getImageUri()));
        title.setText(recipe.getTitle());
        meta.setText(recipe.getPrepMinutes() + "m prep • " + recipe.getCookMinutes() + "m cook • " + recipe.getServings() + " servings • " + recipe.getDifficulty());
        ingredients.setText("Ingredients:\n" + recipe.getIngredients());
        steps.setText("Steps:\n" + recipe.getSteps());
        fav.setChecked(recipe.isFavorite());
        fav.setOnCheckedChangeListener((b, checked) -> new DBHelper(this).toggleFavorite(recipe.getId(), checked));

        share.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String body = recipe.getTitle() + "\n\n" + recipe.getIngredients() + "\n\n" + recipe.getSteps();
            intent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(intent, "Share Recipe"));
        });

        delete.setOnClickListener(v -> {
            new DBHelper(this).deleteRecipe(recipe.getId());
            finish();
        });
    }
}






