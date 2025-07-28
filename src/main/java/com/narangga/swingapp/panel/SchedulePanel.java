package com.narangga.swingapp.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
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
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import com.narangga.swingapp.model.Pet;
import com.narangga.swingapp.dao.PetDAO;
import com.narangga.swingapp.model.Schedule;
import com.narangga.swingapp.dao.ScheduleDAO;
import com.narangga.swingapp.dao.ScheduleInstanceDAO;
import com.narangga.swingapp.form.AddScheduleForm;
import com.narangga.swingapp.model.ScheduleTableModel;

public class SchedulePanel extends JPanel {

    private static final Color ONCE_COLOR = new Color(46, 204, 113); // Emerald
    private static final Color DAILY_COLOR = new Color(155, 89, 182); // Amethyst
    private static final Color WEEKLY_COLOR = new Color(52, 152, 219); // Blue
    private static final Color MONTHLY_COLOR = new Color(231, 76, 60); // Red

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

    private static final int CELL_HEIGHT = 150;

    private Map<String, List<Schedule>> schedulesByTimeSlot = new HashMap<>();

    // Map untuk menandai instance selesai per tanggal (jadwal recurring)
    private final Map<String, Boolean> finishedInstanceMap = new HashMap<>();

    private boolean doesScheduleOccurOn(Schedule schedule, LocalDate date) {
        String recurrence = schedule.getRecurrence();
        if ("Once".equalsIgnoreCase(recurrence)) {
            LocalDate scheduleDate = new java.sql.Date(schedule.getScheduleTime().getTime()).toLocalDate();
            return scheduleDate.equals(date);
        }
        if ("Daily".equalsIgnoreCase(recurrence)) {
            return true;
        }
        if ("Weekly".equalsIgnoreCase(recurrence) || (schedule.getDays() != null && !schedule.getDays().isEmpty())) {
            String dayOfWeekEn = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            String dayOfWeekId = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
            String days = schedule.getDays() == null ? "" : schedule.getDays().toLowerCase();
            return days.contains(dayOfWeekEn.toLowerCase()) || days.contains(dayOfWeekId.toLowerCase());
        }
        if ("Monthly".equalsIgnoreCase(recurrence)) {
            LocalDate scheduleDate = new java.sql.Date(schedule.getScheduleTime().getTime()).toLocalDate();
            return date.getDayOfMonth() == scheduleDate.getDayOfMonth();
        }
        return false;
    }

    private boolean isScheduleInstanceDone(Schedule schedule, LocalDate date) {
        try {
            return new ScheduleInstanceDAO().isInstanceDone(schedule.getId(), java.sql.Date.valueOf(date));
        } catch (SQLException e) {
            return false;
        }
    }

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

