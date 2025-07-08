package com.narangga.swingapp;
import java.util.ArrayList;
import java.util.List;

public class PetManager {
    private List<Pet> pets;
    private List<Schedule> schedules;

    public PetManager() {
        pets = new ArrayList<>();
        schedules = new ArrayList<>();
    }

    public void addPet(Pet pet) {
        pets.add(pet);
    }

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
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

    public List<Pet> getPets() {
        return pets;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }
}
