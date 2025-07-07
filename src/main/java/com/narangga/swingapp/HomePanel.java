package com.narangga.swingapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;

public class HomePanel extends JPanel {

    private static final Color BACKGROUND_COLOR = new Color(255, 248, 234); // #FFF8EA
    private PetDAO petDAO;
    private ScheduleDAO scheduleDAO;
    private JPanel upcomingPanel;
    private JPanel statsPanel;
    private StatCardPanel totalPetsCard;
    private StatCardPanel scheduleCountsCard;

    // Tambahkan referensi ke SchedulePanel
    private SchedulePanel schedulePanel;

    public HomePanel() {
        this.petDAO = new PetDAO();
        this.scheduleDAO = new ScheduleDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Top Panel for Stats ---
        statsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        statsPanel.setOpaque(false);

        // Ambil data pets dan jadwal SEKARANG
        int totalPets = petDAO.getAllPets().size();
        int dailySchedules = 0;
        try {
            dailySchedules = scheduleDAO.getAllSchedules().size();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        totalPetsCard = new StatCardPanel("Total Pets", String.valueOf(totalPets), new Color(125, 204, 244));
        scheduleCountsCard = new StatCardPanel("Daily Schedules", String.valueOf(dailySchedules), new Color(152, 216, 170));
        statsPanel.add(totalPetsCard);
        statsPanel.add(scheduleCountsCard);

        // --- Center Panel for Upcoming Schedule ---
        upcomingPanel = new JPanel();
        upcomingPanel.setLayout(new BoxLayout(upcomingPanel, BoxLayout.Y_AXIS));
        upcomingPanel.setOpaque(false);
        upcomingPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel upcomingTitle = new JLabel("Beranda");
        upcomingTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        upcomingTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        upcomingPanel.add(upcomingTitle);
        upcomingPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        loadUpcomingSchedules();

        JScrollPane scrollPane = new JScrollPane(upcomingPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(statsPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Setter agar MainMenu bisa inject SchedulePanel
    public void setSchedulePanel(SchedulePanel schedulePanel) {
        this.schedulePanel = schedulePanel;
    }

    public void refreshData() {
        System.out.println("[HomePanel] Refreshing data...");
        int totalPets = petDAO.getAllPets().size();
        int dailySchedules = 0;
        try {
            dailySchedules = scheduleDAO.getAllSchedules().size();
        } catch (SQLException e) {
            System.err.println("[HomePanel] Error fetching schedule count: " + e.getMessage());
            e.printStackTrace();
        }

        statsPanel.removeAll();
        totalPetsCard = new StatCardPanel("Total Pets", String.valueOf(totalPets), new Color(125, 204, 244));
        scheduleCountsCard = new StatCardPanel("Daily Schedules", String.valueOf(dailySchedules), new Color(152, 216, 170));
        statsPanel.add(totalPetsCard);
        statsPanel.add(scheduleCountsCard);
        statsPanel.revalidate();
        statsPanel.repaint();
        System.out.println("[HomePanel] Stats updated.");

        upcomingPanel.removeAll();
        JLabel upcomingTitle = new JLabel("Beranda");
        upcomingTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        upcomingTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        upcomingPanel.add(upcomingTitle);
        upcomingPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        loadUpcomingSchedules();

        if (schedulePanel != null) {
            schedulePanel.loadSchedules();
            System.out.println("[HomePanel] SchedulePanel reloaded.");
        }
    }

    private void loadUpcomingSchedules() {
        System.out.println("[HomePanel] Loading upcoming schedules...");
        try {
            List<Schedule> allSchedules = scheduleDAO.getAllSchedules();

            java.time.LocalDate todayDate = java.time.LocalDate.now();
            List<Schedule> todaySchedules = new ArrayList<>();
            List<Schedule> completedSchedules = new ArrayList<>();

            for (Schedule schedule : allSchedules) {
                java.time.LocalDateTime schedTime = com.narangga.swingapp.util.ScheduleUtils.getNextOccurrence(schedule);
                if (schedTime != null && schedTime.toLocalDate().equals(todayDate)) {
                    if (schedule.isActive()) {
                        todaySchedules.add(schedule);
                    } else {
                        completedSchedules.add(schedule);
                    }
                }
            }

            // Section: Jadwal Hari Ini Tersisa
            JLabel sisaLabel = new JLabel("Jadwal Hari Ini Tersisa (" + todaySchedules.size() + ")");
            sisaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            sisaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            upcomingPanel.add(sisaLabel);
            upcomingPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            if (todaySchedules.isEmpty()) {
                JLabel none = new JLabel("Tidak ada jadwal tersisa hari ini.");
                none.setAlignmentX(Component.LEFT_ALIGNMENT);
                upcomingPanel.add(none);
            } else {
                for (Schedule schedule : todaySchedules) {
                    Pet pet = petDAO.getPet(schedule.getPetId());
                    String petName = (pet != null) ? pet.getName() : "Unknown Pet";
                    JPanel card = createScheduleCard(schedule, petName, true);
                    upcomingPanel.add(card);
                    upcomingPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }

            // Section: Jadwal Hari Ini Terselesaikan
            JLabel selesaiLabel = new JLabel("Jadwal Hari Ini Terselesaikan (" + completedSchedules.size() + ")");
            selesaiLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            selesaiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            upcomingPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            upcomingPanel.add(selesaiLabel);
            upcomingPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            if (completedSchedules.isEmpty()) {
                JLabel none = new JLabel("Belum ada jadwal yang terselesaikan hari ini.");
                none.setAlignmentX(Component.LEFT_ALIGNMENT);
                upcomingPanel.add(none);
            } else {
                for (Schedule schedule : completedSchedules) {
                    Pet pet = petDAO.getPet(schedule.getPetId());
                    String petName = (pet != null) ? pet.getName() : "Unknown Pet";
                    JPanel card = createScheduleCard(schedule, petName, false);
                    upcomingPanel.add(card);
                    upcomingPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }

            // Jadwal berikutnya yang akan datang (paling dekat)
            Schedule nextSchedule = null;
            java.time.LocalDateTime soonest = null;
            for (Schedule schedule : allSchedules) {
                if (schedule.isActive()) {
                    java.time.LocalDateTime schedTime = com.narangga.swingapp.util.ScheduleUtils.getNextOccurrence(schedule);
                    if (schedTime != null && (soonest == null || schedTime.isBefore(soonest))) {
                        soonest = schedTime;
                        nextSchedule = schedule;
                    }
                }
            }
            if (nextSchedule != null) {
                upcomingPanel.add(Box.createRigidArea(new Dimension(0, 30)));
                JLabel nextLabel = new JLabel("Jadwal Berikutnya:");
                nextLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
                nextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                upcomingPanel.add(nextLabel);

                Pet pet = petDAO.getPet(nextSchedule.getPetId());
                String petName = (pet != null) ? pet.getName() : "Unknown Pet";
                JPanel card = createScheduleCard(nextSchedule, petName, true);
                upcomingPanel.add(card);
            }

        } catch (SQLException e) {
            System.err.println("[HomePanel] Error loading upcoming schedules: " + e.getMessage());
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading schedules.");
            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            upcomingPanel.add(errorLabel);
        }
        upcomingPanel.revalidate();
        upcomingPanel.repaint();
        System.out.println("[HomePanel] Upcoming schedules loaded.");
    }

    private JPanel createScheduleCard(Schedule schedule, String petName, boolean showCompleteButton) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(schedule.isActive() ? new Color(230, 255, 230) : new Color(220, 220, 220));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(schedule.isActive() ? new Color(125, 204, 244) : new Color(149, 165, 166), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel petLabel = new JLabel("🐾 " + petName);
        petLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel careTypeLabel = new JLabel("Jenis: " + schedule.getCareType());
        careTypeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel categoryLabel = new JLabel("Kategori: " + schedule.getCategory());
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        java.time.LocalDateTime scheduleTime = java.time.LocalDateTime.ofInstant(
            schedule.getScheduleTime().toInstant(),
            java.time.ZoneId.systemDefault()
        );
        JLabel timeLabel = new JLabel(String.format("Waktu: %02d:%02d", scheduleTime.getHour(), scheduleTime.getMinute()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        card.add(petLabel);
        card.add(careTypeLabel);
        card.add(categoryLabel);
        card.add(timeLabel);

        if (showCompleteButton && schedule.isActive()) {
            JButton completeButton = new JButton("Selesai");
            completeButton.addActionListener(e -> markScheduleAsComplete(schedule));
            card.add(completeButton);
        }

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private void markScheduleAsComplete(Schedule schedule) {
        try {
            schedule.setActive(false);
            scheduleDAO.updateSchedule(schedule);
            JOptionPane.showMessageDialog(this, "Jadwal berhasil ditandai sebagai selesai!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}