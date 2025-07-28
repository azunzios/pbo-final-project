package com.narangga.swingapp.dao;

import com.narangga.swingapp.util.DatabaseConnection;
import com.narangga.swingapp.model.PetMeasurement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MeasurementDAO {
    public boolean addMeasurement(PetMeasurement measurement) {
        String sql = "INSERT INTO pet_measurements (pet_id, recorded_at, weight, length, notes) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, measurement.getPetId());
            stmt.setTimestamp(2, measurement.getRecordedAt());
            stmt.setDouble(3, measurement.getWeight());
            stmt.setDouble(4, measurement.getLength());
            stmt.setString(5, measurement.getNotes());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        measurement.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<PetMeasurement> getMeasurementsByPetId(int petId) {
        List<PetMeasurement> measurements = new ArrayList<>();
        String sql = "SELECT * FROM pet_measurements WHERE pet_id = ? ORDER BY recorded_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, petId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PetMeasurement m = new PetMeasurement();
                    m.setId(rs.getInt("id"));
                    m.setPetId(rs.getInt("pet_id"));
                    m.setRecordedAt(rs.getTimestamp("recorded_at"));
                    m.setWeight(rs.getDouble("weight"));
                    m.setLength(rs.getDouble("length"));
                    m.setNotes(rs.getString("notes"));
                    measurements.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return measurements;
    }
}