package com.narangga.swingapp.panel;

import com.narangga.swingapp.dao.PetDAO;
import com.narangga.swingapp.dao.ScheduleDAO;
import com.narangga.swingapp.dao.ScheduleInstanceDAO;
import com.narangga.swingapp.model.Pet;
import com.narangga.swingapp.model.Schedule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class HomePanel extends JPanel {

    private PetDAO petDAO;
    private ScheduleDAO scheduleDAO;
    private JPanel upcomingPanel;
    private JPanel statsPanel;
    private StatCardPanel totalPetsCard;
    private StatCardPanel scheduleCountsCard;

    private SchedulePanel schedulePanel;

    public HomePanel() {
        this.petDAO = new PetDAO();
        this.scheduleDAO = new ScheduleDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(249, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel centerWrapperPanel = new JPanel(new BorderLayout(10, 10));
        centerWrapperPanel.setOpaque(false);

        JPanel dateTimePanel = new JPanel();
        dateTimePanel.setOpaque(false);
        dateTimePanel.setLayout(new BoxLayout(dateTimePanel, BoxLayout.Y_AXIS));
        JLabel dateTimeLabel = new JLabel(getCurrentDateTimeString());
        dateTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dateTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateTimePanel.add(dateTimeLabel);

        add(dateTimePanel, BorderLayout.NORTH);

        statsPanel = new JPanel(new GridLayout(1, 2, 20, 10));
        statsPanel.setOpaque(false);

        int totalPets = petDAO.getAllPets().size();
        int dailySchedules = 0;
        try {
            // Hanya hitung jadwal yang tanggalnya hari ini (baik aktif maupun selesai)
            java.time.LocalDate todayDate = java.time.LocalDate.now();
            List<Schedule> allSchedules = scheduleDAO.getAllSchedules();
            for (Schedule schedule : allSchedules) {
                java.time.LocalDate scheduleDate = java.time.LocalDate.ofInstant(
                    schedule.getScheduleTime().toInstant(),
                    java.time.ZoneId.systemDefault());
                if (scheduleDate.equals(todayDate)) {
                    dailySchedules++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        totalPetsCard = new StatCardPanel("Jumlah Peliharaan", String.valueOf(totalPets),
                new Color(224, 242, 254), new ImageIcon("icons/paw.png"));
        scheduleCountsCard = new StatCardPanel("Banyak Jadwal Hari ini", String.valueOf(dailySchedules),
                new Color(220, 252, 231), new ImageIcon("icons/calendar-day.png"));
        statsPanel.add(totalPetsCard);
        statsPanel.add(scheduleCountsCard);

        centerWrapperPanel.add(statsPanel, BorderLayout.NORTH);

        upcomingPanel = new JPanel();
        upcomingPanel.setLayout(new BoxLayout(upcomingPanel, BoxLayout.Y_AXIS));
        upcomingPanel.setOpaque(false);
        upcomingPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        loadUpcomingSchedules();

        JScrollPane scrollPane = new JScrollPane(upcomingPanel);

        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);


        centerWrapperPanel.add(scrollPane, BorderLayout.CENTER);


        add(centerWrapperPanel, BorderLayout.CENTER);

    }

    private String getCurrentDateTimeString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm", new Locale("id", "ID"));
        return now.format(formatter);
    }

    public void refreshData() {
        System.out.println("[HomePanel] Refreshing data...");
        int totalPets = petDAO.getAllPets().size();
        int dailySchedules = 0;
        try {
            java.time.LocalDate todayDate = java.time.LocalDate.now();
            List<Schedule> allSchedules = scheduleDAO.getAllSchedules();
            dailySchedules = 0;
            for (Schedule schedule : allSchedules) {
                java.time.LocalDate scheduleDate = java.time.LocalDate.ofInstant(
                        schedule.getScheduleTime().toInstant(),
                        java.time.ZoneId.systemDefault());
                if (scheduleDate.equals(todayDate)) {
                    dailySchedules++;
                }
            }
        } catch (SQLException e) {
            System.err.println("[HomePanel] Error fetching schedule count: " + e.getMessage());
            e.printStackTrace();
        }

        statsPanel.removeAll();
        totalPetsCard = new StatCardPanel("Jumlah Peliharaan", String.valueOf(totalPets), new Color(125, 204, 244),
                new ImageIcon("icons/paw.png"));
        scheduleCountsCard = new StatCardPanel("Jumlah Jadwal Hari ini", String.valueOf(dailySchedules),
                new Color(152, 216, 170), new ImageIcon("icons/paw.png"));
        statsPanel.add(totalPetsCard);
        statsPanel.add(scheduleCountsCard);
        statsPanel.revalidate();
        statsPanel.repaint();

        upcomingPanel.removeAll();
        loadUpcomingSchedules();

        if (schedulePanel != null) {
            schedulePanel.loadSchedules();
        }
    }

    private JPanel createScheduleCard(Schedule schedule, String petName, boolean showCompleteButton) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getScheduleColor(schedule.getRecurrence()), 4), // Border tebal dengan warna
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        card.setMaximumSize(new Dimension(500, 200));
        card.setPreferredSize(new Dimension(340, 200));

        JLabel petLabel = new JLabel(petName);
        petLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel careTypeLabel = new JLabel("Jenis: " + schedule.getCareType());
        careTypeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel categoryLabel = new JLabel("Kategori: " + schedule.getCategory());
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        java.time.LocalDateTime scheduleTime = java.time.LocalDateTime.ofInstant(
                schedule.getScheduleTime().toInstant(),
                java.time.ZoneId.systemDefault());
        JLabel timeLabel = new JLabel(
                String.format("Waktu: %02d:%02d", scheduleTime.getHour(), scheduleTime.getMinute()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel remainingTimeLabel = createRemainingTimeLabel(scheduleTime);

        card.add(petLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(careTypeLabel);
        card.add(categoryLabel);
        card.add(timeLabel);
        card.add(remainingTimeLabel);

        if (showCompleteButton) {
            JButton completeButton = new JButton("Selesaikan");
            completeButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
            completeButton.addActionListener(e -> markScheduleAsComplete(schedule));
            card.add(Box.createVerticalStrut(8));
            card.add(completeButton);
        }

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private void loadUpcomingSchedules() {
        System.out.println("[HomePanel] Loading upcoming schedules...");
        try {
            List<Schedule> allSchedules = scheduleDAO.getAllSchedules();

            java.time.LocalDate todayDate = java.time.LocalDate.now();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            List<Schedule> todayActiveSchedules = new ArrayList<>();
            List<Schedule> completedSchedules = new ArrayList<>();

            for (Schedule schedule : allSchedules) {
                if (!schedule.isActive()) continue; // Skip inactive schedules
                
                boolean occursToday = doesScheduleOccurToday(schedule, todayDate);
                if (occursToday) {
                    boolean isCompleted = isScheduleCompletedToday(schedule, todayDate);
                    if (isCompleted) {
                        completedSchedules.add(schedule);
                    } else {
                        todayActiveSchedules.add(schedule);
                    }
                }
            }

            // Sort by urgency - negatif. yang terlewat lebih dulu
            todayActiveSchedules.sort((s1, s2) -> {
                java.time.LocalDateTime time1 = java.time.LocalDateTime.ofInstant(s1.getScheduleTime().toInstant(), java.time.ZoneId.systemDefault());
                java.time.LocalDateTime time2 = java.time.LocalDateTime.ofInstant(s2.getScheduleTime().toInstant(), java.time.ZoneId.systemDefault());
                
                java.time.LocalDateTime today1 = todayDate.atTime(time1.toLocalTime());
                java.time.LocalDateTime today2 = todayDate.atTime(time2.toLocalTime());
                
                long diff1 = java.time.Duration.between(now, today1).toMinutes();
                long diff2 = java.time.Duration.between(now, today2).toMinutes();

                return Long.compare(diff1, diff2);
            });

            // maksimal 2 jadwal aja
            List<Schedule> displaySchedules = todayActiveSchedules.size() > 2 
                ? todayActiveSchedules.subList(0, 2) 
                : todayActiveSchedules;

            // ngebuat Section untuk Jadwal Hari Ini Tersisa (status == 0)
            JLabel sisaLabel = new JLabel("Jadwal Hari Ini Tersisa");
            sisaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            sisaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            upcomingPanel.add(sisaLabel);
            upcomingPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            JLabel sisaCount = new JLabel(String.valueOf(todayActiveSchedules.size()));
            sisaCount.setFont(new Font("Segoe UI", Font.BOLD, 32));
            sisaCount.setAlignmentX(Component.LEFT_ALIGNMENT);
            upcomingPanel.add(sisaCount);
            upcomingPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Tampilkan kartu jadwal yang belum diselesaikan atau pesan jika kosong
            if (displaySchedules.isEmpty()) {
                JLabel noScheduleLabel = new JLabel("Tidak ada jadwal tersisa hari ini");
                noScheduleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                noScheduleLabel.setForeground(Color.GRAY);
                noScheduleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                noScheduleLabel.setPreferredSize(new Dimension(340, 200));
                upcomingPanel.add(noScheduleLabel);
            } else {
                JPanel gridPanel = new JPanel(new GridLayout(0, 2, 12, 12));
                gridPanel.setOpaque(false);
                for (Schedule schedule : displaySchedules) {
                    Pet pet = petDAO.getPet(schedule.getPetId());
                    String petName = (pet != null) ? pet.getName() : "Unknown Pet";
                    JPanel card = createScheduleCard(schedule, petName, true);
                    gridPanel.add(card);
                }
                gridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                upcomingPanel.add(gridPanel);
            }

            upcomingPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Jadwal hari ini klo terselesaikan (status == 1)
            JLabel selesaiLabel = new JLabel("Jadwal Hari Ini Terselesaikan");
            selesaiLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            selesaiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            upcomingPanel.add(selesaiLabel);
            upcomingPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            JLabel selesaiCount = new JLabel(String.valueOf(completedSchedules.size()));
            selesaiCount.setFont(new Font("Segoe UI", Font.BOLD, 32));
            selesaiCount.setAlignmentX(Component.LEFT_ALIGNMENT);
            upcomingPanel.add(selesaiCount);

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

    private boolean doesScheduleOccurToday(Schedule schedule, java.time.LocalDate todayDate) {
        String recurrence = schedule.getRecurrence();
        java.time.LocalDate scheduleDate = java.time.LocalDate.ofInstant(
            schedule.getScheduleTime().toInstant(),
            java.time.ZoneId.systemDefault()
        );

        if ("Once".equalsIgnoreCase(recurrence)) {
            return scheduleDate.equals(todayDate);
        } else if ("Daily".equalsIgnoreCase(recurrence)) {
            return true; // Daily schedules occur every day
        } else if ("Weekly".equalsIgnoreCase(recurrence)) {
            String days = schedule.getDays() == null ? "" : schedule.getDays().toLowerCase();
            String todayName = todayDate.getDayOfWeek().getDisplayName(
                java.time.format.TextStyle.FULL, new java.util.Locale("id", "ID")).toLowerCase();
            return days.contains(todayName);
        } else if ("Monthly".equalsIgnoreCase(recurrence)) {
            return todayDate.getDayOfMonth() == scheduleDate.getDayOfMonth();
        }
        return false;
    }

    private boolean isScheduleCompletedToday(Schedule schedule, java.time.LocalDate todayDate) {
        String recurrence = schedule.getRecurrence();
        if ("Once".equalsIgnoreCase(recurrence)) {
            return !schedule.isActive(); // For once schedules, check is_active
        } else {
            // For recurring schedules, check schedule_instances
            try {
                return new ScheduleInstanceDAO().isInstanceDone(schedule.getId(), java.sql.Date.valueOf(todayDate));
            } catch (SQLException e) {
                return false;
            }
        }
    }

    private JLabel createRemainingTimeLabel(java.time.LocalDateTime scheduleTime) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // Gabungkan tanggal hari ini dengan waktu jadwal
        java.time.LocalDateTime todayScheduleTime = today.atTime(scheduleTime.toLocalTime());
        
        long diffMinutes = java.time.Duration.between(now, todayScheduleTime).toMinutes();
        
        String remainingText;
        Color textColor;
        
        if (diffMinutes < 0) {
            remainingText = "Terlewat";
            textColor = Color.RED;
        } else if (diffMinutes < 60) {
            remainingText = diffMinutes + " menit lagi";
            textColor = new Color(255, 140, 0); // Orange
        } else {
            long hours = diffMinutes / 60;
            long minutes = diffMinutes % 60;
            if (minutes == 0) {
                remainingText = hours + " jam lagi";
            } else {
                remainingText = hours + " jam " + minutes + " menit lagi";
            }
            textColor = new Color(0, 150, 0); // Green
        }
        
        JLabel remainingTimeLabel = new JLabel(remainingText);
        remainingTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        remainingTimeLabel.setForeground(textColor);
        
        return remainingTimeLabel;
    }

    private Color getScheduleColor(String recurrence) {
        if (recurrence == null)
            return new Color(230, 255, 230);
        switch (recurrence.toLowerCase()) {
            case "once":
                return new Color(46, 204, 113); // Emerald
            case "daily":
                return new Color(155, 89, 182); // Amethyst
            case "weekly":
                return new Color(52, 152, 219); // Blue
            case "monthly":
                return new Color(231, 76, 60); // Red
            default:
                return new Color(230, 255, 230);
        }
    }

    private void markScheduleAsComplete(Schedule schedule) {
        try {
            java.time.LocalDate todayDate = java.time.LocalDate.now();
            String recurrence = schedule.getRecurrence();
            
            if ("Once".equalsIgnoreCase(recurrence)) {
                // For once schedules, set is_active to false
                schedule.setActive(false);
                scheduleDAO.updateSchedule(schedule);
            } else {
                // For recurring schedules, add to schedule_instances
                String notes = null;
                boolean requireNotes = com.narangga.swingapp.settings.UserSettings.getCurrentSettings().isRequireNotes();
                if (requireNotes) {
                    notes = JOptionPane.showInputDialog(
                        this,
                        "Tambahkan catatan (opsional):",
                        "Catatan Perawatan",
                        JOptionPane.PLAIN_MESSAGE
                    );
                }
                new ScheduleInstanceDAO().addInstance(
                    schedule.getId(),
                    java.sql.Date.valueOf(todayDate),
                    true,
                    notes
                );
            }
            
            JOptionPane.showMessageDialog(this, "Jadwal berhasil ditandai sebagai selesai!", "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshData();
            
            // Refresh PetManagerPanel jika ada
            java.awt.Component comp = this;
            while (comp != null && !(comp instanceof MainMenu)) {
                comp = comp.getParent();
            }
            if (comp instanceof MainMenu mainMenu) {
                mainMenu.refreshPetManagerPanel();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Tambahkan setter agar MainMenu bisa inject SchedulePanel
    public void setSchedulePanel(SchedulePanel schedulePanel) {
        this.schedulePanel = schedulePanel;
    }
}