package com.narangga.swingapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class MainMenu extends JFrame {
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private ScheduleDAO scheduleDAO;
    private PetDAO petDAO;

    public MainMenu() {
        this.scheduleDAO = new ScheduleDAO();
        this.petDAO = new PetDAO();
        initializeUI();
        loadSchedules();
        startCountdownTimer();
    }
    private DayOfWeek parseDay(String day) {
        String trimmedDay = day.trim().toLowerCase();
        switch (trimmedDay) {
            case "mon": case "monday": return DayOfWeek.MONDAY;
            case "tue": case "tuesday": return DayOfWeek.TUESDAY;
            case "wed": case "wednesday": return DayOfWeek.WEDNESDAY;
            case "thu": case "thursday": return DayOfWeek.THURSDAY;
            case "fri": case "friday": return DayOfWeek.FRIDAY;
            case "sat": case "saturday": return DayOfWeek.SATURDAY;
            case "sun": case "sunday": return DayOfWeek.SUNDAY;
            default: return null;
        }
    }
    private void initializeUI() {
        setTitle("Pet Care Scheduler");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(mainPanel);

        JLabel titleLabel = new JLabel("Today's Schedules", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Kolom baru untuk hitung mundur
        String[] columnNames = {"ID", "Pet Name", "Care Type", "Time", "Days", "Countdown"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scheduleTable = new JTable(tableModel);
        scheduleTable.setFillsViewportHeight(true);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addScheduleButton = new JButton("Add Schedule");
        JButton managePetsButton = new JButton("Manage Pets"); // Tombol diubah
        JButton markDoneButton = new JButton("Mark as Done");
        JButton exportButton = new JButton("Export to CSV");

        // Tombol Add Pet dihapus dari sini
        buttonPanel.add(addScheduleButton);
        buttonPanel.add(managePetsButton);
        buttonPanel.add(markDoneButton);
        buttonPanel.add(exportButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Listeners diupdate
        addScheduleButton.addActionListener(e -> new AddScheduleForm(this).setVisible(true));
        managePetsButton.addActionListener(e -> new PetManagerFrame(this).setVisible(true));
        markDoneButton.addActionListener(e -> markScheduleDone());
        exportButton.addActionListener(e -> exportSchedulesToCSV());
    }

    // Wrapper untuk menyimpan jadwal dan waktu kejadian berikutnya
    private static class ScheduleWrapper implements Comparable<ScheduleWrapper> {
        final Schedule schedule;
        final LocalDateTime nextOccurrence;

        ScheduleWrapper(Schedule schedule, LocalDateTime nextOccurrence) {
            this.schedule = schedule;
            this.nextOccurrence = nextOccurrence;
        }

        @Override
        public int compareTo(ScheduleWrapper other) {
            if (nextOccurrence == null && other.nextOccurrence == null) return 0;
            if (nextOccurrence == null) return 1;
            if (other.nextOccurrence == null) return -1;
            return nextOccurrence.compareTo(other.nextOccurrence);
        }
    }

    private LocalDateTime calculateNextOccurrence(Schedule schedule) {
        LocalTime scheduleTime = schedule.getScheduleTime().toLocalTime();
        Set<DayOfWeek> scheduledDays = new HashSet<>();
        if (schedule.getDays() != null && !schedule.getDays().trim().isEmpty()) {
            for (String dayStr : schedule.getDays().split(",")) {
                DayOfWeek day = parseDay(dayStr);
                if (day != null) {
                    scheduledDays.add(day);
                } else {
                    System.err.println("Invalid day format ignored: " + dayStr);
                }
            }
        }

        if (scheduledDays.isEmpty()) return null;

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 7; i++) {
            LocalDateTime nextCheck = now.plusDays(i);
            if (scheduledDays.contains(nextCheck.getDayOfWeek())) {
                LocalDateTime potentialOccurrence = nextCheck.with(scheduleTime);
                if (potentialOccurrence.isAfter(now)) {
                    return potentialOccurrence;
                }
            }
        }
        return null;
    }

    public void loadSchedules() {
        try {
            List<Schedule> schedules = scheduleDAO.getAllSchedules();
            List<ScheduleWrapper> wrappedSchedules = new ArrayList<>();
            for (Schedule schedule : schedules) {
                LocalDateTime nextOccurrence = calculateNextOccurrence(schedule);
                if (nextOccurrence != null) {
                    wrappedSchedules.add(new ScheduleWrapper(schedule, nextOccurrence));
                }
            }

            Collections.sort(wrappedSchedules); // Sorting berdasarkan waktu terdekat

            tableModel.setRowCount(0);
            for (ScheduleWrapper wrapper : wrappedSchedules) {
                Pet pet = petDAO.getPet(wrapper.schedule.getPetId());
                String petName = (pet != null) ? pet.getName() : "Unknown Pet";
                Vector<Object> row = new Vector<>();
                row.add(wrapper.schedule.getId());
                row.add(petName);
                row.add(wrapper.schedule.getCareType());
                row.add(wrapper.schedule.getScheduleTime());
                row.add(wrapper.schedule.getDays());
                row.add(formatCountdown(wrapper.nextOccurrence)); // Tambah data countdown
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            handleError("Failed to load schedules", e);
        }
    }

    private String formatCountdown(LocalDateTime nextOccurrence) {
        if (nextOccurrence == null) return "N/A";
        Duration duration = Duration.between(LocalDateTime.now(), nextOccurrence);
        long days = duration.toDays();
        long hours = duration.toHours() - days * 24;
        long minutes = duration.toMinutes() - duration.toHours() * 60;
        long seconds = duration.getSeconds() - duration.toMinutes() * 60;

        if (days > 0) return String.format("%d day(s), %02d:%02d", days, hours, minutes);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void startCountdownTimer() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Update countdown di UI thread
                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        int scheduleId = (int) tableModel.getValueAt(i, 0);
                        try {
                            Schedule schedule = scheduleDAO.getSchedule(scheduleId);
                            LocalDateTime nextOccurrence = calculateNextOccurrence(schedule);
                            tableModel.setValueAt(formatCountdown(nextOccurrence), i, 5);
                        } catch (SQLException e) {
                            // ignore
                        }
                    }
                });
            }
        }, 0, 1000); // Update setiap 1 detik
    }


    private void markScheduleDone() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow != -1) {
            int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                Schedule selected = scheduleDAO.getSchedule(scheduleId);
                if (selected == null) {
                    JOptionPane.showMessageDialog(this, "Schedule not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JPanel panel = new JPanel(new GridLayout(0, 1));
                JTextField notesField = new JTextField(20);
                panel.add(new JLabel("Notes:"));
                panel.add(notesField);
                int result = JOptionPane.showConfirmDialog(this, panel, "Mark as Done",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    CareLog log = new CareLog(selected.getPetId(), selected.getCareType(), new Timestamp(System.currentTimeMillis()));
                    log.setScheduleId(selected.getId());
                    log.setNotes(notesField.getText());
                    new CareLogDAO().addCareLog(log);
                    JOptionPane.showMessageDialog(this, "Care logged successfully!");
                    loadSchedules();
                }
            } catch (SQLException ex) {
                handleError("Failed to mark schedule as done", ex);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a schedule to mark as done.");
        }
    }

    private void exportSchedulesToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as CSV");
        fileChooser.setSelectedFile(new File("schedules.csv"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.append(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) writer.append(',');
                }
                writer.append('\n');
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        writer.append(tableModel.getValueAt(i, j).toString());
                        if (j < tableModel.getColumnCount() - 1) writer.append(',');
                    }
                    writer.append('\n');
                }
                JOptionPane.showMessageDialog(this, "Data exported successfully to " + fileToSave.getAbsolutePath());
            } catch (IOException e) {
                handleError("Error exporting data", e);
            }
        }
    }

    private void handleError(String message, Exception e) {
        if (e != null) e.printStackTrace();
        JOptionPane.showMessageDialog(this, message + (e != null ? ": " + e.getMessage() : ""), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}