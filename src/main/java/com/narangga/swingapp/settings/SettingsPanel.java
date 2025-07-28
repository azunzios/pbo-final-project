package com.narangga.swingapp.settings;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class SettingsPanel extends JPanel {
    private final JCheckBox requireNotesCheckbox;

    public SettingsPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Section: Catatan
        JLabel notesLabel = new JLabel("Pengaturan Catatan:");
        notesLabel.setFont(notesLabel.getFont().deriveFont(Font.BOLD, 14f));
        add(notesLabel, gbc);

        requireNotesCheckbox = new JCheckBox("Wajibkan catatan saat menyelesaikan jadwal");
        add(requireNotesCheckbox, gbc);

        // Section: Notifikasi
        JLabel notifLabel = new JLabel("Pengaturan Notifikasi:");
        notifLabel.setFont(notifLabel.getFont().deriveFont(Font.BOLD, 14f));
        add(notifLabel, gbc);

        // Tombol simpan
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        JButton saveButton = new JButton("Simpan Pengaturan");
        saveButton.addActionListener(e -> saveSettings());
        add(saveButton, gbc);

        loadSettings();
    }

    private void loadSettings() {
        UserSettings settings = UserSettings.getCurrentSettings();
        requireNotesCheckbox.setSelected(settings.isRequireNotes());
    }

    private void saveSettings() {
        UserSettings settings = UserSettings.getCurrentSettings();
        settings.setRequireNotes(requireNotesCheckbox.isSelected());
        settings.save();

        JOptionPane.showMessageDialog(this,
            "Pengaturan berhasil disimpan",
            "Sukses",
            JOptionPane.INFORMATION_MESSAGE);
    }
}