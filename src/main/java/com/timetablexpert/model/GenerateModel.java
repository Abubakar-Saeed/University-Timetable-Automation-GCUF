package com.timetablexpert.model;




public class GenerateModel {

        int timetableID;
        String day;
        String slot;
        String slotTitle;


    public GenerateModel(int timetableID, String day, String slot, String slotTitle) {

        this.timetableID = timetableID;
        this.day = day;
        this.slot = slot;
        this.slotTitle = slotTitle;
    }

    public int getTimetableID() {
        return timetableID;
    }

    public void setTimetableID(int timetableID) {
        this.timetableID = timetableID;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getSlotTitle() {
        return slotTitle;
    }

    public void setSlotTitle(String slotTitle) {
        this.slotTitle = slotTitle;
    }
}
