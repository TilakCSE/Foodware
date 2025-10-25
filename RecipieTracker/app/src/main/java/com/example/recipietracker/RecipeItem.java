package com.example.recipietracker;

import com.google.firebase.firestore.DocumentId;

public class RecipeItem {

    @DocumentId // Annotation to automatically map the Firestore document ID to this field
    private String id;
    private String title;
    private String imageUrl;
    // Add any other fields you might want to display on the card later (e.g., cookTime)

    // Public no-argument constructor is required for Firestore deserialization
    public RecipeItem() {
    }

    // Constructor for creating instances manually if needed
    public RecipeItem(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // --- Setters (Optional but good practice) ---
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}