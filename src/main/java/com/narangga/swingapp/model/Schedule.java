package com.narangga.swingapp.model;

import java.util.Date;

public class Schedule {
    private int id;
    private int petId;  // We'll use petId instead of Pet object for simplicity
    private String careType;
    private Date scheduleTime;
    private String days; // Comma separated days: e.g. "Mon,Wed,Fri"
    private String recurrence; // Daily, Weekly, Monthly, Once
    private String category; // Category of the schedule (e.g., Feeding, Grooming, Vet Visit)
    private String notes; // Additional notes for the schedule
    private boolean isActive;

    public Schedule(int petId, String careType, Date scheduleTime, String days, String recurrence, String category, String notes) {
        this.petId = petId;
        this.careType = careType;
        this.scheduleTime = scheduleTime;
        this.days = days;
        this.recurrence = (recurrence != null) ? recurrence : "Once";
        this.category = (category != null) ? category : "General";
        this.notes = notes;
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

    public String getCareType() {
        return careType;
    }


    public Date getScheduleTime() {
        return scheduleTime != null ? new Date(scheduleTime.getTime()) : null;
    }


    public String getDays() {
        return days;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getRecurrence() {
        return recurrence;
    }


    public String getCategory() {
        return category;
    }


    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
