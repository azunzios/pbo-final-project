package com.narangga.swingapp;
import java.util.ArrayList;
import java.util.List;

public class PetManager {
    private List<Pet> pets;
    private List<Schedule> schedules;
    private List<CareLog> logs;

    public PetManager() {
        pets = new ArrayList<>();
        schedules = new ArrayList<>();
        logs = new ArrayList<>();
    }

    public void addPet(Pet pet) {
        pets.add(pet);
    }

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
    }

    public void logCare(CareLog careLog) {
        logs.add(careLog);
    }

    public List<Schedule> getTodaySchedules() {
        List<Schedule> todaySchedules = new ArrayList<>();
        for (Schedule schedule : schedules) {
            if (schedule.isActive() && schedule.isToday()) {
                todaySchedules.add(schedule);
            }
        }
        return todaySchedules;
    }

    public List<CareLog> getPetHistory(int petId) {
        List<CareLog> history = new ArrayList<>();
        for (CareLog log : logs) {
            if (log.getPetId() == petId) {
                history.add(log);
            }
        }
        return history;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public List<CareLog> getLogs() {
        return logs;
    }
}
