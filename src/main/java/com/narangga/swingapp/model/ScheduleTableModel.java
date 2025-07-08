package com.narangga.swingapp.model;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.narangga.swingapp.Pet;
import com.narangga.swingapp.Schedule;

public class ScheduleTableModel extends AbstractTableModel {
    // Tambahkan kolom "Catatan"
    private final String[] columnNames = {"Peliharaan", "Jenis", "Waktu", "Pengulangan", "Status", "Catatan"};
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
            case 0 -> petMap.containsKey(schedule.getPetId()) ? 
                     petMap.get(schedule.getPetId()).getName() : "Unknown Pet";
            case 1 -> schedule.getCareType();
            case 2 -> dateFormat.format(schedule.getScheduleTime());
            case 3 -> schedule.getRecurrence();
            case 4 -> {
                if ("Once".equalsIgnoreCase(schedule.getRecurrence())) {
                    yield schedule.isActive() ? "Belum" : "Selesai";
                } else {
                    yield schedule.isActive() ? "Aktif" : "Nonaktif";
                }
            }
            case 5 -> schedule.getNotes();
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
