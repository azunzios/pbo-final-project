package com.narangga.swingapp.model;

public class ScheduleInstance {
    private int id;
    private int scheduleId;
    private java.sql.Date date;
    private boolean isDone;
    private String notes;

    public ScheduleInstance(int scheduleId, java.sql.Date date, boolean isDone, String notes) {
        this.scheduleId = scheduleId;
        this.date = date;
        this.isDone = isDone;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public java.sql.Date getDate() {
        return date;
    }

    public void setDate(java.sql.Date date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}