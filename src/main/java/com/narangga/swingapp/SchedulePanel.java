package com.narangga.swingapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.narangga.swingapp.model.ScheduleTableModel;

public class SchedulePanel extends JPanel {
    // Enhanced color scheme inspired by modern game UI
    private static final Color ONCE_COLOR = new Color(46, 204, 113);        // Emerald
    private static final Color DAILY_COLOR = new Color(155, 89, 182);       // Amethyst
    private static final Color WEEKLY_COLOR = new Color(52, 152, 219);      // Blue
    private static final Color MONTHLY_COLOR = new Color(231, 76, 60);      // Red
    private static final Color COMPLETED_COLOR = new Color(149, 165, 166);  // Gray for completed
    
    // Game-like button colors
    private static final Color PRIMARY_BTN = new Color(41, 128, 185);
    private static final Color SUCCESS_BTN = new Color(39, 174, 96);
    private static final Color WARNING_BTN = new Color(243, 156, 18);
    private static final Color DANGER_BTN = new Color(231, 76, 60);
    private static final Color INFO_BTN = new Color(142, 68, 173);
    
    // Background colors
    private static final Color BG_PRIMARY = new Color(44, 62, 80);
    private static final Color BG_SECONDARY = new Color(52, 73, 94);
    private static final Color BG_LIGHT = new Color(236, 240, 241);

    private final MainMenu mainMenu;
    private final ScheduleDAO scheduleDAO;
    private final PetDAO petDAO;
    private JPanel calendarGrid;
    private JLabel weekLabel;
    private LocalDate currentWeekStart;
    private Map<Integer, Pet> petMap = new HashMap<>();
    private JPanel calendarPanel;
    private JPanel managePanel;
    private JTabbedPane tabbedPane;
    private JTable scheduleTable;
    private ScheduleTableModel tableModel;
    
    // Constants for detailed time grid - Modified to start from 05:00
    private static final int MINUTES_PER_HOUR = 60;
    private static final int MINUTE_INTERVAL = 15; // Show every 15 minutes
    private static final int SLOTS_PER_HOUR = MINUTES_PER_HOUR / MINUTE_INTERVAL;
    private static final int START_HOUR = 5; // Start from 05:00
    private static final int TOTAL_HOURS = 24; // Total 24 hours
    private static final int TOTAL_TIME_SLOTS = TOTAL_HOURS * SLOTS_PER_HOUR; // 24 hours * 4 slots per hour
    private static final int CELL_HEIGHT = 48; // Lebih tinggi untuk multi-line detail
    private static final int CELL_WIDTH = 180; // Perbesar lebar kolom kalender
    
    // For managing multiple schedules in same time slot
    private Map<String, List<Schedule>> schedulesByTimeSlot = new HashMap<>();

    public SchedulePanel(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        this.scheduleDAO = new ScheduleDAO();
        this.petDAO = new PetDAO();
        this.currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        initializeUI();
        loadSchedules();
    }

   private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(BG_LIGHT);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_SECONDARY);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Calendar Tab
        calendarPanel = createCalendarPanel();
        
        // Manage Tab
        managePanel = createManagePanel();

        tabbedPane.addTab("🗓️ Kalender", calendarPanel);
        tabbedPane.addTab("⚙️ Kelola Jadwal", managePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }


    private JPanel createManagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        
        // Initialize table
        tableModel = new ScheduleTableModel(new ArrayList<>(), new ArrayList<>());
        scheduleTable = new JTable(tableModel);
        scheduleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scheduleTable.setRowHeight(25);
        scheduleTable.setGridColor(new Color(189, 195, 199));
        scheduleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        scheduleTable.getTableHeader().setBackground(BG_SECONDARY);
        scheduleTable.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        
        // Enhanced button panel with game-like styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(BG_LIGHT);
        
        JButton editButton = createGameButton("✏️ Edit", SUCCESS_BTN);
        JButton deleteButton = createGameButton("🗑️ Hapus", DANGER_BTN);
        
        editButton.addActionListener(e -> editSelectedSchedule());
        deleteButton.addActionListener(e -> deleteSelectedSchedule());
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

     private JButton createGameButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient effect
                Color lightColor = new Color(
                    Math.min(255, baseColor.getRed() + 30),
                    Math.min(255, baseColor.getGreen() + 30),
                    Math.min(255, baseColor.getBlue() + 30)
                );
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, lightColor,
                    0, getHeight(), baseColor
                );
                
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Add subtle border
                g2d.setColor(baseColor.darker());
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 35));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setPreferredSize(new Dimension(125, 37));
                button.revalidate();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setPreferredSize(new Dimension(120, 35));
                button.revalidate();
            }
        });
        
        return button;
    }
        
    private void editSelectedSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow >= 0) {
            Schedule schedule = tableModel.getScheduleAt(selectedRow);
            AddScheduleForm form = new AddScheduleForm(this, schedule);
            form.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang ingin diedit!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }

