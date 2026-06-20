package com.activitytracker.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateEntryRequest {

    @NotNull
    private Long activityTypeId;

    @NotNull
    private LocalDate entryDate;

    private String notes;

    private List<FieldValueRequest> fieldValues;

    @Data
    public static class FieldValueRequest {
        @NotNull
        private Long fieldDefId;
        private String value;
    }
}
