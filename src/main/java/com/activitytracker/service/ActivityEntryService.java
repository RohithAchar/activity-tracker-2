package com.activitytracker.service;

import com.activitytracker.dto.request.CreateEntryRequest;
import com.activitytracker.dto.response.EntryResponse;
import com.activitytracker.entity.*;
import com.activitytracker.exception.ResourceNotFoundException;
import com.activitytracker.repository.ActivityEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityEntryService {

    private final ActivityEntryRepository entryRepository;
    private final ActivityTypeService activityTypeService;
    private final UserService userService;

    @Transactional
    public EntryResponse create(CreateEntryRequest request, Long userId) {
        User user = userService.getUserById(userId);
        ActivityType activityType = activityTypeService.getEntityByIdAndUser(request.getActivityTypeId(), userId);

        Map<Long, ActivityFieldDef> fieldDefMap = activityType.getFields().stream()
                .collect(Collectors.toMap(ActivityFieldDef::getId, f -> f));

        ActivityEntry entry = ActivityEntry.builder()
                .user(user)
                .activityType(activityType)
                .entryDate(request.getEntryDate())
                .notes(request.getNotes())
                .build();

        if (request.getFieldValues() != null) {
            for (CreateEntryRequest.FieldValueRequest fvReq : request.getFieldValues()) {
                ActivityFieldDef fieldDef = fieldDefMap.get(fvReq.getFieldDefId());
                if (fieldDef == null) {
                    throw new ResourceNotFoundException("Field definition not found: " + fvReq.getFieldDefId());
                }

                ActivityFieldValue fieldValue = ActivityFieldValue.builder()
                        .entry(entry)
                        .fieldDef(fieldDef)
                        .build();

                switch (fieldDef.getFieldType()) {
                    case NUMERIC -> fieldValue.setValueNumeric(
                            fvReq.getValue() != null ? Double.parseDouble(fvReq.getValue()) : null);
                    case BOOLEAN -> fieldValue.setValueBoolean(
                            fvReq.getValue() != null ? Boolean.parseBoolean(fvReq.getValue()) : null);
                    case TEXT -> fieldValue.setValueText(fvReq.getValue());
                }

                entry.getFieldValues().add(fieldValue);
            }
        }

        entry = entryRepository.save(entry);
        return toResponse(entry);
    }

    public Page<EntryResponse> listByActivityType(Long userId, Long activityTypeId, Pageable pageable) {
        return entryRepository.findByUserIdAndActivityTypeId(userId, activityTypeId, pageable)
                .map(this::toResponse);
    }

    public EntryResponse.EntryStats getStats(Long userId, Long activityTypeId, LocalDate from, LocalDate to) {
        List<ActivityEntry> entries = entryRepository
                .findByUserIdAndActivityTypeIdAndEntryDateBetweenOrderByEntryDateAsc(
                        userId, activityTypeId, from, to);

        long totalEntries = entries.size();

        Map<String, List<Double>> numericValues = new HashMap<>();
        for (ActivityEntry entry : entries) {
            for (ActivityFieldValue fv : entry.getFieldValues()) {
                if (fv.getValueNumeric() != null) {
                    String name = fv.getFieldDef().getFieldName();
                    numericValues.computeIfAbsent(name, k -> new ArrayList<>()).add(fv.getValueNumeric());
                }
            }
        }

        Map<String, Double> averages = new HashMap<>();
        Map<String, Double> totals = new HashMap<>();
        for (Map.Entry<String, List<Double>> e : numericValues.entrySet()) {
            double sum = e.getValue().stream().mapToDouble(Double::doubleValue).sum();
            totals.put(e.getKey(), sum);
            averages.put(e.getKey(), sum / e.getValue().size());
        }

        int[] streakInfo = calculateStreak(userId, activityTypeId, entries);
        int currentStreak = streakInfo[0];
        int longestStreak = streakInfo[1] > 0 ? streakInfo[1] : currentStreak;

        return EntryResponse.EntryStats.builder()
                .totalEntries(totalEntries)
                .averages(averages)
                .totals(totals)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .build();
    }

    private int[] calculateStreak(Long userId, Long activityTypeId, List<ActivityEntry> entries) {
        if (entries.isEmpty()) {
            return new int[]{0, 0};
        }

        Set<LocalDate> activeDates = entries.stream()
                .map(ActivityEntry::getEntryDate)
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        int currentStreak = 0;
        LocalDate check = today;

        while (activeDates.contains(check)) {
            currentStreak++;
            check = check.minusDays(1);
        }

        int longestStreak = 0;
        int tempStreak = 0;
        LocalDate prev = null;

        List<LocalDate> sortedDates = activeDates.stream().sorted().toList();
        for (LocalDate date : sortedDates) {
            if (prev != null && date.equals(prev.plusDays(1))) {
                tempStreak++;
            } else {
                tempStreak = 1;
            }
            longestStreak = Math.max(longestStreak, tempStreak);
            prev = date;
        }

        return new int[]{currentStreak, longestStreak};
    }

    private EntryResponse toResponse(ActivityEntry entry) {
        return EntryResponse.builder()
                .id(entry.getId())
                .activityType(entry.getActivityType().getName())
                .entryDate(entry.getEntryDate())
                .notes(entry.getNotes())
                .fieldValues(entry.getFieldValues().stream()
                        .map(fv -> EntryResponse.FieldValueResponse.builder()
                                .fieldName(fv.getFieldDef().getFieldName())
                                .value(fv.getValueText() != null ? fv.getValueText() :
                                       fv.getValueNumeric() != null ? String.valueOf(fv.getValueNumeric()) :
                                       fv.getValueBoolean() != null ? String.valueOf(fv.getValueBoolean()) : "")
                                .build())
                        .toList())
                .build();
    }
}
