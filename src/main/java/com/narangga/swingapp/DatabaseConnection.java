package com.narangga.swingapp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/petcare";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    // Private constructor to prevent instantiation
    private DatabaseConnection() {}

    /**
     * Creates and returns a new connection to the database.
     * The connection should be closed by the caller, preferably using a try-with-resources block.
     * @return a new Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver if you haven't done it in the main class
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // This is a critical error, so we throw a RuntimeException
            throw new RuntimeException("MySQL JDBC Driver not found!", e);
        }
        // DriverManager.getConnection() creates a new connection each time.
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // The closeConnection() method is removed.
    // Connections will be managed by try-with-resources blocks in the DAO classes.
}