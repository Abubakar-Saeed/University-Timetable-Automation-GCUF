package com.timetablexpert.model;

public class TeacherCourse {

    int teacherCourseID;
    String title;
    int lectureID;
    int courseID;
    int programSemesterID;

    public TeacherCourse(int teacherCourseID, String title, int lectureID, int courseID, int programSemesterID) {

        this.teacherCourseID = teacherCourseID;
        this.title = title;
        this.lectureID = lectureID;
        this.courseID = courseID;
        this.programSemesterID = programSemesterID;
    }

    public int getTeacherCourseID() {
        return teacherCourseID;
    }

    public void setTeacherCourseID(int teacherCourseID) {
        this.teacherCourseID = teacherCourseID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLectureID() {
        return lectureID;
    }

    public void setLectureID(int lectureID) {
        this.lectureID = lectureID;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public int getProgramSemesterID() {
        return programSemesterID;
    }

    public void setProgramSemesterID(int programSemesterID) {
        this.programSemesterID = programSemesterID;
    }
}
