package com.timetablexpert.model;


public class DashboardModel {

    private int totalPrograms;
    private int totalClasses;
    private int totalRegularTeachers;
    private int totalVisitingTeachers;


    public DashboardModel(){}

    public DashboardModel(int totalPrograms, int totalClasses, int totalRegularTeachers, int totalVisitingTeachers) {

        this.totalPrograms = totalPrograms;
        this.totalClasses = totalClasses;
        this.totalRegularTeachers = totalRegularTeachers;
        this.totalVisitingTeachers = totalVisitingTeachers;

    }
    public int getTotalPrograms() {

        return totalPrograms;
    }
    public void setTotalPrograms(int totalPrograms) {
        this.totalPrograms = totalPrograms;
    }
    public int getTotalClasses() {
        return totalClasses;
    }
    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }
    public int getTotalRegularTeachers() {
        return totalRegularTeachers;
    }
    public void setTotalRegularTeachers(int totalRegularTeachers) {
        this.totalRegularTeachers = totalRegularTeachers;
    }
    public int getTotalVisitingTeachers() {
        return totalVisitingTeachers;
    }
    public void setTotalVisitingTeachers(int totalVisitingTeachers) {

        this.totalVisitingTeachers = totalVisitingTeachers;

    }



}
