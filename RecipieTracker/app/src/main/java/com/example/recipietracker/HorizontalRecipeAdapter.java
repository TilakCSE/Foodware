package com.example.recipietracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HorizontalRecipeAdapter extends RecyclerView.Adapter<HorizontalRecipeAdapter.ViewHolder> {

    private final List<RecipeItem> recipeList;

    public HorizontalRecipeAdapter(List<RecipeItem> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card_large, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeItem item = recipeList.get(position);
        holder.title.setText(item.getTitle());
        holder.image.setImageResource(item.getImageResId()); // Or use a library like Glide to load from a URL
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.recipeImageView);
            title = itemView.findViewById(R.id.recipeTitleTextView);
        }
    }
}