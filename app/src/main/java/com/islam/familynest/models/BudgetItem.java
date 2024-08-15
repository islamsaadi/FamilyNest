package com.islam.familynest.models;

import java.util.Date;

public class BudgetItem {

    private String id;
    private String itemName;
    private double amount;
    private String type; // "Income" or "Expense"
    private Date date;

    public BudgetItem() {
    }

    public BudgetItem(String itemName, double amount, String type, Date date) {
        this.itemName = itemName;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
