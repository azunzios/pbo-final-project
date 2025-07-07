package com.narangga.swingapp;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.narangga.swingapp.settings.UserSettings;


public class ScheduleDAO {

    /**
     * Menambahkan jadwal baru ke database.
     * @param schedule Objek Schedule yang akan ditambahkan
     * @throws SQLException jika terjadi kesalahan saat akses database
     */
    public void addSchedule(Schedule schedule) throws SQLException {
        String sql = "INSERT INTO schedules (pet_id, care_type, schedule_time, days, recurrence, category, notes, is_active, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, schedule.getPetId());
            stmt.setString(2, schedule.getCareType());
            stmt.setTimestamp(3, new java.sql.Timestamp(schedule.getScheduleTime().getTime()));
            stmt.setString(4, schedule.getDays());
            stmt.setString(5, schedule.getRecurrence());
            stmt.setString(6, schedule.getCategory());
            stmt.setString(7, schedule.getNotes());
            stmt.setBoolean(8, schedule.isActive());
            stmt.setInt(9, UserSettings.getCurrentSettings().getUserId());
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
        String sql = "SELECT * FROM schedules WHERE user_id = ? ORDER BY schedule_time";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, UserSettings.getCurrentSettings().getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(extractScheduleFromResultSet(rs));
                }
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
        String sql = "UPDATE schedules SET pet_id=?, care_type=?, schedule_time=?, days=?, " +
                    "recurrence=?, category=?, notes=?, is_active=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, schedule.getPetId());
            stmt.setString(2, schedule.getCareType());
            stmt.setTimestamp(3, new java.sql.Timestamp(schedule.getScheduleTime().getTime()));
            stmt.setString(4, schedule.getDays());
            stmt.setString(5, schedule.getRecurrence());
            stmt.setString(6, schedule.getCategory());
            stmt.setString(7, schedule.getNotes());
            stmt.setBoolean(8, schedule.isActive());
            stmt.setInt(9, schedule.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Menghapus jadwal berdasarkan ID.
     * @param id ID jadwal yang akan dihapus
     * @throws SQLException jika terjadi kesalahan saat akses database
     */
    public void deleteSchedule(int id) throws SQLException {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Memeriksa apakah jadwal duplikat ada untuk pet, jenis perawatan, waktu, dan frekuensi yang sama.
     * @param schedule Objek Schedule yang akan diperiksa
     * @return true jika jadwal duplikat ditemukan, false jika tidak
     * @throws SQLException jika terjadi kesalahan saat akses database
     */
    public boolean isDuplicateSchedule(Schedule schedule) throws SQLException {
        String sql = "SELECT COUNT(*) FROM schedules WHERE pet_id=? AND care_type=? AND schedule_time=? AND recurrence=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, schedule.getPetId());
            stmt.setString(2, schedule.getCareType());
            stmt.setTimestamp(3, new java.sql.Timestamp(schedule.getScheduleTime().getTime()));
            stmt.setString(4, schedule.getRecurrence());
            stmt.setInt(5, UserSettings.getCurrentSettings().getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
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
                new Date(rs.getTimestamp("schedule_time").getTime()),
                rs.getString("days"),
                rs.getString("recurrence"),
                rs.getString("category"),
                rs.getString("notes")
        );
        schedule.setId(rs.getInt("id"));
        schedule.setActive(rs.getBoolean("is_active"));
        return schedule;
    }
}