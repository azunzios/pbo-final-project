package com.narangga.swingapp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.Date;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.toedter.calendar.JDateChooser;

public class AddPetForm extends JPanel {
    private JTextField nameField;
    private JTextField weightTF;
    private JTextField lengthTF;

    private JComboBox<String> typeComboBox;
    private JDateChooser birthDateChooser;
    private JTextField imagePathField;
    private JComboBox<String> genderComboBox;
    private JTextArea notesArea;
    private PetManagerPanel petManagerPanel;
    private MainMenu mainMenu;
    private Pet petToEdit;

    public AddPetForm(PetManagerPanel petManagerPanel, MainMenu mainMenu) {
        this(petManagerPanel, mainMenu, null);
    }

    public AddPetForm(PetManagerPanel petManagerPanel, MainMenu mainMenu, Pet petToEdit) {
        this.petManagerPanel = petManagerPanel;
        this.mainMenu = mainMenu;
        this.petToEdit = petToEdit;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(500, 450)); // Increased height to accommodate new fields

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Row counter
        int row = 0;

        // Nama
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0;
        nameField = new JTextField();
        formPanel.add(nameField, gbc);

        // Jenis
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Jenis:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        String[] petTypes = {"Anjing", "Kucing", "Burung", "Ikan", "Lainnya..."};
        typeComboBox = new JComboBox<>(petTypes);
        formPanel.add(typeComboBox, gbc);

        // Tanggal Lahir
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Tanggal Lahir:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        birthDateChooser = new JDateChooser();
        birthDateChooser.setDateFormatString("dd-MM-yyyy");
        formPanel.add(birthDateChooser, gbc);

        // Jenis Kelamin
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        genderComboBox = new JComboBox<>(new String[]{"Jantan", "Betina", "Tidak Diketahui"});
        formPanel.add(genderComboBox, gbc);

        // Berat
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Berat (kg):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        weightTF = new JTextField();
        weightTF.setInputVerifier(new NumericInputVerifier(0, 1000));
        formPanel.add(weightTF, gbc);

        // Panjang
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Panjang (cm):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        lengthTF = new JTextField();
        lengthTF.setInputVerifier(new NumericInputVerifier(0, 2000));
        formPanel.add(lengthTF, gbc);

        // Gambar
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Path Gambar:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        JPanel imagePanel = new JPanel(new BorderLayout(5,0));
        imagePathField = new JTextField();
        imagePathField.setEditable(false);
        imagePanel.add(imagePathField, BorderLayout.CENTER);
        JButton browseButton = new JButton("Pilih...");
        browseButton.addActionListener(e -> selectImage());
        imagePanel.add(browseButton, BorderLayout.EAST);
        formPanel.add(imagePanel, gbc);

        // Catatan
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Catatan:"), gbc);
    gbc.gridx = 1; gbc.gridy = row;
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        formPanel.add(notesScroll, gbc);

        // Load data if in edit mode
        if (petToEdit != null) {
            loadPetData();
        }

        typeComboBox.addActionListener(evt -> {
            if (Objects.equals(typeComboBox.getSelectedItem(), "Lainnya...")) {
                String newType = JOptionPane.showInputDialog(this, "Masukkan jenis hewan baru:");
                if (newType != null && !newType.trim().isEmpty()) {
                    typeComboBox.insertItemAt(newType, 0);
                    typeComboBox.setSelectedItem(newType);
                } else {
                    typeComboBox.setSelectedIndex(0);
                }
            }
        });

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(petToEdit != null ? "Simpan Perubahan" : "Simpan");
        JButton cancelButton = new JButton("Batal");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(evt -> savePet());
        cancelButton.addActionListener(evt -> closeWindow());
        browseButton.addActionListener(evt -> selectImage());
    }

    private void loadPetData() {
        nameField.setText(petToEdit.getName());
        typeComboBox.setSelectedItem(petToEdit.getType());
        if (petToEdit.getBirthDate() != null) {
            birthDateChooser.setDate(petToEdit.getBirthDate());
        }
        genderComboBox.setSelectedItem(petToEdit.getGender());
        weightTF.setText(String.valueOf(petToEdit.getWeight()));  // Fixed: convert double to String
        lengthTF.setText(String.valueOf(petToEdit.getLength()));  // Fixed: was using weightTF twice
        imagePathField.setText(petToEdit.getImagePath());
        notesArea.setText(petToEdit.getNotes());
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih Gambar Hewan");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            imagePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void closeWindow() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

    private void savePet() {
        if (!validateInput()) {
            return;
        }

        try {
            Pet pet = getPetData();
            PetDAO petDAO = new PetDAO();

            if (petToEdit != null) {
                petDAO.updatePet(pet);
                JOptionPane.showMessageDialog(this, "Data hewan berhasil diperbarui!");
            } else {
                petDAO.addPet(pet);
                JOptionPane.showMessageDialog(this, "Hewan baru berhasil disimpan!");
            }

            if (petManagerPanel != null) {
                petManagerPanel.loadPets();
            }
            if (mainMenu != null) {
                mainMenu.refreshData();
            }

            closeWindow();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama hewan tidak boleh kosong.", "Error Validasi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public Pet getPetData() {
        Pet pet = petToEdit != null ? petToEdit : new Pet();

        pet.setName(nameField.getText());
        pet.setType((String) typeComboBox.getSelectedItem());
        pet.setGender((String) genderComboBox.getSelectedItem());
        pet.setImagePath(imagePathField.getText());
        pet.setNotes(notesArea.getText());

        // Fixed: Proper double parsing
        try {
            pet.setWeight(Double.parseDouble(weightTF.getText()));
        } catch (NumberFormatException e) {
            pet.setWeight(0.0);
        }

        try {
            pet.setLength(Double.parseDouble(lengthTF.getText()));
        } catch (NumberFormatException e) {
            pet.setLength(0.0);
        }

        if (birthDateChooser.getDate() != null) {
            pet.setBirthDate(new Date(birthDateChooser.getDate().getTime()));
        }

        return pet;
    }
    private static class NumericInputVerifier extends InputVerifier {
        private final double min;
        private final double max;

        public NumericInputVerifier(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean verify(JComponent input) {
            String text = ((JTextField) input).getText();
            try {
                double value = Double.parseDouble(text);
                return value >= min && value <= max;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        public boolean shouldYieldFocus(JComponent input) {
            boolean valid = verify(input);
            if (!valid) {
                JOptionPane.showMessageDialog(input,
                        String.format("Masukkan angka antara %.1f dan %.1f", min, max),
                        "Input Tidak Valid",
                        JOptionPane.WARNING_MESSAGE);
                ((JTextField) input).selectAll();
            }
            return valid;
        }
    }
}