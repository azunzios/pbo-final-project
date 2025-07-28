package com.narangga.swingapp.form;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import com.narangga.swingapp.dao.PetDAO;
import com.narangga.swingapp.dao.ScheduleDAO;
import com.narangga.swingapp.model.Pet;
import com.narangga.swingapp.model.Schedule;
import com.narangga.swingapp.panel.MainMenu;
import com.narangga.swingapp.panel.SchedulePanel;
import com.toedter.calendar.JDateChooser;

public class AddScheduleForm extends JDialog {
    private final SchedulePanel parent;
    private JComboBox<String> recurrenceBox;
    private JPanel datePanel;
    private JSpinner timeSpinner;
    private JComboBox<Pet> petComboBox;
    private JTextField careTypeField;
    private JTextArea notesArea;
    private List<JCheckBox> weekdayCheckboxes;
    private JSpinner monthDaySpinner;
    private JSpinner dateSpinner; // Use JSpinner for date selection
    private JDateChooser dateChooser;
    private JComboBox<String> categoryBox;
    private Schedule scheduleToEdit;

    public AddScheduleForm(SchedulePanel parent) {
        this(parent, null);
    }

    public AddScheduleForm(SchedulePanel parent, Schedule scheduleToEdit) {
        super(
                parent == null ? null : (Frame) SwingUtilities.getWindowAncestor(parent),
                scheduleToEdit == null ? "Tambah Jadwal Baru" : "Edit Jadwal",
                true);
        this.parent = parent;
        this.scheduleToEdit = scheduleToEdit;
        initializeUI();
        if (scheduleToEdit != null) {
            loadScheduleData();
        }
    }

