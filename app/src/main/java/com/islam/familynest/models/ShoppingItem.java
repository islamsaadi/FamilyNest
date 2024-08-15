package com.islam.familynest.models;

public class ShoppingItem {
    private String uid;
    private String shoppingListUid;
    private String name;
    private double cost;
    private boolean done;

    public ShoppingItem() {
    }

    public ShoppingItem(String uid, String shoppingListUid, String name, double cost, boolean done) {
        this.uid = uid;
        this.shoppingListUid = shoppingListUid;
        this.name = name;
        this.cost = cost;
        this.done = done;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getShoppingListUid() {
        return shoppingListUid;
    }

    public void setShoppingListUid(String shoppingListUid) {
        this.shoppingListUid = shoppingListUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
