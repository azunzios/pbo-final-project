package com.narangga.swingapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.narangga.swingapp.CareLog;
import com.narangga.swingapp.DatabaseConnection;
import com.narangga.swingapp.settings.UserSettings;

public class CareLogDAO {
    public void addCareLog(CareLog careLog) throws SQLException {
        String sql = "INSERT INTO care_logs (pet_id, schedule_id, care_type, timestamp, notes, done_by, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, careLog.getPetId());
            stmt.setObject(2, careLog.getScheduleId(), Types.INTEGER);
            stmt.setString(3, careLog.getCareType());
            stmt.setTimestamp(4, careLog.getCompletedAt());
            stmt.setString(5, careLog.getNotes());
            stmt.setString(6, careLog.getDoneBy());
            stmt.setInt(7, UserSettings.getCurrentSettings().getUserId());
            stmt.executeUpdate();
        }
    }

    public List<CareLog> getCareLogsByPetId(int petId) throws SQLException {
        List<CareLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM care_logs WHERE pet_id = ? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, petId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractCareLogFromResultSet(rs));
                }
            }
        }
        return logs;
    }

    private CareLog extractCareLogFromResultSet(ResultSet rs) throws SQLException {
        CareLog log = new CareLog(
            rs.getInt("pet_id"),
            rs.getString("care_type"),
            rs.getTimestamp("timestamp")
        );
        log.setId(rs.getInt("id"));
        log.setScheduleId(rs.getObject("schedule_id", Integer.class));
        log.setNotes(rs.getString("notes"));
        log.setDoneBy(rs.getString("done_by"));
        return log;
    }
}