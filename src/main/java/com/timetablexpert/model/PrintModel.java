package com.timetablexpert.model;


public class PrintModel {

    int typePDF;
    int typeExcel;
    int teacherWise;
    int departmentWise;
    int roomWise;

    public PrintModel(int typePDF, int typeExcel, int teacherWise, int departmentWise, int roomWise) {

        this.typePDF = typePDF;
        this.typeExcel = typeExcel;
        this.teacherWise = teacherWise;
        this.departmentWise = departmentWise;
        this.roomWise = roomWise;
    }

    public int getTypePDF() {
        return typePDF;
    }

    public void setTypePDF(int typePDF) {
        this.typePDF = typePDF;
    }

    public int getTypeExcel() {
        return typeExcel;
    }

    public void setTypeExcel(int typeExcel) {
        this.typeExcel = typeExcel;
    }

    public int getTeacherWise() {
        return teacherWise;
    }

    public void setTeacherWise(int teacherWise) {
        this.teacherWise = teacherWise;
    }

    public int getDepartmentWise() {
        return departmentWise;
    }

    public void setDepartmentWise(int departmentWise) {
        this.departmentWise = departmentWise;
    }

    public int getRoomWise() {
        return roomWise;
    }

    public void setRoomWise(int roomWise) {
        this.roomWise = roomWise;
    }
}
