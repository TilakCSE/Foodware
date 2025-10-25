package com.example.recipietracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Define public integer constants for each view type
    public static final int VIEW_TYPE_GREETING = 0;
    public static final int VIEW_TYPE_SEARCH = 1;
    public static final int VIEW_TYPE_SECTION_HEADER = 2;
    public static final int VIEW_TYPE_RECIPE_LIST = 3;      // For "New Recipes"
    public static final int VIEW_TYPE_COMMUNITY_LIST = 4;   // For "Community"
    public static final int VIEW_TYPE_CATEGORY_GRID = 5;

    private final List<Object> items;

    public CookAdapter(List<Object> items) {
        this.items = items;
    }

    /**
     * Determines the type of view that should be created for the item at the given position.
     */
    @Override
    public int getItemViewType(int position) {
        // Defensive check for invalid position or null item
        if (position < 0 || position >= items.size() || items.get(position) == null) {
            return -1; // Return an invalid type
        }
        Object item = items.get(position);

        if (item instanceof GreetingItem) {
            return VIEW_TYPE_GREETING;
        } else if (item instanceof String && ((String) item).equals("SEARCH")) {
            return VIEW_TYPE_SEARCH;
        } else if (item instanceof List) {
            List<?> list = (List<?>) item;
            if (!list.isEmpty()) {
                // Check the type of the first element in the list to differentiate
                if (list.get(0) instanceof RecipeItem) {
                    return VIEW_TYPE_RECIPE_LIST;
                } else if (list.get(0) instanceof CommunityPostItem) {
                    return VIEW_TYPE_COMMUNITY_LIST;
                }
            }
            // If list is empty or type unknown, return default
            return -1;
        } else if (item instanceof CategoryItem) {
            return VIEW_TYPE_CATEGORY_GRID;
        } else if (item instanceof String) {
            // Assume any other string is a section header
            return VIEW_TYPE_SECTION_HEADER;
        }
        return -1; // Fallback for safety
    }

    /**
     * Creates new ViewHolder instances based on the view type.
     */
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
            case VIEW_TYPE_RECIPE_LIST:
            case VIEW_TYPE_COMMUNITY_LIST:
                return new HorizontalListViewHolder(inflater.inflate(R.layout.item_horizontal_recycler_view, parent, false));
            case VIEW_TYPE_CATEGORY_GRID:
                return new CategoryGridViewHolder(inflater.inflate(R.layout.item_category_grid_card, parent, false));
            default:
                // Return a simple empty view to prevent crashes for unknown types
                return new EmptyViewHolder(new View(parent.getContext()));
        }
    }

    /**
     * Binds the data at the specified position to the ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Defensive check
        if (position < 0 || position >= items.size()) {
            return;
        }
        Object item = items.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_GREETING:
                ((GreetingViewHolder) holder).bind(((GreetingItem) item).getText());
                break;
            case VIEW_TYPE_SECTION_HEADER:
                ((SectionHeaderViewHolder) holder).bind((String) item);
                break;
            case VIEW_TYPE_RECIPE_LIST:
            case VIEW_TYPE_COMMUNITY_LIST:
                ((HorizontalListViewHolder) holder).bind((List<?>) item);
                break;
            case VIEW_TYPE_CATEGORY_GRID:
                ((CategoryGridViewHolder) holder).bind((CategoryItem) item);
                break;
            // No binding needed for SEARCH or EMPTY view types
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    // --- ViewHolder Classes for Each Row Type ---

    /** ViewHolder for the greeting message */
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

    /** ViewHolder for the search bar */
    static class SearchViewHolder extends RecyclerView.ViewHolder {
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            // No specific views to bind, layout is static
        }
    }

    /** ViewHolder for section headers (e.g., "New Recipes") */
    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView seeAll; // Optional: Find "See All" if needed
        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionTitleTextView);
            seeAll = itemView.findViewById(R.id.seeAllTextView);
        }
        void bind(String text) {
            title.setText(text);
            // Add click listener for seeAll if needed
        }
    }

    /** ViewHolder for rows containing a horizontal RecyclerView */
    static class HorizontalListViewHolder extends RecyclerView.ViewHolder {
        RecyclerView horizontalRecyclerView;
        public HorizontalListViewHolder(@NonNull View itemView) {
            super(itemView);
            horizontalRecyclerView = itemView.findViewById(R.id.horizontalRecyclerView);
        }
        void bind(List<?> list) {
            horizontalRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            // Check the type of the list content to set the correct adapter
            if (!list.isEmpty()) {
                if (list.get(0) instanceof RecipeItem) {
                    horizontalRecyclerView.setAdapter(new HorizontalRecipeAdapter((List<RecipeItem>) list));
                } else if (list.get(0) instanceof CommunityPostItem) {
                    horizontalRecyclerView.setAdapter(new CommunityPostAdapter((List<CommunityPostItem>) list));
                }
            } else {
                // Handle empty list case if necessary, e.g., clear the adapter
                horizontalRecyclerView.setAdapter(null);
            }
        }
    }

    /** ViewHolder for the category grid items */
    static class CategoryGridViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        public CategoryGridViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.categoryTitleTextView);
            image = itemView.findViewById(R.id.categoryImageView);
        }
        void bind(CategoryItem item) {
            title.setText(item.getTitle());
            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_food) // Fallback image
                    .error(R.drawable.placeholder_food) // Image to show if URL fails
                    .into(image);
        }
    }

    /** A fallback ViewHolder for unknown or error states */
    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}