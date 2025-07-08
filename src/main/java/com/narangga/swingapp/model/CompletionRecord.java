package com.narangga.swingapp.model;

import java.time.LocalDate;

public class CompletionRecord {
    private int scheduleId;
    private LocalDate completionDate;
    
    public CompletionRecord(int scheduleId, LocalDate completionDate) {
        this.scheduleId = scheduleId;
        this.completionDate = completionDate;
    }
}
