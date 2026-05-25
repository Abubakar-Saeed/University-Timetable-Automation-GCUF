package com.timetablexpert.model;

public class SessionModel {


    int sessionID;
    String title;

    public SessionModel(int sessionID, String sessionName) {

        this.sessionID = sessionID;
        this.title = sessionName;
    }

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
