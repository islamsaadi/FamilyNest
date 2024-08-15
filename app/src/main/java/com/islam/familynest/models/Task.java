package com.islam.familynest.models;

import java.util.Date;

public class Task {

    private String id;
    private String ownerUserId;
    private String title;
    private Date date;
    private boolean done;
    private int reward;
    private String doneByUserId;
    private int priority; // 1: High, 2: Medium, 3: Low

    public Task() {
    }

    public Task(String id,String ownerUserId, String title, Date date, boolean done, int reward, String doneByUserId, int priority) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.title = title;
        this.date = date;
        this.done = done;
        this.reward = reward;
        this.doneByUserId = doneByUserId;
        this.priority = priority;
    }


    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public String getDoneByUserId() {
        return doneByUserId;
    }

    public void setDoneByUserId(String doneByUserId) {
        this.doneByUserId = doneByUserId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
