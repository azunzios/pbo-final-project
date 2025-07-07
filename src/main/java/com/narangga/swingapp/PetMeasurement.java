package com.narangga.swingapp;

import java.sql.Timestamp;

public class PetMeasurement {
    private int id;
    private int petId;
    private Timestamp recordedAt;
    private double weight;
    private double length;
    private String notes;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }
    public Timestamp getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Timestamp recordedAt) { this.recordedAt = recordedAt; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}