package com.narangga.swingapp.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

import com.narangga.swingapp.dao.MeasurementDAO;
import com.narangga.swingapp.dao.PetDAO;
import com.narangga.swingapp.dao.ScheduleDAO;
import com.narangga.swingapp.form.AddPetForm;
import com.narangga.swingapp.model.Pet;
import com.narangga.swingapp.model.PetMeasurement;
import com.narangga.swingapp.model.Schedule;
import com.narangga.swingapp.model.ScheduleInstance;
import com.narangga.swingapp.util.DatabaseConnection;
import com.toedter.calendar.JDateChooser;

public class PetManagerPanel extends JPanel {
    private JList<Pet> petList;
    private DefaultListModel<Pet> petListModel;
    private JTextArea petDetailsArea;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private PetDAO petDAO;
    private ScheduleDAO scheduleDAO;
    private JLabel petImageLabel;
    private JButton deletePetButton;
    private MainMenu mainMenu;
    private JTable measurementTable;
    private DefaultTableModel measurementTableModel;

    public PetManagerPanel(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        this.petDAO = new PetDAO();
        this.scheduleDAO = new ScheduleDAO(); // Inisialisasi ScheduleDAO
        initializeUI();
        loadPets();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(UIManager.getColor("Panel.background"));

        // label tanggal dan waktu di atas panel
        JLabel dateTimeLabel = new JLabel(getCurrentDateTimeString());
        dateTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dateTimeLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(dateTimeLabel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        add(splitPane, BorderLayout.CENTER);

        JPanel leftPanel = createLeftPanel();
        splitPane.setLeftComponent(leftPanel);

        JPanel rightPanel = createRightPanel();
        splitPane.setRightComponent(rightPanel);

        splitPane.setDividerLocation(200);

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

                // membuat object petmeasurement
                PetMeasurement measurement = new PetMeasurement();
                measurement.setPetId(selectedPet.getId());
                measurement.setRecordedAt(new Timestamp(dateChooser.getDate().getTime()));
                measurement.setWeight(weight);
                measurement.setLength(length);
                measurement.setNotes(notes);

                // simpan measurement ke database
                MeasurementDAO measurementDAO = new MeasurementDAO();
                if (measurementDAO.addMeasurement(measurement)) {
                    // langsung update pet dengan measurement baru
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
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5)); // Ubah dari 4 ke 5
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton addPetButton = new JButton("Tambah Peliharaan");
        JButton editPetButton = new JButton("Edit Peliharaan");
        JButton measurementsButton = new JButton("Update Perkembangan");
        JButton exportButton = new JButton("Ekspor Data"); // Tombol baru

        editPetButton.setEnabled(false);
        deletePetButton = new JButton("Hapus Peliharaan");
        deletePetButton.setEnabled(false);

        // sebelum tombol delete/ edit bisa digunakan, harus ada pet yang dipilih, sehingga disable dulu
        editPetButton.setEnabled(false);
        deletePetButton.setEnabled(false);
        measurementsButton.setEnabled(false);
        exportButton.setEnabled(false);

        Dimension buttonSize = new Dimension(100, 30);
        addPetButton.setPreferredSize(buttonSize);
        editPetButton.setPreferredSize(buttonSize);
        measurementsButton.setPreferredSize(buttonSize);
        exportButton.setPreferredSize(buttonSize);
        deletePetButton.setPreferredSize(buttonSize);

        addPetButton.addActionListener(e -> showAddPetDialog());
        editPetButton.addActionListener(e -> showEditPetDialog());
        measurementsButton.addActionListener(e -> showMeasurementsDialog());
        exportButton.addActionListener(e -> exportPetData()); // Action baru
        deletePetButton.addActionListener(e -> deleteSelectedPet());

        // enable/disable tombol berdasarkan seleksi di petList
        petList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = petList.getSelectedValue() != null;
                editPetButton.setEnabled(hasSelection);
                measurementsButton.setEnabled(hasSelection);
                exportButton.setEnabled(hasSelection); // Enable jika ada seleksi
                deletePetButton.setEnabled(hasSelection);
            }
        });

        buttonPanel.add(addPetButton);
        buttonPanel.add(editPetButton);
        buttonPanel.add(measurementsButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(deletePetButton);

        return buttonPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(UIManager.getColor("Panel.background"));

        JPanel topDetailsPanel = createTopDetailsPanel();
        topDetailsPanel.setPreferredSize(new Dimension(300, 200));
        rightPanel.add(topDetailsPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = createHistoryTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 200));
        rightPanel.add(tabbedPane, BorderLayout.CENTER);

        return rightPanel;
    }

    private JPanel createTopDetailsPanel() {
        JPanel topDetailsPanel = new JPanel(new BorderLayout(0, 10));
        topDetailsPanel.setBorder(BorderFactory.createTitledBorder("Profil Peliharaan"));
        topDetailsPanel.setBackground(UIManager.getColor("Panel.background"));

        JPanel wrap = new JPanel (new GridLayout(0,2));


        petDetailsArea = new JTextArea();
        petDetailsArea.setEditable(false);
        petDetailsArea.setFont(new Font("Aptos", Font.PLAIN, 14));
        JScrollPane detailsScrollPane = new JScrollPane(petDetailsArea);
        wrap.add(detailsScrollPane);

        petImageLabel = new JLabel();
        petImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrap.add(petImageLabel);

        topDetailsPanel.add(wrap, BorderLayout.CENTER);
        return topDetailsPanel;
    }

    private JTabbedPane createHistoryTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Care History Tab
        historyTableModel = new DefaultTableModel(new Object[] { "Tanggal", "Judul", "Catatan", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setShowHorizontalLines(true);
        historyTable.setShowVerticalLines(true);
        historyTable.setIntercellSpacing(new Dimension(1, 1));
        historyTable.setGridColor(new Color(189, 195, 199));
        historyTable.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
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
        measurementTable.setShowHorizontalLines(true);
        measurementTable.setShowVerticalLines(true);
        measurementTable.setIntercellSpacing(new Dimension(1, 1));
        measurementTable.setGridColor(new Color(189, 195, 199));
        measurementTable.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));

        //renderer untuk tanggal
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

        // semua jadwal milik pet (X) di care history
        try {
            refreshScheduleHistory(pet.getId());
        } catch (SQLException ex) {
            handleError("Gagal memuat jadwal perawatan.", ex);
        }
    }

    // menampilkan seluruh jadwal yang dimiliki suatu pet
    private void refreshScheduleHistory(int petId) throws SQLException {
        List<Schedule> schedules = scheduleDAO.getSchedulesByPetId(petId);
        historyTableModel.setRowCount(0); // Clear existing data

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        for (Schedule s : schedules) {
            if ("Once".equalsIgnoreCase(s.getRecurrence())) {
                // Untuk jadwal once, tampilkan dari tabel schedules
                historyTableModel.addRow(new Object[] {
                    dateFormat.format(s.getScheduleTime()),
                    s.getCareType(),
                    s.getNotes(),
                    s.isActive() ? "Belum" : "Selesai"
                });
            } else {
                // Untuk jadwal recurring, tampilkan dari schedule_instances yang sudah selesai
                try {
                    List<ScheduleInstance> instances = getCompletedInstancesByScheduleId(s.getId());
                    for (ScheduleInstance instance : instances) {
                        historyTableModel.addRow(new Object[] {
                            dateFormat.format(instance.getDate()),
                            s.getCareType(),
                            instance.getNotes() != null ? instance.getNotes() : s.getNotes(),
                            "Selesai"
                        });
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private List<ScheduleInstance> getCompletedInstancesByScheduleId(int scheduleId) throws SQLException {
        List<ScheduleInstance> instances = new ArrayList<>();
        String sql = "SELECT * FROM schedule_instances WHERE schedule_id = ? AND is_done = 1 ORDER BY date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scheduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ScheduleInstance instance = new ScheduleInstance(
                        rs.getInt("schedule_id"),
                        rs.getDate("date"),
                        rs.getBoolean("is_done"),
                        rs.getString("notes")
                    );
                    instance.setId(rs.getInt("id"));
                    instances.add(instance);
                }
            }
        }
        return instances;
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

        if (mainMenu != null) mainMenu.refreshData();
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
            if (mainMenu != null) mainMenu.refreshData(); // Tambahkan ini
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
        List<Pet> pets = petDAO.getAllPets();
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
                if (mainMenu != null) mainMenu.refreshData(); // Tambahkan ini
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

    private String getCurrentDateTimeString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm", new Locale("id", "ID"));
        return now.format(formatter);
    }

    private void exportPetData() {
        Pet selectedPet = petList.getSelectedValue();
        if (selectedPet == null) {
            JOptionPane.showMessageDialog(this, "Pilih peliharaan untuk diekspor!", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Simpan Data Peliharaan");
        fileChooser.setSelectedFile(new java.io.File(selectedPet.getName() + "_data.txt"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileToSave)) {
                // Header informasi pet
                writer.println("=== DATA PELIHARAAN ===");
                writer.println("Nama: " + selectedPet.getName());
                writer.println("Jenis: " + selectedPet.getType());
                writer.println("Umur: " + selectedPet.getAge() + " tahun");
                writer.println("Jenis Kelamin: " + selectedPet.getGender());
                writer.println("Berat: " + String.format("%.2f kg", selectedPet.getWeight()));
                writer.println("Panjang: " + String.format("%.2f cm", selectedPet.getLength()));
                writer.println("Catatan: " + (selectedPet.getNotes() != null ? selectedPet.getNotes() : "-"));
                writer.println();

                // Riwayat perawatan
                writer.println("=== RIWAYAT PERAWATAN ===");
                for (int i = 0; i < historyTableModel.getRowCount(); i++) {
                    String tanggal = historyTableModel.getValueAt(i, 0).toString();
                    String tipe = historyTableModel.getValueAt(i, 1).toString();
                    String catatan = historyTableModel.getValueAt(i, 2).toString();
                    String status = historyTableModel.getValueAt(i, 3).toString();
                    
                    writer.println(tanggal + " | " + tipe + " | " + catatan + " | " + status);
                }
                writer.println();

                // Riwayat perkembangan
                writer.println("=== RIWAYAT PERKEMBANGAN ===");
                for (int i = 0; i < measurementTableModel.getRowCount(); i++) {
                    String tanggal = measurementTableModel.getValueAt(i, 0).toString();
                    String berat = measurementTableModel.getValueAt(i, 1).toString();
                    String panjang = measurementTableModel.getValueAt(i, 2).toString();
                    String catatan = measurementTableModel.getValueAt(i, 3).toString();
                    
                    writer.println(tanggal + " | Berat: " + berat + " kg | Panjang: " + panjang + " cm | " + catatan);
                }

                JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke: " + fileToSave.getAbsolutePath(),
                        "Ekspor Berhasil", JOptionPane.INFORMATION_MESSAGE);
                        
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal mengekspor data: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshCurrentPetDetails() {
        Pet selectedPet = petList.getSelectedValue();
        if (selectedPet != null) {
            updatePetDetails(selectedPet);
            refreshMeasurementHistory(selectedPet.getId());
        }
    }
}
