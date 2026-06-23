package com.activitytracker.controller;

import com.activitytracker.dto.request.CreateActivityTypeRequest;
import com.activitytracker.dto.response.ActivityTypeResponse;
import com.activitytracker.service.ActivityTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activity-types")
@RequiredArgsConstructor
public class ActivityTypeController {

    private final ActivityTypeService activityTypeService;

    @PostMapping
    public ResponseEntity<ActivityTypeResponse> create(
            @Valid @RequestBody CreateActivityTypeRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityTypeService.create(request, userId));
    }

    @GetMapping
    public ResponseEntity<Page<ActivityTypeResponse>> list(
            @AuthenticationPrincipal Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(activityTypeService.listByUser(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityTypeResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(activityTypeService.getByIdAndUser(id, userId));
    }
}
