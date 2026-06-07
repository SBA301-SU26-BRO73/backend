package com.sba301.backend.service;

import com.sba301.backend.dto.response.DailySlotResponse;
import java.time.LocalDate;
import java.util.List;

public interface CourtService {
    List<DailySlotResponse> getDailyCourtSchedule(Long courtId, LocalDate date);
}