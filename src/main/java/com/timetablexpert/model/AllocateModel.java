package com.timetablexpert.model;

public class AllocateModel {

    int subjectID;
    String subjectTitle;
    String lab;
    String batch;
    String programName;
    String semester;


    public AllocateModel(int subjectID, String subjectTitle, String lab, String batch, String programName, String semester) {

        this.subjectID = subjectID;
        this.subjectTitle = subjectTitle;
        this.lab = lab;
        this.batch = batch;
        this.programName = programName;
        this.semester = semester;

    }

    public int getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(int subjectID) {
        this.subjectID = subjectID;
    }

    public String getSubjectTitle() {
        return subjectTitle;
    }

    public void setSubjectTitle(String subjectTitle) {
        this.subjectTitle = subjectTitle;
    }

    public String getLab() {
        return lab;
    }

    public void setLab(String lab) {
        this.lab = lab;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}