package com.narangga.swingapp;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

public class AddScheduleForm extends JFrame {
    private JComboBox<Pet> petComboBox;
    private JComboBox<String> careTypeComboBox;
    private JTextField timeField;
    private JTextField daysField;
    private MainMenu mainMenu;

    public AddScheduleForm(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Add New Schedule");
        setSize(400, 300);
        setLocationRelativeTo(mainMenu);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form fields
        formPanel.add(new JLabel("Pet:"));
        petComboBox = new JComboBox<>();
        loadPetsIntoComboBox(); // Memuat hewan dari database

        // Custom renderer untuk menampilkan nama hewan saja
        petComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Pet) {
                    value = ((Pet) value).getName() + " (" + ((Pet) value).getType() + ")";
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        formPanel.add(petComboBox);

        formPanel.add(new JLabel("Care Type:"));
        careTypeComboBox = new JComboBox<>(new String[]{"Feeding", "Grooming", "Walking", "Vet Visit", "Play Time"});
        formPanel.add(careTypeComboBox);

        formPanel.add(new JLabel("Time (HH:MM:SS):"));
        timeField = new JTextField("08:00:00");
        formPanel.add(timeField);

        formPanel.add(new JLabel("Days (e.g. Mon,Wed,Fri):"));
        daysField = new JTextField("Mon,Tue,Wed,Thu,Fri,Sat,Sun");
        formPanel.add(daysField);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveSchedule());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPetsIntoComboBox() {
        try {
            PetDAO petDAO = new PetDAO();
            List<Pet> pets = petDAO.getAllPets();
            for (Pet pet : pets) {
                petComboBox.addItem(pet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load pets: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSchedule() {
        try {
            Pet selectedPet = (Pet) petComboBox.getSelectedItem();
            if (selectedPet == null) {
                JOptionPane.showMessageDialog(this, "Please select a pet.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String careType = (String) careTypeComboBox.getSelectedItem();
            Time time = Time.valueOf(timeField.getText().trim());
            String days = daysField.getText().trim();

            if (days.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Days field cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Schedule schedule = new Schedule(selectedPet.getId(), careType, time, days);

            new ScheduleDAO().addSchedule(schedule);
            JOptionPane.showMessageDialog(this, "Schedule saved successfully!");
            mainMenu.loadSchedules(); // Refresh the schedule list in MainMenu
            dispose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid time format. Please use HH:MM:SS.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}