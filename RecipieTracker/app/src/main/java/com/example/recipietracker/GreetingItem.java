package com.example.recipietracker;

public class GreetingItem {
    private String text;

    public GreetingItem(String text) {
        this.text = text;
    }

    // THIS IS THE MISSING METHOD
    public String getText() {
        return text;
    }
}