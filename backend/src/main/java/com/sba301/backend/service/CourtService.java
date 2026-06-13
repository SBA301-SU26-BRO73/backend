package com.sba301.backend.service;

import com.sba301.backend.dto.response.DailySlotResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.dto.request.CreateCourtRequest;
import com.sba301.backend.dto.request.UpdateCourtRequest;
import com.sba301.backend.dto.response.CourtResponse;

public interface CourtService {
    List<DailySlotResponse> getDailyCourtSchedule(Long courtId, LocalDate date);

    CourtResponse create(CreateCourtRequest request);

    CourtResponse getById(Long id);

    Page<CourtResponse> getAll(Pageable pageable);

    CourtResponse update(Long id, UpdateCourtRequest request);

    void delete(Long id);
}