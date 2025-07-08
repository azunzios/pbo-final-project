package com.narangga.swingapp;

import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.narangga.swingapp.dao.UserDAO;
import com.narangga.swingapp.model.User;
import com.narangga.swingapp.settings.UserSettings;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private final UserDAO userDAO;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JPanel loginPanel;
    private JPanel loadingPanel;

    public LoginForm() {
        super("Login - PetCare");
        this.userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Create login panel
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create loading panel
        loadingPanel = new LoadingPanel();
        
        // Add panels to card layout
        contentPanel.add(loginPanel, "login");
        contentPanel.add(loadingPanel, "loading");
        
        add(contentPanel);

        // Rest of the login panel UI setup
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add logo and title in one panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        headerPanel.setOpaque(false);

        ImageIcon logo = new ImageIcon(getClass().getResource("/icons/logo.png"));
        if (logo.getImage() != null) {
            Image scaled = logo.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaled));
            headerPanel.add(logoLabel);
        }

        JLabel titleLabel = new JLabel("PetCare Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(headerPanel, gbc);

        // Username (adjust gridy)
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.addActionListener(evt -> handleLogin()); // Login on Enter
        loginPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.addActionListener(evt -> handleLogin()); // Login on Enter
        loginPanel.add(passwordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.addActionListener(evt -> handleLogin());
        registerButton.addActionListener(evt -> showRegisterForm());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginPanel.add(buttonPanel, gbc);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                cardLayout.show(contentPanel, "loading");
                
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        UserSettings.initializeForUser(user.getId());
                        Thread.sleep(1500);
                        return null;
                    }

                    @Override
                    protected void done() {
                        ((LoadingPanel) loadingPanel).stopAnimation();
                        new MainMenu(user).setVisible(true);
                        dispose();
                    }
                };
                worker.execute();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegisterForm() {
        new RegisterForm(this).setVisible(true);
    }
}