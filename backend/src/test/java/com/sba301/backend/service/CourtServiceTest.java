package com.sba301.backend.service;

import com.sba301.backend.dto.DailySlotDto;
import com.sba301.backend.exception.ResourceNotFoundException;
import com.sba301.backend.repository.CourtRepository;
import com.sba301.backend.repository.TimeSlotTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private TimeSlotTemplateRepository timeSlotTemplateRepository;

    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private CourtService courtService;

    @Test
    void getDailyCourtSchedule_Success_ReturnsSlotList() {
        // Arrange
        Long courtId = 1L;
        LocalDate date = LocalDate.of(2026, 6, 8);

        DailySlotDto mockSlot = mock(DailySlotDto.class);
        when(mockSlot.getStartTime()).thenReturn(LocalTime.of(8, 0));
        when(mockSlot.getEndTime()).thenReturn(LocalTime.of(8, 30));
        when(mockSlot.getPrice()).thenReturn(new BigDecimal("50000.00"));
        when(mockSlot.getStatus()).thenReturn("AVAILABLE");

        when(courtRepository.existsById(courtId)).thenReturn(true);
        when(timeSlotTemplateRepository.getDailyCourtSchedule(courtId, date))
                .thenReturn(List.of(mockSlot));

        // Act
        List<DailySlotDto> result = courtService.getDailyCourtSchedule(courtId, date);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        // KIỂM TRA ĐẦY ĐỦ CÁC TRƯỜNG ĐỂ MOCKITO KHÔNG BÁO LỖI THỪA
        DailySlotDto resultSlot = result.get(0);
        assertEquals(LocalTime.of(8, 0), resultSlot.getStartTime());
        assertEquals(LocalTime.of(8, 30), resultSlot.getEndTime());
        assertEquals(new BigDecimal("50000.00"), resultSlot.getPrice());
        assertEquals("AVAILABLE", resultSlot.getStatus());

        verify(courtRepository, times(1)).existsById(courtId);
        verify(timeSlotTemplateRepository, times(1)).getDailyCourtSchedule(courtId, date);
    }
    @Test
    void getDailyCourtSchedule_CourtNotFound_ThrowsException() {
        // Arrange
        Long courtId = 999L; // Sân không tồn tại
        LocalDate date = LocalDate.of(2026, 6, 8);

        when(courtRepository.existsById(courtId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            courtService.getDailyCourtSchedule(courtId, date);
        });

        assertTrue(exception.getMessage().contains(String.valueOf(courtId)));

        // Xác minh rằng hàm getDailyCourtSchedule KHÔNG BAO GIỜ được gọi nếu sân không tồn tại
        verify(timeSlotTemplateRepository, never()).getDailyCourtSchedule(anyLong(), any(LocalDate.class));
    }
}