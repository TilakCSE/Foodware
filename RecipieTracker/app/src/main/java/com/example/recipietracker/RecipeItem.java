package com.example.recipietracker;

public class RecipeItem {
    private String title;
    private String imageUrl;

    public RecipeItem() {} // Needed for Firestore

    public RecipeItem(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
}