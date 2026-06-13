package com.sba301.backend.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sba301.backend.dto.request.ApplyTimeSlotTemplateRequest;
import com.sba301.backend.dto.request.CreateTimeSlotTemplateRequest;
import com.sba301.backend.dto.request.UpdateTimeSlotTemplateRequest;
import com.sba301.backend.dto.response.ApiResponse;
import com.sba301.backend.service.TimeSlotTemplateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/time-slot-templates")
@RequiredArgsConstructor
public class TimeSlotTemplateController {

    private final TimeSlotTemplateService timeSlotTemplateService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateTimeSlotTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        timeSlotTemplateService.create(request),
                        "Time slot template created successfully",
                        HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<?> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(timeSlotTemplateService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(timeSlotTemplateService.getById(id)));
    }

    @GetMapping("/court/{courtId}")
    public ResponseEntity<?> getByCourt(@PathVariable Long courtId) {
        return ResponseEntity.ok(ApiResponse.success(timeSlotTemplateService.getByCourt(courtId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTimeSlotTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                timeSlotTemplateService.update(id, request),
                "Time slot template updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        timeSlotTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/apply/{courtId}")
    public ResponseEntity<?> applyToCourt(
            @PathVariable Long courtId,
            @Valid @RequestBody ApplyTimeSlotTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                timeSlotTemplateService.applyToCourt(courtId, request),
                "Time slot templates applied successfully"));
    }
}
