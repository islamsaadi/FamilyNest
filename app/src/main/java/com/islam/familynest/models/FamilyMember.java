package com.islam.familynest.models;

public class FamilyMember {
    private String uid;
    private String name;
    private int rewards;

    public FamilyMember() {
    }

    public FamilyMember(String uid, String name, int rewards) {
        this.uid = uid;
        this.name = name;
        this.rewards = rewards;
    }

    // Getters and setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRewards() {
        return rewards;
    }

    public void setRewards(int rewards) {
        this.rewards = rewards;
    }
}
