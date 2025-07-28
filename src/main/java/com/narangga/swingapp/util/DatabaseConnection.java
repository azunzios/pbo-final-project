package com.narangga.swingapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/petcare";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Creates and returns a new connection to the database.
     * The connection should be closed by the caller, preferably using a try-with-resources block.
     * @return a new Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}