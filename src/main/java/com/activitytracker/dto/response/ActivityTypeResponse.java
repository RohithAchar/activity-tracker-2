package com.activitytracker.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ActivityTypeResponse {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private LocalDateTime createdAt;
    private List<FieldDefResponse> fields;

    @Data
    @Builder
    public static class FieldDefResponse {
        private Long id;
        private String fieldName;
        private String fieldType;
        private boolean required;
        private int displayOrder;
    }
}
