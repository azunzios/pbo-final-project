package com.narangga.swingapp.settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.narangga.swingapp.DatabaseConnection;
import com.narangga.swingapp.model.User; // Ensure this import exists

public class UserSettings {
    private static UserSettings instance;
    private int userId;
    private boolean requireNotes;
    private boolean enableNotifications;
    private int reminderMinutes;

    // Private constructor with default settings
    private UserSettings() {
        this.requireNotes = true;
        this.enableNotifications = true;
        this.reminderMinutes = 15;
    }

    // Returns the current settings singleton instance
    public static UserSettings getCurrentSettings() {
        if (instance == null) {
            instance = new UserSettings();
        }
        return instance;
    }

    // Initializes settings for a specific user and loads from DB
    public static void initializeForUser(int userId) {
        instance = new UserSettings();
        instance.userId = userId;
        instance.loadFromDatabase();
    }

    // Clears the current settings instance
    public static void clearSettings() {
        instance = null;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public boolean isRequireNotes() {
        return requireNotes;
    }

    public void setRequireNotes(boolean requireNotes) {
        this.requireNotes = requireNotes;
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }

    public void setEnableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }

    public int getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
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
                    enableNotifications = rs.getBoolean("enable_notifications");
                    reminderMinutes = rs.getInt("reminder_minutes");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Saves user settings to the database
    public void save() {
        String sql = "INSERT INTO user_settings (user_id, require_notes, enable_notifications, reminder_minutes) " +
                     "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                     "require_notes = VALUES(require_notes), " +
                     "enable_notifications = VALUES(enable_notifications), " +
                     "reminder_minutes = VALUES(reminder_minutes)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setBoolean(2, requireNotes);
            stmt.setBoolean(3, enableNotifications);
            stmt.setInt(4, reminderMinutes);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveCurrentUser(User user) {
        // Mock implementation for saving the current user
        System.out.println("User " + user.getUsername() + " saved as current user.");
    }
}
