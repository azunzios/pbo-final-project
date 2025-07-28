package com.narangga.swingapp.form;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.narangga.swingapp.dao.UserDAO;

public class RegisterForm extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField fullNameField;
    private final UserDAO userDAO;

    public RegisterForm(Frame parent) {
        super(parent, "Register New User", true);
        this.userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nambahkan komponen untuk form
        addFormField(mainPanel, "Username:", usernameField = new JTextField(20), gbc, 0);
        addFormField(mainPanel, "Password:", passwordField = new JPasswordField(20), gbc, 1);
        addFormField(mainPanel, "Confirm Password:", confirmPasswordField = new JPasswordField(20), gbc, 2);
        addFormField(mainPanel, "Email:", emailField = new JTextField(20), gbc, 3);
        addFormField(mainPanel, "Full Name:", fullNameField = new JTextField(20), gbc, 4);

        // tombol2nya
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");

        registerButton.addActionListener(e -> handleRegister());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void handleRegister() {
        if (!validateInput()) {
            return;
        }

        try {
            boolean success = userDAO.register(
                usernameField.getText(),
                new String(passwordField.getPassword()),
                emailField.getText(),
                fullNameField.getText()
            );

            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Registration successful! Please login.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Registration failed: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInput() {
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, 
                "Passwords do not match!", 
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (usernameField.getText().isEmpty() || emailField.getText().isEmpty() || fullNameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all required fields.", 
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}