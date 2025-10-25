package com.example.recipietracker;

import android.util.Log; // Import Log
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

/**
 * The main RecyclerView adapter for the CookFragment.
 * Handles displaying multiple types of rows: greeting, search bar, section headers,
 * horizontal lists (recipes and community posts), and a category grid.
 */
public class CookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Define public integer constants for each view type
    public static final int VIEW_TYPE_GREETING = 0;
    public static final int VIEW_TYPE_SEARCH = 1;
    public static final int VIEW_TYPE_SECTION_HEADER = 2;
    public static final int VIEW_TYPE_RECIPE_LIST = 3;      // For horizontal "New Recipes" list
    public static final int VIEW_TYPE_COMMUNITY_LIST = 4;   // For horizontal "Community" list
    public static final int VIEW_TYPE_CATEGORY_GRID = 5;    // For individual category grid items

    private static final String TAG = "CookAdapter"; // Tag for logging

    private final List<Object> items; // The list of data items (mixed types)

    private final HorizontalRecipeAdapter.OnRecipeClickListener recipeClickListener;

    /**
     * Constructor for CookAdapter.
     * @param items The list of objects representing the rows to display.
     */
    public CookAdapter(List<Object> items, HorizontalRecipeAdapter.OnRecipeClickListener listener) {
        this.items = items;
        this.recipeClickListener = listener;
    }

    /**
     * Determines the type of view that should be created for the item at the given position.
     * This is crucial for handling multiple layouts within the same RecyclerView.
     *
     * @param position The position of the item within the adapter's data set.
     * @return An integer representing the view type. Returns -1 for unknown types.
     */
    @Override
    public int getItemViewType(int position) {
        // Defensive check for invalid position or null item
        if (position < 0 || position >= items.size() || items.get(position) == null) {
            Log.e(TAG, "Invalid position or null item at position: " + position);
            return -1; // Return an invalid type to prevent crashes
        }
        Object item = items.get(position);

        // Check the type of the item and return the corresponding constant
        if (item instanceof GreetingItem) {
            return VIEW_TYPE_GREETING;
        } else if (item instanceof String && ((String) item).equals("SEARCH")) {
            return VIEW_TYPE_SEARCH;
        } else if (item instanceof List) {
            List<?> list = (List<?>) item;
            if (!list.isEmpty()) {
                // Check the type of the first element in the list to differentiate list types
                if (list.get(0) instanceof RecipeItem) {
                    return VIEW_TYPE_RECIPE_LIST;
                } else if (list.get(0) instanceof CommunityPostItem) {
                    return VIEW_TYPE_COMMUNITY_LIST;
                }
            }
            // If list is empty or type unknown, it's an error or unhandled state
            Log.w(TAG, "List item at position " + position + " is empty or has unknown content type.");
            return -1; // Or a specific VIEW_TYPE_EMPTY_LIST if you want to handle it visually
        } else if (item instanceof CategoryItem) {
            return VIEW_TYPE_CATEGORY_GRID;
        } else if (item instanceof String) {
            // Assume any other string is a section header (like "New recipes", "Community", "Categories")
            return VIEW_TYPE_SECTION_HEADER;
        }

        // If none of the above match, log a warning and return an invalid type
        Log.w(TAG, "Unknown item type at position " + position + ": " + item.getClass().getName());
        return -1; // Fallback for safety
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * Inflates the correct layout based on the viewType.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View for the specified view type.
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
                // Both horizontal lists use the same container layout
                return new HorizontalListViewHolder(inflater.inflate(R.layout.item_horizontal_recycler_view, parent, false));
            case VIEW_TYPE_CATEGORY_GRID:
                // Inflate the specific layout for category grid items
                return new CategoryGridViewHolder(inflater.inflate(R.layout.item_category_grid_card, parent, false));
            default:
                // For unknown view types or errors, return an empty ViewHolder
                Log.e(TAG, "Creating EmptyViewHolder for unknown viewType: " + viewType);
                return new EmptyViewHolder(new View(parent.getContext()));
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method fetches the data item and binds it to the provided ViewHolder.
     *
     * @param holder   The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Defensive checks
        if (position < 0 || position >= items.size()) {
            Log.e(TAG, "Invalid position requested in onBindViewHolder: " + position);
            return;
        }
        Object item = items.get(position);
        if (item == null) {
            Log.e(TAG, "Attempting to bind null item at position " + position);
            return; // Don't try to bind null data
        }

        // Bind data based on the ViewHolder's type
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_GREETING:
                ((GreetingViewHolder) holder).bind(((GreetingItem) item).getText());
                break;
            case VIEW_TYPE_SECTION_HEADER:
                ((SectionHeaderViewHolder) holder).bind((String) item);
                break;
            case VIEW_TYPE_RECIPE_LIST:
            case VIEW_TYPE_COMMUNITY_LIST:
                ((HorizontalListViewHolder) holder).bind((List<?>) item, recipeClickListener);
                break;
            case VIEW_TYPE_CATEGORY_GRID:
                ((CategoryGridViewHolder) holder).bind((CategoryItem) item);
                break;
            // No data binding needed for VIEW_TYPE_SEARCH or EmptyViewHolder
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    // --- ViewHolder Classes for Each Row Type ---

    /** ViewHolder for the greeting message row. */
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

    /** ViewHolder for the static search bar row. */
    static class SearchViewHolder extends RecyclerView.ViewHolder {
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            // No specific views to find or bind
        }
    }

    /** ViewHolder for section header rows (e.g., "New Recipes"). */
    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView seeAll; // Reference to the "See all" TextView
        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionTitleTextView);
            seeAll = itemView.findViewById(R.id.seeAllTextView);
        }
        void bind(String text) {
            title.setText(text);
            // TODO: Add click listener for 'seeAll' TextView if needed
            // seeAll.setOnClickListener(v -> { /* Navigate to full list screen */ });
        }
    }

    /** ViewHolder for rows containing a horizontal RecyclerView (used for recipes and community). */
    // Inside CookAdapter.java -> HorizontalListViewHolder class

    static class HorizontalListViewHolder extends RecyclerView.ViewHolder {
        RecyclerView horizontalRecyclerView;
        public HorizontalListViewHolder(@NonNull View itemView) {
            super(itemView);
            horizontalRecyclerView = itemView.findViewById(R.id.horizontalRecyclerView);
        }
        // Modify bind to accept the listener
        void bind(List<?> list, HorizontalRecipeAdapter.OnRecipeClickListener listener) {
            horizontalRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            if (list != null && !list.isEmpty()) {
                if (list.get(0) instanceof RecipeItem) {
                    // Pass the listener directly to the HorizontalRecipeAdapter
                    horizontalRecyclerView.setAdapter(new HorizontalRecipeAdapter((List<RecipeItem>) list, listener));
                } else if (list.get(0) instanceof CommunityPostItem) {
                    // Pass null for now, or implement a community click listener later
                    horizontalRecyclerView.setAdapter(new CommunityPostAdapter((List<CommunityPostItem>) list));
                } else {
                    horizontalRecyclerView.setAdapter(null);
                }
            } else {
                horizontalRecyclerView.setAdapter(null);
            }
        }
    }

    /** ViewHolder for the category grid items. */
    static class CategoryGridViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        public CategoryGridViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.categoryTitleTextView);
            image = itemView.findViewById(R.id.categoryImageView);
        }
        /** Binds a CategoryItem data object to the views. */
        void bind(CategoryItem item) {
            if (item == null) {
                Log.e(TAG, "Binding null CategoryItem in CategoryGridViewHolder");
                title.setText("Error"); // Indicate an error state
                image.setImageResource(R.drawable.placeholder_food); // Set placeholder
                return;
            }
            // Use getName() based on your Firestore field name correction
            title.setText(item.getTitle());
            Log.d("CategoryDebug", "Binding category title: '" + item.getTitle() + "' ImageUrl: " + item.getImageUrl());

            // Use Glide to load the image
            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_food) // Image shown while loading
                    .error(R.drawable.placeholder_food)       // Image shown if loading fails
                    .into(image);

            // TODO: Add click listener for category items if needed
            // itemView.setOnClickListener(v -> { /* Handle category click */ });
        }
    }

    /** A simple fallback ViewHolder used for unknown view types or error states. */
    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            // This view holder doesn't need to do anything
        }
    }
}