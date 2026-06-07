package com.sba301.backend.service;

import com.sba301.backend.dto.DailySlotDto;
import com.sba301.backend.exception.ResourceNotFoundException; // Gọi exception bạn đã định nghĩa
import com.sba301.backend.repository.CourtRepository;
import com.sba301.backend.repository.TimeSlotTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final TimeSlotTemplateRepository timeSlotTemplateRepository;
    private final CourtRepository courtRepository;

    @Transactional(readOnly = true)
    public List<DailySlotDto> getDailyCourtSchedule(Long courtId, LocalDate date) {
        if (!courtRepository.existsById(courtId)) {
            throw new ResourceNotFoundException("Không tìm thấy sân với ID: " + courtId);
        }

        return timeSlotTemplateRepository.getDailyCourtSchedule(courtId, date);
    }
}