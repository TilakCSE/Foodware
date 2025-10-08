package com.example.recipietracker;

public class DashboardItem {
    private final String label;
    private final int iconResId;

    public DashboardItem(String label, int iconResId) {
        this.label = label;
        this.iconResId = iconResId;
    }

    public String getLabel() {
        return label;
    }

    public int getIconResId() {
        return iconResId;
    }
}







