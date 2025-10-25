package com.example.recipietracker;

import com.google.firebase.firestore.DocumentId;

public class CategoryItem {
    @DocumentId
    private String id;
    private String title; // Ensure this is 'title'
    private String imageUrl;

    public CategoryItem() {}

    // Ensure the getter matches the field name
    public String getId() { return id; }
    public String getTitle() { return title; } // Ensure this is getTitle()
    public String getImageUrl() { return imageUrl; }

    // Optional setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; } // Ensure this is setTitle()
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}