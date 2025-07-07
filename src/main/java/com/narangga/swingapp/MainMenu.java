package com.narangga.swingapp;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.narangga.swingapp.model.User;
import com.narangga.swingapp.settings.UserSettings;

public class MainMenu extends JFrame {

    private static final Color SECONDARY_COLOR = new Color(139, 195, 74);
    private static final Color ACCENT_COLOR = new Color(255, 213, 79);
    private static final Color BACKGROUND_COLOR = new Color(249, 251, 231);
    private static final Color TEXT_COLOR = new Color(94, 80, 63);
    private static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private SchedulePanel schedulePanel;
    private HomePanel homePanel;
    private PetManagerPanel petManagerPanel;

    private JLabel userNameLabel;
    private final User currentUser;
    private JButton homeButton; // Store reference to home button

    public MainMenu(User user) {
        this.currentUser = user;

        // Initialize frame properties before UI components
        setTitle("Manajemen PetCare");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Ensure cardLayout is initialized first
        cardLayout = new CardLayout();

        // Initialize the UI components
        add(createSideBar(), BorderLayout.WEST);
        add(createMainContentPanel(), BorderLayout.CENTER);

        // Update user information after UI is created
        updateUserInfo();

        // Add window listener to handle cleanup
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Any cleanup needed when closing
            }
        });

        // This ensures the frame is fully initialized before becoming visible
        pack();
        setLocationRelativeTo(null); // Center on screen
        
        // Click the home button after the UI is fully initialized
        SwingUtilities.invokeLater(() -> {
            if (homeButton != null) {
                homeButton.doClick();
            }
        });
    }

    private JPanel createSideBar() {
        JPanel sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBackground(new Color(240, 240, 240));
        sideBar.setPreferredSize(new Dimension(200, 0));
        sideBar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Tambahkan logo
        JLabel logoLabel = new JLabel(new ImageIcon("path/to/logo.png")); // Ganti dengan path logo
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sideBar.add(logoLabel);
        sideBar.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel titleLabel = new JLabel("PetCare");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 70, 70));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sideBar.add(titleLabel);
        sideBar.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel greetingLabel = new JLabel(getGreeting());
        greetingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        greetingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userNameLabel = new JLabel("User");
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sideBar.add(greetingLabel);
        sideBar.add(userNameLabel);
        sideBar.add(Box.createRigidArea(new Dimension(0, 30)));

        homeButton = addMenuButton(sideBar, "Beranda", "HOME");
        addMenuButton(sideBar, "Peliharaan", "PETS");
        addMenuButton(sideBar, "Jadwal", "SCHEDULE");

        sideBar.add(Box.createVerticalGlue());

        addMenuButton(sideBar, "Settings", "SETTINGS");
        addMenuButton(sideBar, "Logout", "LOGOUT");

        return sideBar;
    }

    private JPanel createMainContentPanel() {
        mainContentPanel = new JPanel(cardLayout); // Gunakan cardLayout yang sudah diinisialisasi
        mainContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainContentPanel.setBackground(BACKGROUND_COLOR);

        homePanel = new HomePanel();
        schedulePanel = new SchedulePanel(this);
        petManagerPanel = new PetManagerPanel(this);

        // Inject SchedulePanel ke HomePanel agar bisa auto-refresh kalender
        homePanel.setSchedulePanel(schedulePanel);

        JPanel profilePanel = new JPanel();
        profilePanel.add(new JLabel("Area Profil Pengguna"));
        profilePanel.setBackground(BACKGROUND_COLOR);

        // Ganti area pengaturan dengan SettingsPanel
        JPanel settingsPanel = new com.narangga.swingapp.settings.SettingsPanel();
        settingsPanel.setBackground(BACKGROUND_COLOR);

        mainContentPanel.add(homePanel, "HOME");
        mainContentPanel.add(schedulePanel, "SCHEDULE");
        mainContentPanel.add(petManagerPanel, "PETS");
        mainContentPanel.add(profilePanel, "PROFILE");
        mainContentPanel.add(settingsPanel, "SETTINGS");

        return mainContentPanel;
    }

    private JButton addMenuButton(JPanel panel, String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setActionCommand(actionCommand);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(panel.getBackground());
        button.setForeground(new Color(70, 70, 70));

        button.addActionListener(e -> {
            if ("LOGOUT".equals(actionCommand)) {
                handleLogout();
                return;
            }
            
            // Check if mainContentPanel has a parent before calling cardLayout.show()
            if (mainContentPanel.getParent() != null) {
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JButton) {
                        comp.setBackground(panel.getBackground());
                        comp.setForeground(new Color(70, 70, 70));
                    }
                }
                button.setBackground(new Color(200, 230, 255));
                button.setForeground(new Color(0, 100, 200));
                cardLayout.show(mainContentPanel, e.getActionCommand());
            }
        });

        panel.add(button);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        return button; // Return the button reference
    }

    private String getGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour >= 5 && hour < 12)
            return "Selamat pagi,";
        if (hour >= 12 && hour < 18)
            return "Selamat siang,";
        return "Selamat malam,";
    }

    public void refreshData() {
        System.out.println("[MainMenu] Refreshing data...");
        if (homePanel != null) {
            homePanel.refreshData();
            System.out.println("[MainMenu] HomePanel refreshed.");
        }
        if (schedulePanel != null) {
            schedulePanel.loadSchedules();
            System.out.println("[MainMenu] SchedulePanel refreshed.");
        }
        if (petManagerPanel != null) {
            petManagerPanel.loadPets();
            System.out.println("[MainMenu] PetManagerPanel refreshed.");
        }
    }

    private void updateUserInfo() {
        if (userNameLabel != null) {
            userNameLabel.setText(currentUser.getFullName());
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            UserSettings.clearSettings();
            dispose();
            new LoginForm().setVisible(true);
        }
    }
}