package com.narangga.swingapp.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.narangga.swingapp.util.DatabaseConnection;
import com.narangga.swingapp.model.Pet;
import com.narangga.swingapp.settings.UserSettings;

/**
 * Data Access Object for Pet entities
 * Handles all database operations related to pets
 */
public class PetDAO {
    
    /**
     * Adds a new pet to the database
     * @param pet The pet to add
     * @return true if successful, false otherwise
     */
    public boolean addPet(Pet pet) {
        String sql = "INSERT INTO pets (name, type, birth_date, weight, length, gender, notes, image_path, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getType());
            
            // Handle null birth date
            if (pet.getBirthDate() != null) {
                stmt.setDate(3, new java.sql.Date(pet.getBirthDate().getTime()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            
            stmt.setDouble(4, pet.getWeight());
            stmt.setDouble(5, pet.getLength());
            stmt.setString(6, pet.getGender());
            stmt.setString(7, pet.getNotes());
            stmt.setString(8, pet.getImagePath());
            stmt.setInt(9, UserSettings.getCurrentSettings().getUserId()); // <-- Tambahkan ini

            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating pet failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pet.setId(generatedKeys.getInt(1));
                    return true;
                } else {
                    throw new SQLException("Creating pet failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            handleException("Error adding pet", e);
            return false;
        }
    }
    
    /**
     * Retrieves a pet by ID
     * @param id The ID of the pet to retrieve
     * @return The Pet object, or null if not found
     */
    public Pet getPet(int id) {
        String sql = "SELECT * FROM pets WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPetFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            handleException("Error retrieving pet", e);
        }
        
        return null;
    }
    
    /**
     * Retrieves all pets from the database
     * @return A list of all pets, ordered by name
     */
    public List<Pet> getAllPets() {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT * FROM pets WHERE user_id = ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, UserSettings.getCurrentSettings().getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pets.add(extractPetFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            handleException("Error retrieving pets", e);
        }
        return pets;
    }
    
    /**
     * Updates an existing pet in the database
     * @param pet The pet with updated information
     * @return true if successful, false otherwise
     */
    public boolean updatePet(Pet pet) {
        String sql = "UPDATE pets SET name = ?, type = ?, birth_date = ?, weight = ?, length = ? ,gender = ?, notes = ?, image_path = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getType());
            
            // Handle null birth date
            if (pet.getBirthDate() != null) {
                stmt.setDate(3, new java.sql.Date(pet.getBirthDate().getTime()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            
            stmt.setDouble(4, pet.getWeight());
            stmt.setDouble(5, pet.getLength());
            stmt.setString(6, pet.getGender());
            stmt.setString(7, pet.getNotes());
            stmt.setString(8, pet.getImagePath());
            stmt.setInt(9, pet.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            handleException("Error updating pet", e);
            return false;
        }
    }
    
    /**
     * Deletes a pet from the database
     * @param id The ID of the pet to delete
     * @return true if successful, false otherwise
     */
    public boolean deletePet(int id) {
        String sql = "DELETE FROM pets WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            handleException("Error deleting pet", e);
            return false;
        }
    }
    
    /**
     * Helper method to extract a Pet object from a ResultSet
     * @param rs The ResultSet containing pet data
     * @return A Pet object populated with data from the ResultSet
     * @throws SQLException if a database error occurs
     */
    private Pet extractPetFromResultSet(ResultSet rs) throws SQLException {
        Pet pet = new Pet();
        pet.setId(rs.getInt("id"));
        pet.setName(rs.getString("name"));
        pet.setType(rs.getString("type"));
        pet.setBirthDate(rs.getDate("birth_date"));
        pet.setWeight(rs.getDouble("weight"));
        pet.setGender(rs.getString("gender"));
        pet.setNotes(rs.getString("notes"));
        pet.setImagePath(rs.getString("image_path"));
        return pet;
    }
    
    /**
     * Helper method to handle database exceptions
     */
    private void handleException(String message, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
            null, 
            message + ": " + e.getMessage(), 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE
        );
    }
}
