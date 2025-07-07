package com.narangga.swingapp.util;

import com.narangga.swingapp.Schedule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ScheduleUtils {

    public static LocalDateTime getNextOccurrence(Schedule schedule) {
        if (schedule == null || schedule.getScheduleTime() == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduleTime = LocalDateTime.ofInstant(
            schedule.getScheduleTime().toInstant(), 
            ZoneId.systemDefault()
        );
        
        // If it's a one-time schedule and in the past, no next occurrence
        if ("Once".equals(schedule.getRecurrence()) && scheduleTime.isBefore(now)) {
            return null;
        }

        if (scheduleTime.isAfter(now)) {
            return scheduleTime; // If the schedule is in the future, that's the next occurrence.
        }

        switch (schedule.getRecurrence()) {
            case "Daily":
                LocalDateTime nextDay = scheduleTime;
                while (!nextDay.isAfter(now)) {
                    nextDay = nextDay.plusDays(1);
                }
                return nextDay;
            case "Weekly":
                LocalDateTime nextWeek = scheduleTime;
                while (!nextWeek.isAfter(now)) {
                    nextWeek = nextWeek.plusWeeks(1);
                }
                return nextWeek;
            case "Monthly":
                LocalDateTime nextMonth = scheduleTime;
                while (!nextMonth.isAfter(now)) {
                    nextMonth = nextMonth.plusMonths(1);
                }
                return nextMonth;
            case "Once":
            default:
                return null; // No next occurrence for non-recurring or past events
        }
    }

    /**
     * Generate a human-readable countdown string for the next occurrence
     * @param nextOccurrence The next occurrence date/time, or null if none
     * @return A formatted countdown string
     */
    public static String getCountdownString(LocalDateTime nextOccurrence) {
        if (nextOccurrence == null) {
            return "No upcoming";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, nextOccurrence);

        long days = duration.toDays();
        if (days > 0) {
            return String.format("in %d day(s)", days);
        }

        long hours = duration.toHours();
        if (hours > 0) {
            return String.format("in %d hour(s)", hours);
        }

        long minutes = duration.toMinutes();
        if (minutes > 0) {
            return String.format("in %d minute(s)", minutes);
        }

        return "Now";
    }
}
