package com.narangga.swingapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Vector;

public class PetManagerFrame extends JFrame {
    private JList<Pet> petList;
    private DefaultListModel<Pet> petListModel;
    private JTextArea petDetailsArea;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private PetDAO petDAO;
    private CareLogDAO careLogDAO;
    private JLabel petImageLabel;
    private JButton deletePetButton;
    private MainMenu mainMenu; // Referensi untuk refresh jadwal

    public PetManagerFrame(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        this.petDAO = new PetDAO();
        this.careLogDAO = new CareLogDAO();
        initializeUI();
        loadPets();
    }

    private void initializeUI() {
        setTitle("Pet Manager");
        setSize(850, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(mainPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Kiri: Daftar Peliharaan dan Tombol
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        petListModel = new DefaultListModel<>();
        petList = new JList<>(petListModel);
        petList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        petList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Pet) {
                    value = ((Pet) value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        JScrollPane listScrollPane = new JScrollPane(petList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("All Pets"));
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        // Panel Tombol di Kiri
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addPetButton = new JButton("Add New Pet");
        deletePetButton = new JButton("Delete Selected Pet");
        deletePetButton.setEnabled(false); // Awalnya nonaktif

        buttonPanel.add(addPetButton);
        buttonPanel.add(deletePetButton);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);

        // Kanan: Panel untuk Detail dan Riwayat
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        JPanel topDetailsPanel = new JPanel(new BorderLayout(10, 10));

        petDetailsArea = new JTextArea(8, 30);
        petDetailsArea.setEditable(false);
        petDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane detailsScrollPane = new JScrollPane(petDetailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Pet Details"));
        topDetailsPanel.add(detailsScrollPane, BorderLayout.CENTER);

        petImageLabel = new JLabel();
        petImageLabel.setPreferredSize(new Dimension(150, 150));
        petImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        petImageLabel.setBorder(BorderFactory.createTitledBorder("Photo"));
        topDetailsPanel.add(petImageLabel, BorderLayout.EAST);

        String[] historyColumns = {"Date", "Care Type", "Notes"};
        historyTableModel = new DefaultTableModel(historyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Care History"));

        rightPanel.add(topDetailsPanel, BorderLayout.NORTH);
        rightPanel.add(historyScrollPane, BorderLayout.CENTER);

        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(250);

        // Listeners
        petList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Pet selectedPet = petList.getSelectedValue();
                displayPetDetails(selectedPet);
                deletePetButton.setEnabled(selectedPet != null); // Aktifkan tombol delete jika ada pet yang dipilih
            }
        });

        addPetButton.addActionListener(e -> {
            // Kirim referensi PetManagerFrame agar bisa refresh
            new AddPetForm(this, mainMenu).setVisible(true);
        });

        deletePetButton.addActionListener(e -> deleteSelectedPet());
    }

    public void loadPets() {
        SwingWorker<List<Pet>, Void> worker = new SwingWorker<List<Pet>, Void>() {
            @Override
            protected List<Pet> doInBackground() throws Exception {
                return petDAO.getAllPets();
            }

            @Override
            protected void done() {
                try {
                    List<Pet> pets = get();
                    petListModel.clear();
                    for (Pet pet : pets) {
                        petListModel.addElement(pet);
                    }
                } catch (Exception e) {
                    handleError("Failed to load pets", e);
                }
            }
        };
        worker.execute();
    }

    private void deleteSelectedPet() {
        Pet selectedPet = petList.getSelectedValue();
        if (selectedPet == null) {
            JOptionPane.showMessageDialog(this, "Please select a pet to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + selectedPet.getName() + "'?\nThis will also delete all associated schedules and care history.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (petDAO.deletePet(selectedPet.getId())) {
                JOptionPane.showMessageDialog(this, "Pet deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPets(); // Refresh list
                mainMenu.loadSchedules(); // Refresh jadwal di menu utama
            } else {
                handleError("Failed to delete pet.", null);
            }
        }
    }

    private void displayPetDetails(Pet pet) {
        if (pet == null) {
            petDetailsArea.setText("");
            historyTableModel.setRowCount(0);
            petImageLabel.setIcon(null);
            petImageLabel.setText("");
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append(String.format("ID      : %d\n", pet.getId()));
        details.append(String.format("Name    : %s\n", pet.getName()));
        details.append(String.format("Type    : %s\n", pet.getType()));
        details.append(String.format("Gender  : %s\n", pet.getGender()));
        details.append(String.format("Birth   : %s\n", pet.getBirthDate()));
        details.append(String.format("Weight  : %.2f kg\n", pet.getWeight()));
        details.append(String.format("Notes   : %s\n", pet.getNotes()));
        petDetailsArea.setText(details.toString());

        String imagePath = pet.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(imagePath);
                Image image = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                petImageLabel.setIcon(new ImageIcon(image));
                petImageLabel.setText("");
            } else {
                petImageLabel.setIcon(null);
                petImageLabel.setText("Image not found");
            }
        } else {
            petImageLabel.setIcon(null);
            petImageLabel.setText("No Photo");
        }

        SwingWorker<List<CareLog>, Void> historyWorker = new SwingWorker<List<CareLog>, Void>() {
            @Override
            protected List<CareLog> doInBackground() throws Exception {
                return careLogDAO.getCareLogsByPetId(pet.getId());
            }

            @Override
            protected void done() {
                try {
                    List<CareLog> logs = get();
                    historyTableModel.setRowCount(0);
                    for (CareLog log : logs) {
                        Vector<Object> row = new Vector<>();
                        row.add(log.getCompletedAt());
                        row.add(log.getCareType());
                        row.add(log.getNotes());
                        historyTableModel.addRow(row);
                    }
                } catch (Exception e) {
                    handleError("Failed to load care history", e);
                }
            }
        };
        historyWorker.execute();
    }

    private void handleError(String message, Exception e) {
        if (e != null) e.printStackTrace();
        JOptionPane.showMessageDialog(this, message + (e != null ? ": " + e.getMessage() : ""), "Error", JOptionPane.ERROR_MESSAGE);
    }
}