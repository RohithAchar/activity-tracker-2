package com.activitytracker.service;

import com.activitytracker.dto.request.CreateActivityTypeRequest;
import com.activitytracker.dto.response.ActivityTypeResponse;
import com.activitytracker.entity.ActivityFieldDef;
import com.activitytracker.entity.ActivityType;
import com.activitytracker.entity.User;
import com.activitytracker.exception.ResourceConflictException;
import com.activitytracker.exception.ResourceNotFoundException;
import com.activitytracker.repository.ActivityTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityTypeService {

    private final ActivityTypeRepository activityTypeRepository;
    private final UserService userService;

    @Transactional
    public ActivityTypeResponse create(CreateActivityTypeRequest request, Long userId) {
        User user = userService.getUserById(userId);

        if (activityTypeRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new ResourceConflictException("Activity type with name '" + request.getName() + "' already exists");
        }

        ActivityType activityType = ActivityType.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .build();

        if (request.getFields() != null) {
            for (CreateActivityTypeRequest.FieldDefRequest fieldReq : request.getFields()) {
                ActivityFieldDef field = ActivityFieldDef.builder()
                        .activityType(activityType)
                        .fieldName(fieldReq.getFieldName())
                        .fieldType(ActivityFieldDef.FieldType.valueOf(fieldReq.getFieldType().toUpperCase()))
                        .required(fieldReq.isRequired())
                        .displayOrder(fieldReq.getDisplayOrder())
                        .build();
                activityType.getFields().add(field);
            }
        }

        activityType = activityTypeRepository.save(activityType);
        return toResponse(activityType);
    }

    public Page<ActivityTypeResponse> listByUser(Long userId, Pageable pageable) {
        return activityTypeRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    public ActivityTypeResponse getByIdAndUser(Long id, Long userId) {
        ActivityType type = activityTypeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity type not found"));
        return toResponse(type);
    }

    public ActivityType getEntityByIdAndUser(Long id, Long userId) {
        return activityTypeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity type not found"));
    }

    private ActivityTypeResponse toResponse(ActivityType type) {
        return ActivityTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .description(type.getDescription())
                .icon(type.getIcon())
                .createdAt(type.getCreatedAt())
                .fields(type.getFields().stream()
                        .map(f -> ActivityTypeResponse.FieldDefResponse.builder()
                                .id(f.getId())
                                .fieldName(f.getFieldName())
                                .fieldType(f.getFieldType().name())
                                .required(f.isRequired())
                                .displayOrder(f.getDisplayOrder())
                                .build())
                        .toList())
                .build();
    }
}
