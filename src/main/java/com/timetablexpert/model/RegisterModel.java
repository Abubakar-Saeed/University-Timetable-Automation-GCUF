package com.timetablexpert.model;

public class RegisterModel {

    private int userID;
    private String userName;
    private String password;
    private byte[] image; // Updated from String to byte[]
    private int isSuperAdmin;

    public RegisterModel(int userID, String userName, String password, byte[] image, int isSuperAdmin) {
        this.userID = userID;
        this.userName = userName;
        this.password = password;
        this.image = image;
        this.isSuperAdmin = isSuperAdmin;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public int getIsSuperAdmin() {
        return isSuperAdmin;
    }

    public void setIsSuperAdmin(int isSuperAdmin) {
        this.isSuperAdmin = isSuperAdmin;
    }
}
