package com.activitytracker.controller;

import com.activitytracker.dto.request.CreateEntryRequest;
import com.activitytracker.dto.response.EntryResponse;
import com.activitytracker.service.ActivityEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/entries")
@RequiredArgsConstructor
public class ActivityEntryController {

    private final ActivityEntryService entryService;

    @PostMapping
    public ResponseEntity<EntryResponse> create(
            @Valid @RequestBody CreateEntryRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entryService.create(request, userId));
    }

    @GetMapping
    public ResponseEntity<Page<EntryResponse>> list(
            @RequestParam Long activityTypeId,
            @AuthenticationPrincipal Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(entryService.listByActivityType(userId, activityTypeId, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<EntryResponse.EntryStats> stats(
            @RequestParam Long activityTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(entryService.getStats(userId, activityTypeId, from, to));
    }
}
