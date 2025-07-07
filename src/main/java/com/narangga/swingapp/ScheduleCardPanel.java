package com.narangga.swingapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import com.narangga.swingapp.util.ScheduleUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScheduleCardPanel extends JPanel {
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 50);

    public ScheduleCardPanel(Schedule schedule, String petName) {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 15, 10, 15));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        setOpaque(false); // Important for custom painting

        // Pet Photo Placeholder
        JLabel petPhoto = new JLabel();
        petPhoto.setPreferredSize(new Dimension(70, 70));
        petPhoto.setOpaque(true);
        petPhoto.setBackground(new Color(220, 220, 220));
        petPhoto.setHorizontalAlignment(SwingConstants.CENTER);
        petPhoto.setText("Photo"); // Placeholder, can be replaced with actual pet photo
        petPhoto.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(petPhoto, BorderLayout.WEST);

        // Details Panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        JLabel petNameLabel = new JLabel(petName);
        petNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        LocalDateTime nextOccurrence = ScheduleUtils.getNextOccurrence(schedule);
        String timeString;
        String countdownString;

        if (nextOccurrence != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy HH:mm");
            timeString = formatter.format(nextOccurrence);
            countdownString = ScheduleUtils.getCountdownString(nextOccurrence);
        } else {
            timeString = "No upcoming schedule";
            countdownString = "Finished";
        }

        JLabel timeLabel = new JLabel(timeString);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel categoryLabel = new JLabel(schedule.getCategory());
        categoryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        categoryLabel.setForeground(Color.GRAY);

        detailsPanel.add(petNameLabel);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(timeLabel);
        detailsPanel.add(categoryLabel);
        add(detailsPanel, BorderLayout.CENTER);

        // Countdown Label
        JLabel countdownLabel = new JLabel(countdownString, SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        countdownLabel.setForeground(new Color(255, 100, 100));
        add(countdownLabel, BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw shadow
        g2d.setColor(SHADOW_COLOR);
        g2d.fillRoundRect(2, 2, getWidth() - 3, getHeight() - 3, 12, 12);

        // Draw background
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 8, 8);

        g2d.dispose();
        super.paintComponent(g);
    }
}
