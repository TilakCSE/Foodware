package com.example.recipietracker;

public class CategoryItem {
    private String title;
    private int imageResId;

    public CategoryItem(String title, int imageResId) {
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