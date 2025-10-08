package com.example.recipietracker;

public class Recipe {
    private long id;
    private String title;
    private String ingredients;
    private String steps;
    private String cuisine;
    private boolean favorite;
    private String imageUri; // persisted as string
    private int prepMinutes;
    private int cookMinutes;
    private int servings;
    private String difficulty; // Easy, Medium, Hard

    public Recipe() {}

    public Recipe(long id, String title, String ingredients, String steps, String cuisine, boolean favorite) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.steps = steps;
        this.cuisine = cuisine;
        this.favorite = favorite;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public int getPrepMinutes() { return prepMinutes; }
    public void setPrepMinutes(int prepMinutes) { this.prepMinutes = prepMinutes; }

    public int getCookMinutes() { return cookMinutes; }
    public void setCookMinutes(int cookMinutes) { this.cookMinutes = cookMinutes; }

    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}



