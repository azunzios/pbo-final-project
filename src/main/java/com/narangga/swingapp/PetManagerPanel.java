package com.narangga.swingapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

public class PetManagerPanel extends JPanel {
    private JList<Pet> petList;
    private DefaultListModel<Pet> petListModel;
    private JTextArea petDetailsArea;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private PetDAO petDAO;
    private CareLogDAO careLogDAO;
    private JLabel petImageLabel;
    private JButton deletePetButton;
    private MainMenu mainMenu;
    private JTable measurementTable;
    private DefaultTableModel measurementTableModel;

    public PetManagerPanel(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        this.petDAO = new PetDAO();
        this.careLogDAO = new CareLogDAO();
        initializeUI();
        loadPets();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(UIManager.getColor("Panel.background"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        add(splitPane, BorderLayout.CENTER);

        JPanel leftPanel = createLeftPanel();
        splitPane.setLeftComponent(leftPanel);

        JPanel rightPanel = createRightPanel();
        splitPane.setRightComponent(rightPanel);

        splitPane.setDividerLocation(250);

        // Add selection listener to petList
        petList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Pet selectedPet = petList.getSelectedValue();
                if (selectedPet != null) {
                    updatePetDetails(selectedPet);
                    refreshMeasurementHistory(selectedPet.getId());
                }
            }
        });
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(UIManager.getColor("Panel.background"));
        leftPanel.setPreferredSize(new Dimension(300, 0)); // Sesuaikan ukuran agar tombol tidak terpotong

        petListModel = new DefaultListModel<>();
        petList = new JList<>(petListModel);
        petList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        petList.setCellRenderer(new PetListCellRenderer());

