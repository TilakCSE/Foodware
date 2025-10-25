package com.example.recipietracker;

public class CommunityPostItem {
    private String authorName;
    private String recipeName;
    private String comment;
    private String imageUrl;

    public CommunityPostItem() {} // For Firestore

    // --- Add getters for all fields ---
    public String getAuthorName() { return authorName; }
    public String getRecipeName() { return recipeName; }
    public String getComment() { return comment; }
    public String getImageUrl() { return imageUrl; }
}