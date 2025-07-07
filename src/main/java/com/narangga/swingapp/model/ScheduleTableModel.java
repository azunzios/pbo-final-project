package com.narangga.swingapp.model;

import com.narangga.swingapp.*;
import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.*;

public class ScheduleTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Peliharaan", "Jenis", "Waktu", "Pengulangan", "Status"};
    private List<Schedule> schedules;
    private Map<Integer, Pet> petMap;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ScheduleTableModel(List<Schedule> schedules, List<Pet> pets) {
        this.schedules = schedules;
        this.petMap = new HashMap<>();
        for (Pet pet : pets) {
            petMap.put(pet.getId(), pet);
        }
    }

    @Override
    public int getRowCount() {
        return schedules.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Schedule schedule = schedules.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> schedule.getId();
            case 1 -> petMap.containsKey(schedule.getPetId()) ? 
                     petMap.get(schedule.getPetId()).getName() : "Unknown Pet";
            case 2 -> schedule.getCareType();
            case 3 -> dateFormat.format(schedule.getScheduleTime());
            case 4 -> schedule.getRecurrence();
            case 5 -> schedule.isActive() ? "Aktif" : "Nonaktif";
            default -> null;
        };
    }

    public Schedule getScheduleAt(int row) {
        return schedules.get(row);
    }

    public void updateData(List<Schedule> newSchedules, List<Pet> newPets) {
        this.schedules = newSchedules;
        this.petMap.clear();
        for (Pet pet : newPets) {
            petMap.put(pet.getId(), pet);
        }
        fireTableDataChanged();
    }
}
