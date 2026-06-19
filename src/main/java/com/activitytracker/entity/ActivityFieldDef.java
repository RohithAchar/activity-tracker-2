package com.activitytracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_field_defs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityFieldDef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_type_id", nullable = false)
    private ActivityType activityType;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 20)
    private FieldType fieldType;

    @Column(nullable = false)
    @Builder.Default
    private boolean required = false;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    public enum FieldType {
        NUMERIC, BOOLEAN, TEXT
    }
}
