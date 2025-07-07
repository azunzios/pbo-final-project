package com.narangga.swingapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.narangga.swingapp.DatabaseConnection;
import com.narangga.swingapp.Pet;
import com.narangga.swingapp.settings.UserSettings;

public class PetDAO {

    public List<Pet> getAllPets() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT * FROM pets WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, UserSettings.getCurrentSettings().getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pets.add(extractPetFromResultSet(rs));
                }
            }
        }
        return pets;
    }

    public void addPet(Pet pet) throws SQLException {
        int userId = UserSettings.getCurrentSettings().getUserId();
        if (!isUserIdValid(userId)) {
            throw new SQLException("Invalid user_id: " + userId);
        }

        String sql = "INSERT INTO pets (name, type, birth_date, weight, gender, notes, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getType());
            stmt.setDate(3, new java.sql.Date(pet.getBirthDate().getTime()));
            stmt.setDouble(4, pet.getWeight());
            stmt.setString(5, pet.getGender());
            stmt.setString(6, pet.getNotes());
            stmt.setInt(7, userId); // Ensure user_id is valid
            stmt.executeUpdate();
        }
    }

    public void updatePet(Pet pet) throws SQLException {
        String sql = "UPDATE pets SET name=?, type=?, birth_date=?, weight=?, gender=?, notes=?, length=?, image_path=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getType());
            stmt.setDate(3, new java.sql.Date(pet.getBirthDate().getTime()));
            stmt.setDouble(4, pet.getWeight());
            stmt.setString(5, pet.getGender());
            stmt.setString(6, pet.getNotes());
            stmt.setDouble(7, pet.getLength());
            stmt.setString(8, pet.getImagePath());
            stmt.setInt(9, pet.getId());
            stmt.executeUpdate();
        }
    }

    private Pet extractPetFromResultSet(ResultSet rs) throws SQLException {
        Pet pet = new Pet();
        pet.setId(rs.getInt("id"));
        pet.setName(rs.getString("name"));
        pet.setType(rs.getString("type"));
        pet.setBirthDate(rs.getDate("birth_date"));
        pet.setWeight(rs.getDouble("weight"));
        pet.setGender(rs.getString("gender"));
        pet.setNotes(rs.getString("notes"));
        return pet;
    }

    private boolean isUserIdValid(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Return true if user_id exists
                }
            }
        }
        return false;
    }
}