package com.timetablexpert.model;

public class RoomModel {

    int roomID;
    String roomNo;
    int capacity;
    String program;
    int programID;
    int type;

    public RoomModel(int roomID, String roomNo, int capacity, String program, int programID, int type) {

        this.roomID = roomID;
        this.roomNo = roomNo;
        this.capacity = capacity;
        this.program = program;
        this.programID = programID;
        this.type = type;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public int getProgramID() {
        return programID;
    }

    public void setProgramID(int programID) {
        this.programID = programID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
