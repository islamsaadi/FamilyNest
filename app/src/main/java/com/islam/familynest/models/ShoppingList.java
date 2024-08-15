package com.islam.familynest.models;
import java.util.Map;

public class ShoppingList {
    private String uid;
    private String ownerUid;
    private String name;
    private Map<String, ShoppingItem> shoppingItems;

    public ShoppingList() {
    }

    public ShoppingList(String uid, String ownerUid, String name, Map<String, ShoppingItem> shoppingItems) {
        this.uid = uid;
        this.ownerUid = ownerUid;
        this.name = name;
        this.shoppingItems = shoppingItems;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ShoppingItem> getShoppingItems() {
        return shoppingItems;
    }

    public void setShoppingItems(Map<String, ShoppingItem> shoppingItems) {
        this.shoppingItems = shoppingItems;
    }
}
