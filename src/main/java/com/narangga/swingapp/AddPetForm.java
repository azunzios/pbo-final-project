package com.narangga.swingapp;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class AddPetForm extends JFrame {
    private JTextField nameField;
    private JComboBox<String> typeComboBox;
    private JTextField birthDateField;
    private JTextField imagePathField; // Field baru untuk path gambar
    private PetManagerFrame petManagerFrame;
    private MainMenu mainMenu;

    public AddPetForm(PetManagerFrame petManagerFrame, MainMenu mainMenu) {
        this.petManagerFrame = petManagerFrame;
        this.mainMenu = mainMenu;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Add New Pet");
        setSize(450, 350); // Ukuran disesuaikan
        setLocationRelativeTo(petManagerFrame);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout()); // Menggunakan GridBagLayout untuk fleksibilitas
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        nameField = new JTextField();
        formPanel.add(nameField, gbc);

        // Type
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        String[] petTypes = {"Dog", "Cat", "Bird", "Fish", "Other..."};
        typeComboBox = new JComboBox<>(petTypes);
        formPanel.add(typeComboBox, gbc);

        // Birth Date
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Birth Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        birthDateField = new JTextField();
        formPanel.add(birthDateField, gbc);

        // Image Path
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Image Path:"), gbc);

        JPanel imagePanel = new JPanel(new BorderLayout(5,0));
        imagePathField = new JTextField();
        imagePathField.setEditable(false); // Hanya bisa diisi melalui JFileChooser
        imagePanel.add(imagePathField, BorderLayout.CENTER);

        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> selectImage());
        imagePanel.add(browseButton, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(imagePanel, gbc);


        typeComboBox.addActionListener(e -> {
            if (Objects.equals(typeComboBox.getSelectedItem(), "Other...")) {
                String newType = JOptionPane.showInputDialog(this, "Enter new pet type:");
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
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> savePet());
        cancelButton.addActionListener(e -> dispose());
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Pet Image");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            imagePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void savePet() {
        try {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pet name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Pet pet = new Pet();
            pet.setName(nameField.getText());
            pet.setType((String) typeComboBox.getSelectedItem());
            pet.setImagePath(imagePathField.getText()); // Simpan path gambar

            if (!birthDateField.getText().trim().isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsedDate = sdf.parse(birthDateField.getText());
                pet.setBirthDate(new Date(parsedDate.getTime()));
            }

            new PetDAO().addPet(pet);
            JOptionPane.showMessageDialog(this, "Pet saved successfully!");

            if (petManagerFrame != null) {
                petManagerFrame.loadPets();
            }
            if (mainMenu != null) {
                mainMenu.loadSchedules();
            }

            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}