        JScrollPane listScrollPane = new JScrollPane(petList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("All Pets"));
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        return leftPanel;
    }

    private void showMeasurementsDialog() {
        Pet selectedPet = petList.getSelectedValue();
        if (selectedPet == null)
            return;

        JDialog dialog = new JDialog();
        dialog.setTitle("Update Measurements for " + selectedPet.getName());
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel dateLabel = new JLabel("Date:");
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date());

        JLabel weightLabel = new JLabel("Weight (kg):");
        JTextField weightField = new JTextField(String.format("%.2f", selectedPet.getWeight()));

        JLabel lengthLabel = new JLabel("Length (cm):");
        JTextField lengthField = new JTextField(String.format("%.2f", selectedPet.getLength()));

        JLabel notesLabel = new JLabel("Notes:");
        JTextArea notesArea = new JTextArea();
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        formPanel.add(dateLabel);
        formPanel.add(dateChooser);
        formPanel.add(weightLabel);
        formPanel.add(weightField);
        formPanel.add(lengthLabel);
        formPanel.add(lengthField);
        formPanel.add(notesLabel);
        formPanel.add(notesScroll);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                double weight = Double.parseDouble(weightField.getText());
                double length = Double.parseDouble(lengthField.getText());
                String notes = notesArea.getText();

                // Create measurement record
                PetMeasurement measurement = new PetMeasurement();
                measurement.setPetId(selectedPet.getId());
                measurement.setRecordedAt(new Timestamp(dateChooser.getDate().getTime()));
                measurement.setWeight(weight);
                measurement.setLength(length);
                measurement.setNotes(notes);

                // Save to database
                MeasurementDAO measurementDAO = new MeasurementDAO();
                if (measurementDAO.addMeasurement(measurement)) {
                    // Update current pet values
                    selectedPet.setWeight(weight);
                    selectedPet.setLength(length);
                    petDAO.updatePet(selectedPet);

                    JOptionPane.showMessageDialog(dialog, "Measurements saved successfully!");
                    updatePetDetails(selectedPet);
                    refreshMeasurementHistory(selectedPet.getId());
                    dialog.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for weight and length",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createButtonPanel() {
        // Ubah layout menjadi 2 baris, 2 kolom dengan jarak horizontal dan vertikal 5
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton addPetButton = new JButton("Add New Pet");
        JButton editPetButton = new JButton("Edit Pet");
        JButton measurementsButton = new JButton("Update Measurements");
        deletePetButton = new JButton("Delete"); // Asumsi 'deletePetButton' dideklarasikan di level kelas

        // Awalnya nonaktifkan tombol yang memerlukan pilihan
        editPetButton.setEnabled(false);
        deletePetButton.setEnabled(false);
        measurementsButton.setEnabled(false);

        Dimension buttonSize = new Dimension(100, 30); // setPreferredSize mungkin tidak berpengaruh banyak di
                                                       // GridLayout
        addPetButton.setPreferredSize(buttonSize);
        editPetButton.setPreferredSize(buttonSize);
        measurementsButton.setPreferredSize(buttonSize);
        deletePetButton.setPreferredSize(buttonSize);

        addPetButton.addActionListener(e -> showAddPetDialog());
        editPetButton.addActionListener(e -> showEditPetDialog());
        measurementsButton.addActionListener(e -> showMeasurementsDialog());
        deletePetButton.addActionListener(e -> deleteSelectedPet());

        // Tambahkan listener untuk mengaktifkan/menonaktifkan tombol berdasarkan
        // pilihan
        petList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = petList.getSelectedValue() != null;
                editPetButton.setEnabled(hasSelection);
                measurementsButton.setEnabled(hasSelection);
                deletePetButton.setEnabled(hasSelection); // Tambahkan ini juga
            }
        });

        buttonPanel.add(addPetButton);
        buttonPanel.add(editPetButton);
        buttonPanel.add(measurementsButton);
        buttonPanel.add(deletePetButton);

        return buttonPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(UIManager.getColor("Panel.background"));

        JPanel topDetailsPanel = createTopDetailsPanel();
        rightPanel.add(topDetailsPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = createHistoryTabbedPane();
        rightPanel.add(tabbedPane, BorderLayout.CENTER);

        return rightPanel;
    }

    private JPanel createTopDetailsPanel() {
        JPanel topDetailsPanel = new JPanel(new BorderLayout(10, 10));
        topDetailsPanel.setBackground(UIManager.getColor("Panel.background"));

        // Details panel
        petDetailsArea = new JTextArea();
        petDetailsArea.setEditable(false);
        petDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        petDetailsArea.setLineWrap(true); // Tambahkan agar teks tidak meluas horizontal
        petDetailsArea.setWrapStyleWord(true);
        JScrollPane detailsScrollPane = new JScrollPane(petDetailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Pet Details"));
        detailsScrollPane.setPreferredSize(new Dimension(0, 200)); // Tetapkan tinggi tetap

        // Image panel
        petImageLabel = new JLabel();
        petImageLabel.setPreferredSize(new Dimension(150, 150));
        petImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        petImageLabel.setBorder(BorderFactory.createTitledBorder("Photo"));

        topDetailsPanel.add(detailsScrollPane, BorderLayout.CENTER);
        topDetailsPanel.add(petImageLabel, BorderLayout.EAST);

        return topDetailsPanel;
    }

    private JTabbedPane createHistoryTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Care History Tab
        historyTableModel = new DefaultTableModel(new Object[] { "Date", "Care Type", "Notes" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        tabbedPane.addTab("Care History", new JScrollPane(historyTable));

        // Growth History Tab
        measurementTableModel = new DefaultTableModel(
                new Object[] { "Date", "Weight (kg)", "Length (cm)", "Notes" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        measurementTable = new JTable(measurementTableModel);

        // Set custom renderer for date column
        measurementTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Timestamp) {
                    value = dateFormat.format(value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
            }
        });

        tabbedPane.addTab("Growth History", new JScrollPane(measurementTable));

        return tabbedPane;
    }

    private void refreshMeasurementHistory(int petId) {
        MeasurementDAO measurementDAO = new MeasurementDAO();
        List<PetMeasurement> measurements = measurementDAO.getMeasurementsByPetId(petId);

        measurementTableModel.setRowCount(0); // Clear existing data

        for (PetMeasurement m : measurements) {
            measurementTableModel.addRow(new Object[] {
                    m.getRecordedAt(),
                    String.format("%.2f", m.getWeight()),
                    String.format("%.2f", m.getLength()),
                    m.getNotes()
            });
        }
    }

    private void updatePetDetails(Pet pet) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(pet.getName()).append("\n");
        details.append("Type: ").append(pet.getType()).append("\n");
        details.append("Age: ").append(pet.getAge()).append(" years\n");
        details.append("Gender: ").append(pet.getGender()).append("\n");
        details.append("Weight: ").append(String.format("%.2f kg", pet.getWeight())).append("\n");
        details.append("Length: ").append(String.format("%.2f cm", pet.getLength())).append("\n");
        details.append("\nNotes:\n").append(pet.getNotes());

        petDetailsArea.setText(details.toString());

        // Update image
        if (pet.getImagePath() != null && !pet.getImagePath().isEmpty()) {
            ImageIcon icon = new ImageIcon(pet.getImagePath());
            if (icon.getImage() != null) {
                Image image = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                petImageLabel.setIcon(new ImageIcon(image));
            } else {
                petImageLabel.setIcon(null);
            }
        } else {
            petImageLabel.setIcon(null);
        }
    }

    private class PetListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Pet) {
                setText(((Pet) value).getName());
                setIcon(getScaledPetIcon((Pet) value, 30, 30));
            }
            return this;
        }
    }

    private void showAddPetDialog() {
        AddPetForm form = new AddPetForm(this, mainMenu);
        showFormDialog("Add New Pet", form);
        // Tambahkan ini agar HomePanel refresh setelah dialog ditutup
        if (mainMenu != null)
            mainMenu.refreshData();
    }

    private void showEditPetDialog() {
        Pet selectedPet = petList.getSelectedValue();
        if (selectedPet != null) {
            AddPetForm form = new AddPetForm(this, mainMenu, selectedPet);
            JDialog dialog = new JDialog();
            dialog.setTitle("Edit Pet");
            dialog.setContentPane(form);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setModal(true);
            dialog.setVisible(true);

            // Refresh after editing
            loadPets();
            if (mainMenu != null)
                mainMenu.refreshData(); // Tambahkan ini
        }
    }

    private void showFormDialog(String title, JPanel form) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setContentPane(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    private ImageIcon getScaledPetIcon(Pet pet, int width, int height) {
        if (pet.getImagePath() != null && !pet.getImagePath().isEmpty()) {
            File imageFile = new File(pet.getImagePath());
            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(pet.getImagePath());
                Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(image);
            }
        }
        return null;
    }

    public void loadPets() {
        List<Pet> pets = petDAO.getAllPets(); // Ensure petDAO is connected to the database
        petListModel.clear();
        for (Pet pet : pets) {
            petListModel.addElement(pet);
        }
    }

    private void deleteSelectedPet() {
        Pet selectedPet = petList.getSelectedValue();
        if (selectedPet == null) {
            JOptionPane.showMessageDialog(this, "Please select a pet to delete.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + selectedPet.getName()
                        + "'?\nThis will also delete all associated schedules and care history.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (petDAO.deletePet(selectedPet.getId())) {
                JOptionPane.showMessageDialog(this, "Pet deleted successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadPets();
                if (mainMenu != null)
                    mainMenu.refreshData(); // Tambahkan ini
            } else {
                handleError("Failed to delete pet.", null);
            }
        }
    }

    private void handleError(String message, Exception e) {
        if (e != null)
            e.printStackTrace();
        JOptionPane.showMessageDialog(this, message + (e != null ? ": " + e.getMessage() : ""), "Error",
                JOptionPane.ERROR_MESSAGE);
    }

}
