package com.islam.familynest.models;

public class Budget {

    private String id;
    private String category;
    private double amount;

    public Budget() {
    }

    public Budget(String category, double amount) {
        this.category = category;
        this.amount = amount;
    }

    // Getters and setters for the fields
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
