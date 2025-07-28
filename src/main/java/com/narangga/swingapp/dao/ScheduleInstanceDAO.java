package com.narangga.swingapp.dao;

import com.narangga.swingapp.util.DatabaseConnection;

import java.sql.*;

public class ScheduleInstanceDAO {
    public void addInstance(int scheduleId, java.sql.Date date, boolean isDone, String notes) throws SQLException {
        String sql = "INSERT INTO schedule_instances (schedule_id, date, is_done, notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scheduleId);
            stmt.setDate(2, date);
            stmt.setBoolean(3, isDone);
            stmt.setString(4, notes);
            stmt.executeUpdate();
        }
    }

    public boolean isInstanceDone(int scheduleId, java.sql.Date date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM schedule_instances WHERE schedule_id = ? AND date = ? AND is_done = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scheduleId);
            stmt.setDate(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
