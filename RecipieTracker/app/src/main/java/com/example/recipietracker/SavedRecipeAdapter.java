package com.example.recipietracker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Map;

public class SavedRecipeAdapter extends RecyclerView.Adapter<SavedRecipeAdapter.ViewHolder> {

    private static final String TAG = "SavedRecipeAdapter";

    private Context context;
    private List<Map<String, Object>> recipeRefList;
    private FirebaseFirestore db;

    public SavedRecipeAdapter(Context context, List<Map<String, Object>> recipeRefList, FirebaseFirestore db) {
        this.context = context;
        this.recipeRefList = recipeRefList;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> recipeRef = recipeRefList.get(position);

        String recipeId = (String) recipeRef.get("recipeId");
        String authorId = (String) recipeRef.get("authorId");

        if (recipeId == null) {
            Log.e(TAG, "Recipe ID is null at position " + position);
            return;
        }

        // 1. Get the correct path to the recipe document
        DocumentReference recipeDocRef;
        if (authorId != null) {
            // It's a user's private recipe
            recipeDocRef = db.collection("users").document(authorId).collection("my_recipes").document(recipeId);
        } else {
            // It's a public recipe
            recipeDocRef = db.collection("recipes").document(recipeId);
        }

        // 2. Fetch the recipe details
        recipeDocRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // We have the recipe data, bind it
                String title = doc.getString("title");
                String imageUrl = doc.getString("imageUrl");
                String authorName = doc.getString("authorName");

                holder.titleTextView.setText(title);
                holder.authorTextView.setText(authorName != null ? "by " + authorName : "");
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_food)
                        .error(R.drawable.placeholder_food)
                        .into(holder.imageView);

                // 3. Set the click listener to open RecipeDetailsActivity
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, RecipeDetailsActivity.class);
                    intent.putExtra("RECIPE_ID", recipeId);
                    intent.putExtra("USER_ID", authorId); // Pass the authorId (even if null)
                    context.startActivity(intent);
                });
            } else {
                Log.e(TAG, "Could not find recipe document at: " + recipeDocRef.getPath());
                holder.titleTextView.setText("Recipe not found");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching recipe " + recipeId, e));
    }

    @Override
    public int getItemCount() {
        return recipeRefList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView, authorTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recipeImageView);
            titleTextView = itemView.findViewById(R.id.recipeTitleTextView);
            authorTextView = itemView.findViewById(R.id.recipeAuthorTextView);
        }
    }
}