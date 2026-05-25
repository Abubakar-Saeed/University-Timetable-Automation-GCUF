package com.timetablexpert.model;

public class User {

    private final String username;
    private final boolean isSuperAdmin;
    private final String imageBase64;

    public User(String username, boolean isSuperAdmin, String imageBase64) {
        this.username = username;
        this.isSuperAdmin = isSuperAdmin;
        this.imageBase64 = imageBase64;
    }

    public String getUsername() {
        return username;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public String getImageBase64() {
        return imageBase64;
    }
}