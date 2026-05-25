package com.timetablexpert.model;

public class TeacherModel {


    int teacherID;
    String teacherName;
    String phoneNo;
    String email;
    String gender;
    String department;
    String type;

    public TeacherModel(int teacherID, String teacherName, String phoneNo, String email, String gender, String department, String type) {
        this.teacherID = teacherID;
        this.teacherName = teacherName;
        this.phoneNo = phoneNo;
        this.email = email;
        this.gender = gender;
        this.department = department;
        this.type = type;
    }

    public int getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(int teacherID) {
        this.teacherID = teacherID;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
