package com.sba301.backend.service.impl;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.DailySlotDto;
import com.sba301.backend.dto.response.DailySlotResponse;
import com.sba301.backend.repository.CourtRepository;
import com.sba301.backend.repository.TimeSlotTemplateRepository;
import com.sba301.backend.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final TimeSlotTemplateRepository timeSlotTemplateRepository;
    private final CourtRepository courtRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DailySlotResponse> getDailyCourtSchedule(Long courtId, LocalDate date) {
        // Ném AppException tập trung với Custom Message
        if (!courtRepository.existsById(courtId)) {
            throw new AppException(ErrorEnum.RESOURCE_NOT_FOUND, "Không tìm thấy sân với ID: " + courtId);
        }

        List<DailySlotDto> dbSlots = timeSlotTemplateRepository.getDailyCourtSchedule(courtId, date);

        // Chuyển đổi sang Response DTO (Null-safe)
        return dbSlots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DailySlotResponse mapToResponse(DailySlotDto dto) {
        if (dto == null) return null;

        return DailySlotResponse.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .price(dto.getPrice())
                .status(dto.getStatus() != null ? dto.getStatus() : "AVAILABLE") // An toàn
                .build();
    }
}