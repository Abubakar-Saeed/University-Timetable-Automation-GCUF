package com.timetablexpert.model;

public class SemesterModel {

    int semesterID;
    String semesterTitle;
    int semesterCreditHours;
    String section;
    int capacity;
    int programID;
    int sID;
    int sessionID;


    public SemesterModel(int semesterID, String semesterTitle, int semesterCreditHours, String section, int capacity, int programID, int sID, int sessionID) {

        this.semesterID = semesterID;
        this.semesterTitle = semesterTitle;
        this.semesterCreditHours = semesterCreditHours;
        this.section = section;
        this.capacity = capacity;
        this.programID = programID;
        this.sID = sID;
        this.sessionID = sessionID;
    }


    public int getCapacity() {

        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getProgramID() {
        return programID;
    }

    public void setProgramID(int programID) {
        this.programID = programID;
    }

    public int getsID() {
        return sID;
    }

    public void setsID(int sID) {
        this.sID = sID;
    }



    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public int getSemesterID() {
        return semesterID;
    }

    public void setSemesterID(int semesterID) {
        this.semesterID = semesterID;
    }

    public String getSemesterTitle() {
        return semesterTitle;
    }

    public void setSemesterTitle(String semesterTitle) {
        this.semesterTitle = semesterTitle;
    }

    public int getSemesterCreditHours() {
        return semesterCreditHours;
    }

    public void setSemesterCreditHours(int semesterCreditHours) {
        this.semesterCreditHours = semesterCreditHours;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }






}
