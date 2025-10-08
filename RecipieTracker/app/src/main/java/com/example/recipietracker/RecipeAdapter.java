package com.example.recipietracker;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeActionListener {
        void onToggleFavorite(Recipe recipe, boolean favorite);
    }

    private List<Recipe> recipes;
    private final OnRecipeActionListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeActionListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    public void updateData(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe r = recipes.get(position);
        holder.txtTitle.setText(r.getTitle());
        holder.txtCuisine.setText(r.getCuisine());
        String meta = r.getPrepMinutes() + "m prep • " + r.getCookMinutes() + "m cook • " + r.getServings() + " servings • " + (r.getDifficulty() == null ? "" : r.getDifficulty());
        holder.txtMeta.setText(meta);
        if (r.getImageUri() != null && !r.getImageUri().isEmpty()) {
            holder.imgThumb.setImageURI(Uri.parse(r.getImageUri()));
        } else {
            holder.imgThumb.setImageResource(R.drawable.gradient_header);
        }
        holder.chkFavorite.setOnCheckedChangeListener(null);
        holder.chkFavorite.setChecked(r.isFavorite());
        holder.chkFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            r.setFavorite(isChecked);
            listener.onToggleFavorite(r, isChecked);
        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), RecipeDetailsActivity.class);
            intent.putExtra("id", r.getId());
            v.getContext().startActivity(intent);
        });
        holder.txtIngredients.setText("Ingredients: " + r.getIngredients());
        holder.txtSteps.setText("Steps: " + r.getSteps());
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtCuisine;
        TextView txtMeta;
        ImageView imgThumb;
        TextView txtIngredients;
        TextView txtSteps;
        CheckBox chkFavorite;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtCuisine = itemView.findViewById(R.id.txtCuisine);
            txtMeta = itemView.findViewById(R.id.txtMeta);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            txtIngredients = itemView.findViewById(R.id.txtIngredients);
            txtSteps = itemView.findViewById(R.id.txtSteps);
            chkFavorite = itemView.findViewById(R.id.chkFavorite);
        }
    }
}



