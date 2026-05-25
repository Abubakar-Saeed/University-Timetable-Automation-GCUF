package com.timetablexpert.model;

public class AllocatedHours {

    int semesterID;
    int allocatedHours;

    public AllocatedHours(int semesterID, int allocatedHours) {
        this.semesterID = semesterID;
        this.allocatedHours = allocatedHours;
    }

    public int getSemesterID() {
        return semesterID;
    }

    public void setSemesterID(int semesterID) {
        this.semesterID = semesterID;
    }

    public int getAllocatedHours() {
        return allocatedHours;
    }

    public void setAllocatedHours(int allocatedHours) {
        this.allocatedHours = allocatedHours;
    }
}
