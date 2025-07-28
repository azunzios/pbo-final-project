package com.narangga.swingapp;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;
import com.narangga.swingapp.form.LoginForm;

public class PetCareApplication {
    public static void main(String[] args) {
        setupLookAndFeel();
        setupDatabase();
        
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static void setupDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Driver Database MySQL tidak ditemukan!", 
                "Error Kritis", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}