package com.narangga.swingapp.model;

public class User {
    private int id;
    private String username;
    private String fullName;

    public User(String username, String email, String fullName) {
        this.username = username;
        this.fullName = fullName;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }
}
