package com.activitytracker.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class EntryResponse {
    private Long id;
    private String activityType;
    private LocalDate entryDate;
    private String notes;
    private List<FieldValueResponse> fieldValues;

    @Data
    @Builder
    public static class FieldValueResponse {
        private String fieldName;
        private String value;
    }

    @Data
    @Builder
    public static class EntryStats {
        private long totalEntries;
        private Map<String, Double> averages;
        private Map<String, Double> totals;
        private int currentStreak;
        private int longestStreak;
    }
}
