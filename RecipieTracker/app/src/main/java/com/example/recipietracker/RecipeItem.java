package com.example.recipietracker;

public class RecipeItem {
    private String title;
    private int imageResId; // We'll use local drawable IDs for now

    public RecipeItem(String title, int imageResId) {
        this.title = title;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }
}