private void deleteSelectedSchedule() {
    int selectedRow = scheduleTable.getSelectedRow();
    if (selectedRow >= 0) {
        Schedule schedule = tableModel.getScheduleAt(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda yakin ingin menghapus jadwal ini?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                System.out.println("[SchedulePanel] Deleting schedule with ID: " + schedule.getId());
                scheduleDAO.deleteSchedule(schedule.getId());
                loadSchedules();
                System.out.println("[SchedulePanel] Schedule deleted and reloaded.");

                // Tambahkan pemanggilan mainMenu.refreshData() untuk menyinkronkan data
                if (mainMenu != null) {
                    mainMenu.refreshData();
                    System.out.println("[SchedulePanel] MainMenu data refreshed after deletion.");
                }

                JOptionPane.showMessageDialog(this, "Jadwal berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                System.err.println("[SchedulePanel] Error deleting schedule: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Error menghapus jadwal: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    } else {
        JOptionPane.showMessageDialog(this, "Pilih jadwal yang ingin dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
    }
}

    private JPanel createCalendarPanel() {
        calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setBackground(BG_LIGHT);
        initializeCalendarPanel();
        return calendarPanel;
    }

    private void initializeCalendarPanel() {
        // Enhanced top panel with updated layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_SECONDARY);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Navigation panel - Previous and Next buttons side by side
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navPanel.setOpaque(false);
        
        JButton prevButton = createGameButton("⬅️ Sebelumnya", PRIMARY_BTN);
        prevButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            loadSchedules();
        });

        JButton nextButton = createGameButton("Berikutnya ➡️", PRIMARY_BTN);
        nextButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            loadSchedules();
        });

        navPanel.add(prevButton);
        navPanel.add(nextButton);

        // Date label with new format
        weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        weekLabel.setForeground(Color.WHITE);

        // Right panel with buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        JButton todayButton = createGameButton("📅 Hari Ini", INFO_BTN);
        todayButton.addActionListener(e -> {
            currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            loadSchedules();
        });

        JButton addButton = createGameButton("➕ Tambah Jadwal", SUCCESS_BTN);
        addButton.addActionListener(e -> {
            AddScheduleForm form = new AddScheduleForm(this);
            form.setVisible(true);
        });

        rightPanel.add(todayButton);
        rightPanel.add(addButton);

        topPanel.add(navPanel, BorderLayout.WEST);
        topPanel.add(weekLabel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        calendarPanel.add(topPanel, BorderLayout.NORTH);

        // Create enhanced calendar grid with detailed time slots
        calendarGrid = new JPanel();
        calendarGrid.setBackground(Color.WHITE);
        
        // Create scroll pane with both horizontal and vertical scrolling
        JScrollPane scrollPane = new JScrollPane(calendarGrid);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2));
        scrollPane.setPreferredSize(new Dimension(1200, 600)); // Perbesar default width
        
        // Percepat scroll mouse
        scrollPane.getVerticalScrollBar().setUnitIncrement(48 * 2); // 2x cell height
        scrollPane.getHorizontalScrollBar().setUnitIncrement(CELL_WIDTH);

        calendarPanel.add(scrollPane, BorderLayout.CENTER);
    }

     public void loadSchedules() {
        System.out.println("[SchedulePanel] Loading schedules...");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d", new Locale("id", "ID"));
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("id", "ID"));

        String startDay = currentWeekStart.format(formatter);
        String endDay = currentWeekStart.plusDays(6).format(formatter);
        String monthYear = currentWeekStart.format(monthFormatter);

        weekLabel.setText(startDay + " - " + endDay + " " + monthYear);

        SwingWorker<List<Schedule>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Schedule> doInBackground() throws Exception {
                petMap.clear();
                List<Pet> pets = petDAO.getAllPets();
                petMap = pets.stream().collect(Collectors.toMap(Pet::getId, pet -> pet));
                return scheduleDAO.getAllSchedules();
            }

            @Override
            protected void done() {
                try {
                    List<Schedule> allSchedules = get();
                    buildEnhancedCalendarGrid(allSchedules);
                    System.out.println("[SchedulePanel] Schedules loaded and calendar updated.");
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("[SchedulePanel] Error loading schedules: " + e.getMessage());
                    JOptionPane.showMessageDialog(SchedulePanel.this, "Gagal memuat jadwal.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();

        try {
            List<Schedule> schedules = scheduleDAO.getAllSchedules();
            List<Pet> pets = petDAO.getAllPets();
            if (tableModel != null) {
                tableModel.updateData(schedules, pets);
                System.out.println("[SchedulePanel] Table model updated.");
            }
        } catch (SQLException e) {
            System.err.println("[SchedulePanel] Error updating table model: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error loading schedules: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

       private void buildEnhancedCalendarGrid(List<Schedule> allSchedules) {
        calendarGrid.removeAll();
        calendarGrid.setLayout(new GridBagLayout());
        calendarGrid.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();

        // Bersihkan mapping jadwal sebelumnya
        schedulesByTimeSlot.clear();

        // Kelompokkan jadwal berdasarkan hari
        groupSchedulesByDay(allSchedules);

        // Buat header hari
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE\ndd MMM", new Locale("id", "ID"));
        for (int i = 0; i < 7; i++) {
            gbc.gridx = i;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.BOTH;

            JLabel dayLabel = new JLabel("<html><center>" +
                currentWeekStart.plusDays(i).format(dayFormatter).replace("\n", "<br>") +
                "</center></html>", SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dayLabel.setOpaque(true);
            dayLabel.setBackground(BG_SECONDARY);
            dayLabel.setForeground(Color.WHITE);
            dayLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(189, 195, 199)));
            dayLabel.setPreferredSize(new Dimension(CELL_WIDTH, 50));
            calendarGrid.add(dayLabel, gbc);
        }

        // Buat sel kalender untuk setiap hari
        for (int col = 0; col < 7; col++) {
            gbc.gridx = col;
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;

            JPanel dayColumn = new JPanel();
            dayColumn.setLayout(new BoxLayout(dayColumn, BoxLayout.Y_AXIS));
            dayColumn.setBackground(Color.WHITE);
            dayColumn.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(236, 240, 241)));

            LocalDate dayInWeek = currentWeekStart.plusDays(col);
            String dayKey = dayInWeek.toString();

            List<Schedule> schedulesInDay = schedulesByTimeSlot.get(dayKey);
            if (schedulesInDay != null && !schedulesInDay.isEmpty()) {
                for (Schedule schedule : schedulesInDay) {
                    JPanel scheduleCard = createEnhancedScheduleCard(schedule, 0);
                    dayColumn.add(scheduleCard);
                    dayColumn.add(Box.createRigidArea(new Dimension(0, 5))); // Spasi antar jadwal
                }
            }

            calendarGrid.add(dayColumn, gbc);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private void groupSchedulesByDay(List<Schedule> allSchedules) {
        for (Schedule schedule : allSchedules) {
            if (!schedule.isActive()) continue;

            LocalDateTime scheduleTime = LocalDateTime.ofInstant(
                schedule.getScheduleTime().toInstant(),
                ZoneId.systemDefault()
            );

            LocalDate scheduleDate = scheduleTime.toLocalDate();
            String dayKey = scheduleDate.toString();

            schedulesByTimeSlot.computeIfAbsent(dayKey, k -> new ArrayList<>()).add(schedule);
        }
    }

    // Tambahkan method untuk cek apakah jadwal sudah selesai (is_active == false)
    private boolean isScheduleCompleted(Schedule schedule) {
        return !schedule.isActive();
    }

    // Ubah createEnhancedScheduleCard agar menampilkan detail multi-line dan label selesai
    private JPanel createEnhancedScheduleCard(Schedule schedule, int index) {
        boolean completed = isScheduleCompleted(schedule);
        Color baseColor = completed ? COMPLETED_COLOR : getScheduleColor(schedule.getRecurrence());
        Color lightColor = completed
                ? COMPLETED_COLOR.brighter()
                : new Color(
                    Math.min(255, baseColor.getRed() + 40),
                    Math.min(255, baseColor.getGreen() + 40),
                    Math.min(255, baseColor.getBlue() + 40)
                );

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, lightColor,
                        0, getHeight(), baseColor
                );

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(baseColor.darker());
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(CELL_WIDTH - 8, CELL_HEIGHT - 4));
        card.setMaximumSize(new Dimension(CELL_WIDTH - 8, CELL_HEIGHT - 4));

        Pet pet = petMap.get(schedule.getPetId());
        final String petName = (pet != null) ? pet.getName() : "Deleted Pet";

        // Multi-line detail
        JLabel petLabel = new JLabel("🐾 " + petName);
        petLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        petLabel.setForeground(Color.WHITE);
        petLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel careTypeLabel = new JLabel("Jenis: " + schedule.getCareType());
        careTypeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        careTypeLabel.setForeground(Color.WHITE);
        careTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel categoryLabel = new JLabel("Kategori: " + schedule.getCategory());
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        categoryLabel.setForeground(Color.WHITE);
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        LocalDateTime scheduleTime = LocalDateTime.ofInstant(
            schedule.getScheduleTime().toInstant(),
            ZoneId.systemDefault()
        );
        JLabel timeLabel = new JLabel(String.format("Waktu: %02d:%02d", scheduleTime.getHour(), scheduleTime.getMinute()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(petLabel);
        card.add(careTypeLabel);
        card.add(categoryLabel);
        card.add(timeLabel);

        if (completed) {
            JLabel selesaiLabel = new JLabel("(Selesai)");
            selesaiLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            selesaiLabel.setForeground(Color.YELLOW);
            selesaiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(selesaiLabel);
        }

        card.setToolTipText(String.format(
            "<html><b>%s</b><br/>Jenis: %s<br/>Kategori: %s<br/>Waktu: %02d:%02d<br/>%s</html>",
            petName,
            schedule.getCareType(),
            schedule.getCategory(),
            scheduleTime.getHour(),
            scheduleTime.getMinute(),
            completed ? "<b>Status: Selesai</b>" : ""
        ));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!completed) {
                    showScheduleCompleteDialog(schedule, petName);
                    // Setelah selesai, tetap tampilkan blok dengan warna abu-abu
                    // Gunakan SchedulePanel.this agar tidak error akses parent
                    SchedulePanel.this.loadSchedules();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setPreferredSize(new Dimension(CELL_WIDTH - 4, CELL_HEIGHT));
                card.revalidate();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setPreferredSize(new Dimension(CELL_WIDTH - 8, CELL_HEIGHT - 4));
                card.revalidate();
            }
        });

        return card;
    }

    // Helper method to get color based on recurrence type
    private Color getScheduleColor(String recurrence) {
        if (recurrence == null) return ONCE_COLOR;
        switch (recurrence.toLowerCase()) {
            case "once":
                return ONCE_COLOR;
            case "daily":
                return DAILY_COLOR;
            case "weekly":
                return WEEKLY_COLOR;
            case "monthly":
                return MONTHLY_COLOR;
            default:
                return ONCE_COLOR;
        }
    }

    // Ubah showScheduleCompleteDialog agar update is_active dan notes sesuai user_settings
    private void showScheduleCompleteDialog(Schedule schedule, String petName) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            String.format("Tandai jadwal '%s' untuk '%s' sebagai selesai?", schedule.getCareType(), petName),
            "Konfirmasi Selesai",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
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

            CareLog log = new CareLog(schedule.getPetId(), schedule.getCareType(), new Timestamp(System.currentTimeMillis()));
            log.setScheduleId(schedule.getId());
            log.setNotes(notes);
            log.setDoneBy("Pengguna");

            try {
                new CareLogDAO().addCareLog(log);
                // Set is_active = false di database
                schedule.setActive(false);
                new ScheduleDAO().updateSchedule(schedule);
                JOptionPane.showMessageDialog(this, "Jadwal telah dicatat sebagai selesai!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                mainMenu.refreshData();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menyimpan catatan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public MainMenu getMainMenu() {
        return mainMenu;
    }
}