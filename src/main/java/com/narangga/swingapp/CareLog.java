package com.narangga.swingapp;
import java.sql.Timestamp;

public class CareLog {
    private int id;
    private int petId;
    private Integer scheduleId; // Can be null if not from a schedule
    private String careType;
    private Timestamp completedAt;
    private String notes;
    private String doneBy;

    public CareLog(int petId, String careType, Timestamp completedAt) {
        this.petId = petId;
        this.careType = careType;
        this.completedAt = completedAt;
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

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getCareType() {
        return careType;
    }

    public void setCareType(String careType) {
        this.careType = careType;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDoneBy() {
        return doneBy;
    }

    public void setDoneBy(String doneBy) {
        this.doneBy = doneBy;
    }

    public void addNotes(String note) {
        if (this.notes == null) {
            this.notes = note;
        } else {
            this.notes += "\n" + note;
        }
    }
}