    private void initializeUI() {
        // Ganti layout utama menjadi GridBagLayout agar field tidak bertumpuk
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Mulai dari atas

        int row = 0;

        // Pet selection
        gbc.gridx = 0; gbc.gridy = row;
        mainPanel.add(new JLabel("Peliharaan:"), gbc);
        gbc.gridx = 1;
        petComboBox = new JComboBox<>();
        loadPets();
        mainPanel.add(petComboBox, gbc);

        // Care type
        gbc.gridx = 0; gbc.gridy = ++row;
        mainPanel.add(new JLabel("Jenis Perawatan:"), gbc);
        gbc.gridx = 1;
        careTypeField = new JTextField(20);
        mainPanel.add(careTypeField, gbc);

        // Recurrence selection
        gbc.gridx = 0; gbc.gridy = ++row;
        mainPanel.add(new JLabel("Pengulangan:"), gbc);
        gbc.gridx = 1;
        recurrenceBox = new JComboBox<>(new String[] { "Once", "Daily", "Weekly", "Monthly" });
        recurrenceBox.addActionListener(e -> updateDatePanel());
        mainPanel.add(recurrenceBox, gbc);

        // Dynamic date/time panel
        gbc.gridx = 0; gbc.gridy = ++row;
        gbc.gridwidth = 2;
        datePanel = new JPanel(new CardLayout());
        mainPanel.add(datePanel, gbc);
        gbc.gridwidth = 1;

        // Time selection (common for all types)
        gbc.gridx = 0; gbc.gridy = ++row;
        mainPanel.add(new JLabel("Waktu:"), gbc);
        gbc.gridx = 1;
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        mainPanel.add(timeSpinner, gbc);

        // Category selection
        gbc.gridx = 0; gbc.gridy = ++row;
        mainPanel.add(new JLabel("Kategori:"), gbc);
        gbc.gridx = 1;
        categoryBox = new JComboBox<>(new String[] { "General", "Feeding", "Medicine", "Grooming", "Exercise" });
        mainPanel.add(categoryBox, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = ++row;
        mainPanel.add(new JLabel("Catatan:"), gbc);
        gbc.gridx = 1;
        notesArea = new JTextArea(3, 20);
        mainPanel.add(new JScrollPane(notesArea), gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Simpan");
        JButton cancelButton = new JButton("Batal");
        saveButton.addActionListener(e -> saveSchedule());
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        initializeDatePanels();
        pack();
        setLocationRelativeTo(parent);
    }

    private void loadPets() {
        PetDAO petDAO = new PetDAO();
        try {
            List<Pet> pets = petDAO.getAllPets();
            for (Pet pet : pets) {
                petComboBox.addItem(pet);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading pets: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeDatePanels() {
        // Once panel with date picker (using JSpinner)
        JPanel oncePanel = new JPanel();
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        oncePanel.add(dateSpinner);
        datePanel.add(oncePanel, "Once");

        // Weekly panel with day checkboxes
        JPanel weeklyPanel = new JPanel();
        weekdayCheckboxes = new ArrayList<>();
        String[] days = { "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu" };
        for (String day : days) {
            JCheckBox cb = new JCheckBox(day);
            weekdayCheckboxes.add(cb);
            weeklyPanel.add(cb);
        }
        datePanel.add(weeklyPanel, "Weekly");

        // Monthly panel with day spinner
        JPanel monthlyPanel = new JPanel();
        monthDaySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
        monthlyPanel.add(new JLabel("Tanggal:"));
        monthlyPanel.add(monthDaySpinner);
        datePanel.add(monthlyPanel, "Monthly");

        // Empty panel for daily (only needs time)
        datePanel.add(new JPanel(), "Daily");
    }

    private void updateDatePanel() {
        CardLayout cl = (CardLayout) datePanel.getLayout();
        cl.show(datePanel, (String) recurrenceBox.getSelectedItem());
    }

    private void saveSchedule() {
        if (parent == null) return;
        try {
            // Validasi input
            Pet selectedPet = (Pet) petComboBox.getSelectedItem();
            if (selectedPet == null) {
                JOptionPane.showMessageDialog(this, "Please select a pet.", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String careType = careTypeField.getText().trim();
            String notes = notesArea.getText().trim();
            String recurrence = (String) recurrenceBox.getSelectedItem();
            Date scheduleTime = (Date) timeSpinner.getValue();
            Date finalScheduleTime = new Date();

            switch (recurrence) {
                case "Once":
                    Date selectedDate = (Date) dateSpinner.getValue();
                    finalScheduleTime.setTime(selectedDate.getTime());
                    finalScheduleTime.setHours(scheduleTime.getHours());
                    finalScheduleTime.setMinutes(scheduleTime.getMinutes());
                    finalScheduleTime.setSeconds(0);
                    break;
                    
                case "Monthly":
                    finalScheduleTime = new Date();
                    int day = (Integer) monthDaySpinner.getValue();
                    finalScheduleTime.setDate(day);
                    finalScheduleTime.setHours(scheduleTime.getHours());
                    finalScheduleTime.setMinutes(scheduleTime.getMinutes());
                    finalScheduleTime.setSeconds(0);
                    break;
                    
                case "Weekly":
                    if (getSelectedDays().isEmpty()) {
                        JOptionPane.showMessageDialog(this, 
                            "Pilih minimal satu hari untuk jadwal mingguan", 
                            "Validation Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    finalScheduleTime = scheduleTime;
                    break;
                    
                case "Daily":
                    finalScheduleTime = scheduleTime;
                    break;
            }

            Schedule schedule = new Schedule(
                selectedPet.getId(),
                careType,
                finalScheduleTime,
                getSelectedDays(),
                recurrence,
                (String) categoryBox.getSelectedItem(),
                notes);

            ScheduleDAO scheduleDAO = new ScheduleDAO();
            if (scheduleToEdit != null) {
                // Edit mode
                schedule.setId(scheduleToEdit.getId());
                scheduleDAO.updateSchedule(schedule);
                JOptionPane.showMessageDialog(this, "Schedule updated successfully!");
            } else {
                // Add mode
                scheduleDAO.addSchedule(schedule);
                JOptionPane.showMessageDialog(this, "Schedule saved successfully!");
            }
            parent.loadSchedules();

            // Cari MainMenu dari parent chain, lalu refreshData (agar HomePanel juga refresh)
            Component comp = parent;
            while (comp != null && !(comp instanceof MainMenu)) {
                comp = comp.getParent();
            }
            if (comp instanceof MainMenu mainMenu) {
                mainMenu.refreshData();
            }
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedDays() {
        StringBuilder sb = new StringBuilder();
        if (weekdayCheckboxes != null) {
            for (JCheckBox cb : weekdayCheckboxes) {
                if (cb.isSelected()) {
                    if (sb.length() > 0)
                        sb.append(",");
                    sb.append(cb.getText());
                }
            }
        }
        return sb.toString();
    }

    private void loadScheduleData() {
        if (scheduleToEdit != null) {
            for (int i = 0; i < petComboBox.getItemCount(); i++) {
                Pet pet = (Pet) petComboBox.getItemAt(i);
                if (pet.getId() == scheduleToEdit.getPetId()) {
                    petComboBox.setSelectedIndex(i);
                    break;
                }
            }
            careTypeField.setText(scheduleToEdit.getCareType());
            timeSpinner.setValue(scheduleToEdit.getScheduleTime());
            recurrenceBox.setSelectedItem(scheduleToEdit.getRecurrence());
            categoryBox.setSelectedItem(scheduleToEdit.getCategory());
            notesArea.setText(scheduleToEdit.getNotes());

            // Load specific recurrence data
            if ("Weekly".equals(scheduleToEdit.getRecurrence())) {
                String[] selectedDays = scheduleToEdit.getDays().split(",");
                for (String day : selectedDays) {
                    for (JCheckBox cb : weekdayCheckboxes) {
                        if (cb.getText().equals(day.trim())) {
                            cb.setSelected(true);
                        }
                    }
                }
            }
        }
    }
}