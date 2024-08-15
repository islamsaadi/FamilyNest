package com.islam.familynest.models;

public class User {
    private String name;
    private String email;
    private int rewards;
    private String imageUrl;

    public User() {
    }

    public User(String name, String email, int rewards, String imageUrl) {
        this.name = name;
        this.email = email;
        this.rewards = rewards;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getRewards() {
        return rewards;
    }

    public void setRewards(int rewards) {
        this.rewards = rewards;
    }
}
