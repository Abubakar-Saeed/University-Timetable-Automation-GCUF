package com.timetablexpert.model;

public class LectureView {

    int lectureID;
    int creditHours;

    public LectureView(int lectureID, int creditHours) {

        this.lectureID = lectureID;
        this.creditHours = creditHours;

    }

    public int getLectureID() {
        return lectureID;
    }

    public void setLectureID(int lectureID) {
        this.lectureID = lectureID;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }
}
