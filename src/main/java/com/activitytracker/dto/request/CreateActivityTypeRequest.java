package com.activitytracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class CreateActivityTypeRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 50)
    private String icon;

    private List<FieldDefRequest> fields;

    @Data
    public static class FieldDefRequest {
        @NotBlank
        @Size(max = 100)
        private String fieldName;

        @NotBlank
        private String fieldType;

        private boolean required;
        private int displayOrder;
    }
}
