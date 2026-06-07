package com.sba301.backend.controller;

import com.sba301.backend.dto.response.ApiResponse;
import com.sba301.backend.dto.response.DailySlotResponse;
import com.sba301.backend.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping("/{courtId}/daily-schedule")
    public ResponseEntity<ApiResponse<List<DailySlotResponse>>> getDailyCourtSchedule(
            @PathVariable Long courtId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<DailySlotResponse> data = courtService.getDailyCourtSchedule(courtId, date);

        return ResponseEntity.ok(ApiResponse.success(data, "Lấy danh sách lịch sân thành công"));
    }
}