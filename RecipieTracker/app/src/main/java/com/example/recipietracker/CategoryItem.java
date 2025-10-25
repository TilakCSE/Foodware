package com.example.recipietracker;

public class CategoryItem {
    private String title;
    private String imageUrl;

    public CategoryItem() {} // Needed for Firestore

    public CategoryItem(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
}