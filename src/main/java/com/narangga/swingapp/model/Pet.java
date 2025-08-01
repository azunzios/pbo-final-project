package com.narangga.swingapp.model;

import java.util.Date;

public class Pet {
    private int id;
    private String name;
    private String type;
    private Date birthDate;
    private double weight;
    private double length;
    private String notes;
    private String gender;
    private String imagePath;

    public Pet() {
    }

    public Pet(String name, String type, Date birthDate) {
        this.name = name;
        this.type = type;
        this.birthDate = birthDate != null ? new Date(birthDate.getTime()) : null;
    }

    public Pet(int id, String name, String type, Date birthDate, double weight, String gender, String notes) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.birthDate = birthDate != null ? new Date(birthDate.getTime()) : null;
        this.weight = weight;
        this.gender = gender;
        this.notes = notes;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type != null ? type : "";
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getBirthDate() {
        return birthDate != null ? new Date(birthDate.getTime()) : null;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate != null ? new Date(birthDate.getTime()) : null;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getGender() {
        return gender != null ? gender : "";
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getAge() {
        if (birthDate == null) {
            return 0;
        }

        Date now = new Date();
        long diffInMillies = Math.abs(now.getTime() - birthDate.getTime());
        return (int) (diffInMillies / (1000L * 60 * 60 * 24 * 365));
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, type);
    }
}
