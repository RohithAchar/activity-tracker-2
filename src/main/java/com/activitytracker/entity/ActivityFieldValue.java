package com.activitytracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_field_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private ActivityEntry entry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_def_id")
    private ActivityFieldDef fieldDef;

    @Column(name = "value_text", length = 500)
    private String valueText;

    @Column(name = "value_numeric")
    private Double valueNumeric;

    @Column(name = "value_boolean")
    private Boolean valueBoolean;
}
