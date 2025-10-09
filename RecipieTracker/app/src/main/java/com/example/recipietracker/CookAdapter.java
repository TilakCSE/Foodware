package com.example.recipietracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Define integer constants for each view type
    // Define integer constants for each view type
    public static final int VIEW_TYPE_GREETING = 0;
    public static final int VIEW_TYPE_SEARCH = 1;
    public static final int VIEW_TYPE_SECTION_HEADER = 2;
    public static final int VIEW_TYPE_HORIZONTAL_LIST = 3;
    public static final int VIEW_TYPE_CATEGORY_GRID = 4;

    private final List<Object> items;

    public CookAdapter(List<Object> items) {
        this.items = items;
    }

    // This method is the key to handling multiple view types
    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String && ((String) item).equals("GREETING")) {
            return VIEW_TYPE_GREETING;
        } else if (item instanceof String && ((String) item).equals("SEARCH")) {
            return VIEW_TYPE_SEARCH;
        } else if (item instanceof String) { // Section Headers are simple strings
            return VIEW_TYPE_SECTION_HEADER;
        } else if (item instanceof List) { // Horizontal Lists are a List of RecipeItems
            return VIEW_TYPE_HORIZONTAL_LIST;
        } else if (item instanceof CategoryItem) { // Grid items are CategoryItems
            return VIEW_TYPE_CATEGORY_GRID;
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_GREETING:
                return new GreetingViewHolder(inflater.inflate(R.layout.item_header_greeting, parent, false));
            case VIEW_TYPE_SEARCH:
                return new SearchViewHolder(inflater.inflate(R.layout.item_search_bar, parent, false));
            case VIEW_TYPE_SECTION_HEADER:
                return new SectionHeaderViewHolder(inflater.inflate(R.layout.item_section_header, parent, false));
            case VIEW_TYPE_HORIZONTAL_LIST:
                return new HorizontalListViewHolder(inflater.inflate(R.layout.item_horizontal_recycler_view, parent, false));
            case VIEW_TYPE_CATEGORY_GRID:
                return new CategoryGridViewHolder(inflater.inflate(R.layout.item_category_grid_card, parent, false));
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_GREETING:
                ((GreetingViewHolder) holder).bind("Good night, Tilak!");
                break;
            case VIEW_TYPE_SECTION_HEADER:
                ((SectionHeaderViewHolder) holder).bind((String) items.get(position));
                break;
            case VIEW_TYPE_HORIZONTAL_LIST:
                ((HorizontalListViewHolder) holder).bind((List<RecipeItem>) items.get(position));
                break;
            case VIEW_TYPE_CATEGORY_GRID:
                ((CategoryGridViewHolder) holder).bind((CategoryItem) items.get(position));
                break;
            // Search and Greeting have no data to bind, they are static
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // --- ViewHolder Classes for Each Row Type ---

    static class GreetingViewHolder extends RecyclerView.ViewHolder {
        TextView greetingText;
        public GreetingViewHolder(@NonNull View itemView) {
            super(itemView);
            greetingText = itemView.findViewById(R.id.greetingTextView);
        }
        void bind(String text) {
            greetingText.setText(text);
        }
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionTitleTextView);
        }
        void bind(String text) {
            title.setText(text);
        }
    }

    static class HorizontalListViewHolder extends RecyclerView.ViewHolder {
        RecyclerView horizontalRecyclerView;
        public HorizontalListViewHolder(@NonNull View itemView) {
            super(itemView);
            horizontalRecyclerView = itemView.findViewById(R.id.horizontalRecyclerView);
        }
        void bind(List<RecipeItem> recipeList) {
            horizontalRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            HorizontalRecipeAdapter adapter = new HorizontalRecipeAdapter(recipeList);
            horizontalRecyclerView.setAdapter(adapter);
        }
    }

    static class CategoryGridViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        // ImageView image; // Find image if you need to set it here
        public CategoryGridViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.categoryTitleTextView);
            // image = itemView.findViewById(R.id.categoryImageView);
        }
        void bind(CategoryItem item) {
            title.setText(item.getTitle());
        }
    }
}