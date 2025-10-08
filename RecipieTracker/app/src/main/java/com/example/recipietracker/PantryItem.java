package com.example.recipietracker;

public class PantryItem {
    private long id;
    private String name;
    private double quantity;
    private String unit;
    private long expiryMillis; // 0 = unknown

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public long getExpiryMillis() { return expiryMillis; }
    public void setExpiryMillis(long expiryMillis) { this.expiryMillis = expiryMillis; }
}


