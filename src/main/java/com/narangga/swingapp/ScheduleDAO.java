package com.narangga.swingapp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {

    /**
     * Menambahkan jadwal baru ke database.
     * @param schedule Objek Schedule yang akan ditambahkan
     * @throws SQLException jika terjadi kesalahan saat akses database
     */
    public void addSchedule(Schedule schedule) throws SQLException {
        String sql = "INSERT INTO schedules (pet_id, care_type, schedule_time, days, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, schedule.getPetId());
            stmt.setString(2, schedule.getCareType());
            stmt.setTime(3, schedule.getScheduleTime());
            stmt.setString(4, schedule.getDays());
            stmt.setBoolean(5, schedule.isActive());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    schedule.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Mengambil semua jadwal untuk pet_id tertentu.
     * @param petId ID dari peliharaan
     * @return Daftar objek Schedule
     * @throws SQLException jika terjadi kesalahan saat akses database
     */
    public List<Schedule> getSchedulesByPetId(int petId) throws SQLException {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE pet_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, petId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(extractScheduleFromResultSet(rs));
                }
            }
        }
        return schedules;
    }

    /**
     * Mengambil semua jadwal yang ada di database, diurutkan berdasarkan waktu.
     * @return Daftar semua objek Schedule
     * @throws SQLException jika terjadi kesalahan saat akses database
     */
    public List<Schedule> getAllSchedules() throws SQLException {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules ORDER BY schedule_time";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                schedules.add(extractScheduleFromResultSet(rs));
            }
        }
        return schedules;
    }

    /**
     * Mengambil satu jadwal berdasarkan ID-nya.
     * @param id ID dari jadwal yang dicari
     * @return Objek Schedule jika ditemukan, jika tidak null
     * @throws SQLException jika terjadi kesalahan saat akses database
     */
    public Schedule getSchedule(int id) throws SQLException {
        String sql = "SELECT * FROM schedules WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractScheduleFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Memperbarui data jadwal yang sudah ada.
     * @param schedule Objek Schedule dengan informasi yang sudah diperbarui
     * @throws SQLException jika terjadi kesalahan saat akses database
     */
    public void updateSchedule(Schedule schedule) throws SQLException {
        String sql = "UPDATE schedules SET pet_id=?, care_type=?, schedule_time=?, days=?, is_active=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, schedule.getPetId());
            stmt.setString(2, schedule.getCareType());
            stmt.setTime(3, schedule.getScheduleTime());
            stmt.setString(4, schedule.getDays());
            stmt.setBoolean(5, schedule.isActive());
            stmt.setInt(6, schedule.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Helper method untuk mengekstrak objek Schedule dari ResultSet
     * untuk menghindari duplikasi kode.
     * @param rs ResultSet yang berisi data schedule
     * @return Objek Schedule yang sudah diisi data
     * @throws SQLException jika terjadi kesalahan saat membaca ResultSet
     */
    private Schedule extractScheduleFromResultSet(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule(
                rs.getInt("pet_id"),
                rs.getString("care_type"),
                rs.getTime("schedule_time"),
                rs.getString("days")
        );
        schedule.setId(rs.getInt("id"));
        schedule.setActive(rs.getBoolean("is_active"));
        return schedule;
    }
}