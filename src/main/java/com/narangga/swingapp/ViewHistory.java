package com.narangga.swingapp;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ViewHistory extends JFrame {
    private JList<CareLog> logList;
    private DefaultListModel<CareLog> listModel;
    private JComboBox<Pet> petComboBox;

    public ViewHistory(MainMenu mainMenu) {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Care Log History");
        setSize(600, 500);
        setLayout(new BorderLayout());

        // Pet selection
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Pet:"));
        petComboBox = new JComboBox<>();
        
        // Load pets (simplified for now)
        petComboBox.addItem(new Pet("Buddy", "Dog", null));
        petComboBox.addItem(new Pet("Whiskers", "Cat", null));
        
        petComboBox.addActionListener(e -> loadLogs());
        topPanel.add(petComboBox);
        add(topPanel, BorderLayout.NORTH);

        // Log list
        listModel = new DefaultListModel<>();
        logList = new JList<>(listModel);
        logList.setCellRenderer(new LogListRenderer());
        JScrollPane scrollPane = new JScrollPane(logList);
        add(scrollPane, BorderLayout.CENTER);

        // Load initial logs
        loadLogs();
    }

    private void loadLogs() {
        Pet selectedPet = (Pet) petComboBox.getSelectedItem();
        if (selectedPet == null) return;
        
        listModel.clear();
        try {
            // In real implementation: new CareLogDAO().getCareLogsByPetId(selectedPet.getId())
            listModel.addElement(new CareLog(1, "Feeding", new java.sql.Timestamp(System.currentTimeMillis())));
            listModel.addElement(new CareLog(1, "Grooming", new java.sql.Timestamp(System.currentTimeMillis())));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class LogListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof CareLog) {
            CareLog log = (CareLog) value;
            value = log.getCompletedAt() + " - " + log.getCareType();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
