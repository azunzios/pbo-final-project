package com.narangga.swingapp;

import java.sql.Time;

public class Schedule {
    private int id;
    private int petId;  // We'll use petId instead of Pet object for simplicity
    private String careType;
    private Time scheduleTime;
    private String days; // Comma separated days: e.g. "Mon,Wed,Fri"
    private boolean isActive;

    public Schedule(int petId, String careType, Time scheduleTime, String days) {
        this.petId = petId;
        this.careType = careType;
        this.scheduleTime = scheduleTime;
        this.days = days;
        this.isActive = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public String getCareType() {
        return careType;
    }

    public void setCareType(String careType) {
        this.careType = careType;
    }

    public Time getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Time scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }

    // Placeholder for isToday method
    public boolean isToday() {
        // Check if today is in the days string
        // This is a placeholder
        return true;
    }
}
