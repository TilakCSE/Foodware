package com.example.recipietracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.ViewHolder> {

    private final List<CommunityPostItem> postList;

    public CommunityPostAdapter(List<CommunityPostItem> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommunityPostItem item = postList.get(position);
        holder.authorText.setText(item.getAuthorName() + " cooked");
        holder.recipeNameText.setText(item.getRecipeName());
        holder.commentText.setText(item.getComment());

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder_food)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView authorText, recipeNameText, commentText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.postImageView);
            authorText = itemView.findViewById(R.id.authorTextView);
            recipeNameText = itemView.findViewById(R.id.recipeNameTextView);
            commentText = itemView.findViewById(R.id.commentTextView);
        }
    }
}