package com.sba301.backend.controller;

import com.sba301.backend.dto.response.DailySlotResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.sba301.backend.dto.request.CreateCourtRequest;
import com.sba301.backend.dto.request.UpdateCourtRequest;
import com.sba301.backend.dto.response.ApiResponse;
import com.sba301.backend.service.CourtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateCourtRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(courtService.create(request), "Court created successfully", 201));
    }

    @GetMapping
    public ResponseEntity<?> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(courtService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courtService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourtRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(courtService.update(id, request), "Court updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        courtService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{courtId}/daily-schedule")
    public ResponseEntity<ApiResponse<List<DailySlotResponse>>> getDailyCourtSchedule(
            @PathVariable Long courtId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DailySlotResponse> data = courtService.getDailyCourtSchedule(courtId, date);
        return ResponseEntity.ok(ApiResponse.success(data, "Get a list of successful itineraries"));
    }
}

