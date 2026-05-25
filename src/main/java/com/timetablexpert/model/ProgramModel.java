package com.timetablexpert.model;

public class ProgramModel {

    int programID;
    String programName;

    public ProgramModel(int programID, String programName) {

        this.programID = programID;
        this.programName = programName;
    }

    public int getProgramID() {

        return programID;

    }

    public void setProgramID(int programID) {
        this.programID = programID;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

}
