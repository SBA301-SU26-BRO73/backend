package com.sba301.backend.controller;

import com.sba301.backend.dto.DailySlotDto;
import com.sba301.backend.service.CourtService;
import com.sba301.backend.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping("/{courtId}/daily-schedule")
    public ResponseEntity<Map<String, Object>> getDailyCourtSchedule(
            @PathVariable Long courtId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<DailySlotDto> schedule = courtService.getDailyCourtSchedule(courtId, date);

        return ResponseUtil.buildResponse("Lấy danh sách lịch sân thành công", schedule);
    }
}