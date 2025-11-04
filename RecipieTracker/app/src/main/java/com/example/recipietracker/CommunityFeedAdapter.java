package com.example.recipietracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Make sure to import Glide

import java.util.List;

public class CommunityFeedAdapter extends RecyclerView.Adapter<CommunityFeedAdapter.PostViewHolder> {

    private List<CommunityPostItem> postList;
    private Context context;

    public CommunityFeedAdapter(List<CommunityPostItem> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_community_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPostItem post = postList.get(position);

        holder.recipeNameTextView.setText(post.getRecipeName());
        holder.commentTextView.setText(post.getComment());
        holder.authorNameTextView.setText(post.getAuthorName() + " cooked"); // Added "cooked"

        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.placeholder_food) // You should have this from before
                .error(R.drawable.placeholder_food)
                .into(holder.postImageView);

        holder.itemView.setOnClickListener(v -> {
            // Get the IDs from the post object
            String recipeId = post.getOriginalRecipeId();
            String userId = post.getOriginalUserId();

            // Check if the IDs exist (for old posts, they will be null)
            if (recipeId != null && !recipeId.isEmpty() && userId != null && !userId.isEmpty()) {
                // We have IDs, so we can open the details
                Intent intent = new Intent(context, RecipeDetailsActivity.class);

                // Pass the IDs to the details activity
                // Your RecipeDetailsActivity is already set up to look for these!
                intent.putExtra("RECIPE_ID", recipeId);
                intent.putExtra("USER_ID", userId);

                context.startActivity(intent);
            } else {
                // This is an old post without the IDs
                Toast.makeText(context, "Cannot open this post (missing data).", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;
        TextView authorNameTextView, recipeNameTextView, commentTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
            authorNameTextView = itemView.findViewById(R.id.authorNameTextView);
            recipeNameTextView = itemView.findViewById(R.id.recipeNameTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);

            // TODO: Add click listeners for likes/replies here later
        }
    }
}