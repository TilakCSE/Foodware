package com.example.recipietracker;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class HorizontalRecipeAdapter extends RecyclerView.Adapter<HorizontalRecipeAdapter.ViewHolder> {

    /**
     * Interface definition for a callback to be invoked when a recipe item is clicked.
     */
    public interface OnRecipeClickListener {
        /**
         * Called when a recipe item has been clicked.
         * @param recipeId The unique ID of the clicked recipe.
         */
        void onRecipeClick(String recipeId);
    }

    private final List<RecipeItem> recipeList;
    private final OnRecipeClickListener clickListener; // Listener to handle item clicks

    /**
     * Constructor for HorizontalRecipeAdapter.
     * @param recipeList The list of RecipeItem objects to display.
     * @param listener The listener that will handle item clicks.
     */
    public HorizontalRecipeAdapter(List<RecipeItem> recipeList, OnRecipeClickListener listener) {
        this.recipeList = recipeList;
        this.clickListener = listener;
    }

    /**
     * Creates new ViewHolder instances (invoked by the layout manager).
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single recipe card item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card_large, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data at the specified position to the ViewHolder (invoked by the layout manager).
     * Sets the recipe title, loads the image using Glide, and sets the click listener.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the RecipeItem object for the current position
        RecipeItem item = recipeList.get(position);
        if (item == null) {
            return; // Avoid null pointer exceptions
        }

        // Set the recipe title
        holder.title.setText(item.getTitle());

        // Use Glide to load the recipe image from the URL
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())                 // The URL of the image
                .placeholder(R.drawable.placeholder_food) // Image shown while loading
                .error(R.drawable.placeholder_food)       // Image shown if loading fails
                .into(holder.image);                     // The ImageView to load into

        // Set the click listener on the entire item view (the card)
        // Inside HorizontalRecipeAdapter.java -> onBindViewHolder method

        holder.itemView.setOnClickListener(v -> {
            // ADD THIS LOG STATEMENT
            Log.d("RecipeClickDebug", "Item clicked! Position: " + holder.getAdapterPosition());

            if (clickListener != null && item.getId() != null) {
                // ADD THIS LOG STATEMENT
                Log.d("RecipeClickDebug", "Listener is not null, Recipe ID: " + item.getId());
                clickListener.onRecipeClick(item.getId());
            } else {
                // ADD THIS LOG STATEMENT
                if (clickListener == null) {
                    Log.e("RecipeClickDebug", "ClickListener is NULL!");
                }
                if (item.getId() == null) {
                    Log.e("RecipeClickDebug", "Recipe ID is NULL for item: " + item.getTitle());
                }
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    @Override
    public int getItemCount() {
        return recipeList != null ? recipeList.size() : 0;
    }

    /**
     * ViewHolder class that holds references to the views for each recipe card item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views within the item layout
            image = itemView.findViewById(R.id.recipeImageView);
            title = itemView.findViewById(R.id.recipeTitleTextView);
        }
    }
}