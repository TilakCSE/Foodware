package com.example.recipietracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CookFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cook, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.cookRecyclerView);

        List<Object> items = createDummyData();
        CookAdapter adapter = new CookAdapter(items);

        // Use a GridLayoutManager to handle both single-column rows and the two-column grid
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // If the item is a category grid item, it takes 1 span. All others take 2 spans (full width).
                switch (adapter.getItemViewType(position)) {
                    case CookAdapter.VIEW_TYPE_CATEGORY_GRID:
                        return 1;
                    default:
                        return 2;
                }
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private List<Object> createDummyData() {
        ArrayList<Object> items = new ArrayList<>();

        // Add each row type to the list in the order you want them to appear
        items.add("GREETING");
        items.add("SEARCH");

        items.add("New recipes");
        items.add(createDummyRecipeList()); // This is a horizontal list

        items.add("Video Recipes");
        items.add(createDummyRecipeList()); // Another horizontal list

        // Start of the grid section
        items.add("Categories");
        items.add(new CategoryItem("5 ingredients or less", R.drawable.placeholder_food));
        items.add(new CategoryItem("No-Cook", R.drawable.placeholder_food));
        items.add(new CategoryItem("Fitness", R.drawable.placeholder_food));
        items.add(new CategoryItem("Breakfast", R.drawable.placeholder_food));
        items.add(new CategoryItem("Healthy Snacks", R.drawable.placeholder_food));
        items.add(new CategoryItem("Desserts", R.drawable.placeholder_food));
        // ... add all your other grid categories here

        return items;
    }

    private List<RecipeItem> createDummyRecipeList() {
        ArrayList<RecipeItem> recipeList = new ArrayList<>();
        recipeList.add(new RecipeItem("Chicken Salad Lettuce Wraps", R.drawable.placeholder_food));
        recipeList.add(new RecipeItem("Fresh Curry Lentils", R.drawable.placeholder_food));
        recipeList.add(new RecipeItem("Hazelnut and Cocoa Cookies", R.drawable.placeholder_food));
        recipeList.add(new RecipeItem("Amazing Pasta Dish", R.drawable.placeholder_food));
        return recipeList;
    }
}