        // Tambahkan label tanggal dan waktu sekarang di atas tabbedPane
        JLabel dateTimeLabel = new JLabel(getCurrentDateTimeString());
        dateTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dateTimeLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(dateTimeLabel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        //pane kalender
        calendarPanel = createCalendarPanel();

        //panel manage (kelola jadwal)
        managePanel = createManagePanel();

        tabbedPane.addTab("Kalender", calendarPanel);
        tabbedPane.addTab("Kelola Jadwal", managePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private String getCurrentDateTimeString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm", new Locale("id", "ID"));
        return now.format(formatter);
    }

    private TableRowSorter<ScheduleTableModel> sorter;
    private JTextField searchField;

    private JPanel createManagePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        //membuat tabel2
        tableModel = new ScheduleTableModel(new ArrayList<>(), new ArrayList<>());
        scheduleTable = new JTable(tableModel);
        scheduleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scheduleTable.setRowHeight(25);
        scheduleTable.setGridColor(new Color(189, 195, 199));
        scheduleTable.setShowHorizontalLines(true);
        scheduleTable.setShowVerticalLines(true);
        scheduleTable.setIntercellSpacing(new Dimension(1, 1));
        scheduleTable.setBackground(Color.WHITE);
        scheduleTable.setForeground(Color.BLACK);
        scheduleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        scheduleTable.getTableHeader().setBackground(new Color(245, 245, 245));
        scheduleTable.getTableHeader().setForeground(Color.BLACK);
        scheduleTable.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        scheduleTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // sorting dan search
        sorter = new TableRowSorter<>(tableModel);
        scheduleTable.setRowSorter(sorter);

        // Panel search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel searchLabel = new JLabel("Cari:");
        searchField = new JTextField(20);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Listener untuk search
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }
        });

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2));

        // scroll speed
        scrollPane.getVerticalScrollBar().setUnitIncrement(96); // 2x cell height
        scrollPane.getHorizontalScrollBar().setUnitIncrement(120);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Hapus");
        JButton markDoneButton = new JButton("Tandai Selesai");

        editButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deleteButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        markDoneButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        editButton.addActionListener(e -> editSelectedSchedule());
        deleteButton.addActionListener(e -> deleteSelectedSchedule());
        markDoneButton.addActionListener(e -> markSelectedScheduleAsDone());

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(markDoneButton);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void editSelectedSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow >= 0) {
            Schedule schedule = tableModel.getScheduleAt(selectedRow);
            AddScheduleForm form = new AddScheduleForm(this, schedule);
            form.setVisible(true);
            // Setelah edit, refresh HomePanel melalui MainMenu
            if (mainMenu != null) {
                mainMenu.refreshData();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang ingin diedit!", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
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
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    scheduleDAO.deleteSchedule(schedule.getId());
                    loadSchedules();
                    // Setelah hapus, refresh HomePanel melalui MainMenu
                    if (mainMenu != null) {
                        mainMenu.refreshData();
                    }
                    JOptionPane.showMessageDialog(this, "Jadwal berhasil dihapus!", "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error menghapus jadwal: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang ingin dihapus!", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void markSelectedScheduleAsDone() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow >= 0) {
            Schedule schedule = tableModel.getScheduleAt(selectedRow);
            if (!schedule.isActive()) {
                JOptionPane.showMessageDialog(this, "Jadwal ini sudah selesai.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String notes = schedule.getNotes() == null ? "" : schedule.getNotes();
            String newNotes = notes;
            boolean requireNotes = com.narangga.swingapp.settings.UserSettings.getCurrentSettings().isRequireNotes();
            String userNotes = null;
            if (requireNotes) {
                userNotes = JOptionPane.showInputDialog(
                    this,
                    "Tambahkan catatan (opsional):",
                    "Catatan Perawatan",
                    JOptionPane.PLAIN_MESSAGE
                );
                if (userNotes != null && !userNotes.trim().isEmpty()) {
                    // Tambahkan catatan baru sebagai poin baru di bawah catatan lama, dipisah titik
                    if (!notes.trim().isEmpty()) {
                        newNotes = notes.trim() + ". " + userNotes.trim();
                    } else {
                        newNotes = userNotes.trim();
                    }
                }
            }
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tandai jadwal ini sebagai selesai?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    schedule.setActive(false);
                    schedule.setNotes(newNotes);
                    scheduleDAO.updateSchedule(schedule);
                    loadSchedules();
                    if (mainMenu != null) {
                        mainMenu.refreshData();
                    }
                    JOptionPane.showMessageDialog(this, "Jadwal berhasil ditandai sebagai selesai!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Gagal update status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang ingin ditandai selesai!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel createCalendarPanel() {
        calendarPanel = new JPanel(new BorderLayout());
        initializeCalendarPanel();
        return calendarPanel;
    }

    private void initializeCalendarPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navPanel.setOpaque(false);

        JButton prevButton = new JButton("Sebelumnya");
        prevButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        prevButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            loadSchedules();
        });

        JButton nextButton = new JButton("Berikutnya");
        nextButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nextButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            loadSchedules();
        });

        navPanel.add(prevButton);
        navPanel.add(nextButton);

        weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        weekLabel.setForeground(Color.BLACK);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JButton todayButton = new JButton("Hari Ini");
        todayButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        todayButton.addActionListener(e -> {
            currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            loadSchedules();
        });

        JButton addButton = new JButton("(+) Tambah Jadwal");
        addButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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

        calendarGrid = new JPanel();

        JScrollPane scrollPane = new JScrollPane(calendarGrid);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2));
        scrollPane.setPreferredSize(new Dimension(800, 600));

        // atur scroll speed lebih cepat
        scrollPane.getVerticalScrollBar().setUnitIncrement(96);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(120);

        calendarPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public void loadSchedules() {
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d", new Locale("id", "ID"));
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("id", "ID"));
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy", new Locale("id", "ID"));

        LocalDate start = currentWeekStart;
        LocalDate end = currentWeekStart.plusDays(6);

        String startDay = start.format(dayFormatter);
        String endDay = end.format(dayFormatter);
        String startMonth = start.format(monthFormatter);
        String endMonth = end.format(monthFormatter);
        String year = end.format(yearFormatter);

        String monthYearLabel;
        if (start.getMonth().equals(end.getMonth())) {
            monthYearLabel = startDay + " - " + endDay + " " + endMonth + " " + year;
        } else {
            //buat minggu untuk bulan yang berbeda
            monthYearLabel = startDay + " " + startMonth + " - " + endDay + " " + endMonth + " " + year;
        }
        weekLabel.setText(monthYearLabel);

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
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(SchedulePanel.this, "Gagal memuat jadwal.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();

        // update tabel
        try {
            List<Schedule> schedules = scheduleDAO.getAllSchedules();
            List<Pet> pets = petDAO.getAllPets();
            if (tableModel != null) {
                tableModel.updateData(schedules, pets);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading schedules: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildEnhancedCalendarGrid(List<Schedule> allSchedules) {
        calendarGrid.removeAll();
        calendarGrid.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        //clear data sebelumnya
        schedulesByTimeSlot.clear();
        finishedInstanceMap.clear();

        // ini grouping jadwal
        Map<LocalDate, List<Schedule>> schedulesByDay = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = currentWeekStart.plusDays(i);
            schedulesByDay.put(day, new ArrayList<>());
        }
        for (Schedule schedule : allSchedules) {
            for (int i = 0; i < 7; i++) {
                LocalDate day = currentWeekStart.plusDays(i);
                if (doesScheduleOccurOn(schedule, day)) {
                    schedulesByDay.get(day).add(schedule);
                }
            }
        }

        for (List<Schedule> daySchedules : schedulesByDay.values()) {
            daySchedules.sort((s1, s2) -> {
                LocalDateTime t1 = LocalDateTime.ofInstant(s1.getScheduleTime().toInstant(), ZoneId.systemDefault());
                LocalDateTime t2 = LocalDateTime.ofInstant(s2.getScheduleTime().toInstant(), ZoneId.systemDefault());
                return t1.toLocalTime().compareTo(t2.toLocalTime());
            });
        }

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM", new Locale("id", "ID"));
        for (int i = 0; i < 7; i++) {
            gbc.gridx = i;
            gbc.gridy = 0;
            gbc.weighty = 0;
            JLabel dayLabel = new JLabel(currentWeekStart.plusDays(i).format(dayFormatter), SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dayLabel.setOpaque(true);
            dayLabel.setBorder(BorderFactory.createMatteBorder(0, i == 0 ? 1 : 0, 2, 1, new Color(189, 195, 199)));
            dayLabel.setPreferredSize(new Dimension(160, 40));
            calendarGrid.add(dayLabel, gbc);
        }

        gbc.weighty = 1.0;
        for (int col = 0; col < 7; col++) {
            LocalDate day = currentWeekStart.plusDays(col);
            List<Schedule> schedules = schedulesByDay.get(day);

            gbc.gridx = col;
            gbc.gridy = 1;

            JPanel cellPanel = new JPanel();
            cellPanel.setLayout(new BoxLayout(cellPanel, BoxLayout.Y_AXIS));
            cellPanel.setBorder(BorderFactory.createMatteBorder(
                0,
                col == 0 ? 1 : 0,
                1,
                1,
                new Color(189, 195, 199)
            ));
            cellPanel.setPreferredSize(new Dimension(160, Math.max(CELL_HEIGHT, schedules.size() * CELL_HEIGHT)));
            cellPanel.setBackground(Color.WHITE);

            // Stack all schedules for this day, sorted by time
            for (Schedule schedule : schedules) {
                cellPanel.add(createScheduleDetailPanel(schedule, day));
                cellPanel.add(Box.createVerticalStrut(4)); // spacing between blocks
            }

            calendarGrid.add(cellPanel, gbc);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JPanel createScheduleDetailPanel(Schedule schedule, LocalDate day) {
        LocalDateTime scheduleTime = LocalDateTime.ofInstant(
            schedule.getScheduleTime().toInstant(),
            ZoneId.systemDefault()
        );
        boolean instanceFinished = isScheduleInstanceDone(schedule, day) || !schedule.isActive();

        Color borderColor = getScheduleColor(schedule.getRecurrence());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        panel.setMaximumSize(new Dimension(300, CELL_HEIGHT));
        panel.setPreferredSize(new Dimension(220, CELL_HEIGHT));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Pet pet = petMap.get(schedule.getPetId());
        final String petName = (pet != null) ? pet.getName() : "Deleted Pet";

        JLabel petLabel = new JLabel(petName);
        petLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        petLabel.setForeground(Color.BLACK);

        JLabel careTypeLabel = new JLabel("Jenis: " + schedule.getCareType());
        careTypeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        careTypeLabel.setForeground(Color.BLACK);

        JLabel categoryLabel = new JLabel("Kategori: " + schedule.getCategory());
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryLabel.setForeground(Color.BLACK);

        JLabel timeLabel = new JLabel("Waktu: " + String.format("%02d:%02d", scheduleTime.getHour(), scheduleTime.getMinute()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(Color.BLACK);

        JLabel notesLabel = new JLabel("Catatan: " + (schedule.getNotes() == null ? "-" : schedule.getNotes()));
        notesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        notesLabel.setForeground(new Color(80, 80, 80));

        JLabel statusLabel;
        if ("Once".equalsIgnoreCase(schedule.getRecurrence())) {
            statusLabel = new JLabel(instanceFinished ? "Selesai" : "Belum");
        } else {
            if (instanceFinished) {
                statusLabel = new JLabel("Selesai");
            } else {
                statusLabel = new JLabel(schedule.isActive() ? "Aktif" : "Nonaktif");
            }
        }
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(instanceFinished ? Color.GRAY : Color.BLUE);

        panel.add(petLabel);
        panel.add(careTypeLabel);
        panel.add(categoryLabel);
        panel.add(timeLabel);
        panel.add(notesLabel);
        panel.add(statusLabel);

        if (!instanceFinished) {
            JButton completeButton = new JButton("Selesaikan");
            completeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            completeButton.addActionListener(e -> {
                String notes = null;
                boolean requireNotes = com.narangga.swingapp.settings.UserSettings.getCurrentSettings().isRequireNotes();
                if (requireNotes) {
                    notes = JOptionPane.showInputDialog(
                        panel,
                        "Tambahkan catatan (opsional):",
                        "Catatan Perawatan",
                        JOptionPane.PLAIN_MESSAGE
                    );
                }
                markScheduleInstanceAsComplete(schedule, day, notes);
                
                // Update status label saja, tidak ubah border
                statusLabel.setText("Selesai");
                statusLabel.setForeground(Color.GRAY);
                completeButton.setVisible(false);
                panel.revalidate();
                panel.repaint();
            });
            panel.add(Box.createVerticalStrut(8));
            panel.add(completeButton);
        }

        return panel;
    }

    private Color getScheduleColor(String recurrence) {
        if (recurrence == null)
            return ONCE_COLOR;
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

    private void markScheduleInstanceAsComplete(Schedule schedule, LocalDate instanceDate, String notes) {
        try {
            String recurrence = schedule.getRecurrence();
            
            if ("Once".equalsIgnoreCase(recurrence)) {
                schedule.setActive(false);
                scheduleDAO.updateSchedule(schedule);
            } else {
                new ScheduleInstanceDAO().addInstance(
                    schedule.getId(),
                    java.sql.Date.valueOf(instanceDate),
                    true,
                    notes
                );
            }
            
            JOptionPane.showMessageDialog(this, "Jadwal berhasil ditandai sebagai selesai!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            mainMenu.refreshData();

            mainMenu.refreshPetManagerPanel();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan jadwal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}