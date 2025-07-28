package com.narangga.swingapp.settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.narangga.swingapp.util.DatabaseConnection;

public class UserSettings {
    private static UserSettings instance;
    private int userId;
    private boolean requireNotes;

    private UserSettings() {
        this.requireNotes = true;
    }

    //Current Settings dengan desain Singleton Instance
    public static UserSettings getCurrentSettings() {
        if (instance == null) {
            instance = new UserSettings();
        }
        return instance;
    }

    // menginisialisasi instance untuk user tertentu dan memuat settings dari database
    public static void initializeForUser(int userId) {
        instance = new UserSettings();
        instance.userId = userId;
        instance.loadFromDatabase();
    }

    // menghapus instance settings
    public static void clearSettings() {
        instance = null;
    }

    // getters dan setters
    public int getUserId() {
        return userId;
    }

    public boolean isRequireNotes() {
        return requireNotes;
    }

    public void setRequireNotes(boolean requireNotes) {
        this.requireNotes = requireNotes;
    }

    // Loads user settings from the database
    private void loadFromDatabase() {
        String sql = "SELECT * FROM user_settings WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    requireNotes = rs.getBoolean("require_notes");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Saves user settings to the database
    public void save() {
        String sql = "INSERT INTO user_settings (user_id, require_notes) " +
                     "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                     "require_notes = VALUES(require_notes), ";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setBoolean(2, requireNotes);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
