package com.timetablexpert.model;

public class CourseViewModel {


    int courseID;
    String courseTitle;
    String courseCode;
    int creditHours;
    String semester;
    String program;
    int programID = 0;
    int semesterID = 0;




    public CourseViewModel(int courseID, String courseTitle, String courseCode, int creditHours, String semester, String program, int programID,int semesterID) {

        this.courseID = courseID;
        this.courseTitle = courseTitle;
        this.courseCode = courseCode;
        this.creditHours = creditHours;
        this.semester = semester;
        this.program = program;
        this.programID = programID;
        this.semesterID = semesterID;


    }

    public int getProgramID() {
        return programID;
    }

    public void setProgramID(int programID) {
        this.programID = programID;
    }

    public int getSemesterID() {
        return semesterID;
    }

    public void setSemesterID(int semesterID) {
        this.semesterID = semesterID;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }
}
