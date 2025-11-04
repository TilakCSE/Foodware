package com.example.recipietracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.recipietracker.R;
import com.example.recipietracker.RecipeDetailsActivity;
import com.example.recipietracker.RecipeItem;
import java.util.List;

// We can re-use the layout from SavedRecipeAdapter
public class CategoryRecipeAdapter extends RecyclerView.Adapter<CategoryRecipeAdapter.ViewHolder> {

    private Context context;
    private List<RecipeItem> recipeList;

    public CategoryRecipeAdapter(Context context, List<RecipeItem> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeItem recipe = recipeList.get(position);

        holder.titleTextView.setText(recipe.getTitle());
        holder.authorTextView.setText(""); // Public recipes don't need an author name here

        Glide.with(context)
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.placeholder_food)
                .into(holder.imageView);

        // Set click listener to open RecipeDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailsActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            intent.putExtra("USER_ID", recipe.getUserId()); // This will be null, which is correct
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // We re-use the ViewHolder from item_saved_recipe.xml
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