package com.sba301.backend.service;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.repository.projection.DailySlotProjection;
import com.sba301.backend.dto.response.DailySlotResponse;
import com.sba301.backend.repository.CourtRepository;
import com.sba301.backend.repository.TimeSlotTemplateRepository;
import com.sba301.backend.service.impl.CourtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtServiceImplTest {

    @Mock
    private TimeSlotTemplateRepository timeSlotTemplateRepository;

    @Mock
    private CourtRepository courtRepository;

    @InjectMocks // Tự động bơm 2 Repository phía trên vào Service
    private CourtServiceImpl courtService;

    private DailySlotProjection mockDbSlot;

    @BeforeEach
    void setUp() {
        // Tạo dữ liệu giả lập cho Interface Projection
        mockDbSlot = Mockito.mock(DailySlotProjection.class);
        lenient().when(mockDbSlot.getStartTime()).thenReturn(LocalTime.of(8, 0));
        lenient().when(mockDbSlot.getEndTime()).thenReturn(LocalTime.of(8, 30));
        lenient().when(mockDbSlot.getPrice()).thenReturn(new BigDecimal("50000.00"));
        lenient().when(mockDbSlot.getStatus()).thenReturn("AVAILABLE");
    }

    @Test
    void getDailyCourtSchedule_Success_ShouldReturnMappedResponse() {
        Long courtId = 1L;
        LocalDate date = LocalDate.of(2026, 6, 8);

        // Setup mock logic cho DB
        when(courtRepository.existsById(courtId)).thenReturn(true);
        when(timeSlotTemplateRepository.getDailyCourtSchedule(courtId, date))
                .thenReturn(Collections.singletonList(mockDbSlot));

        // Thực thi
        List<DailySlotResponse> result = courtService.getDailyCourtSchedule(courtId, date);

        // Kiểm tra kết quả map
        assertNotNull(result);
        assertEquals(1, result.size());

        DailySlotResponse responseItem = result.get(0);
        assertEquals(LocalTime.of(8, 0), responseItem.getStartTime());
        assertEquals(LocalTime.of(8, 30), responseItem.getEndTime());
        assertEquals(new BigDecimal("50000.00"), responseItem.getPrice());
        assertEquals("AVAILABLE", responseItem.getStatus());

        verify(courtRepository, times(1)).existsById(courtId);
    }

    @Test
    void getDailyCourtSchedule_CourtNotFound_ShouldThrowAppException() {
        Long courtId = 99L; // Sân không tồn tại
        LocalDate date = LocalDate.of(2026, 6, 8);

        when(courtRepository.existsById(courtId)).thenReturn(false);

        // Bắt và kiểm tra Exception
        AppException exception = assertThrows(AppException.class, () -> {
            courtService.getDailyCourtSchedule(courtId, date);
        });

        // Đảm bảo ném đúng mã ErrorEnum
        assertEquals(ErrorEnum.RESOURCE_NOT_FOUND, exception.getErrorEnum());
        // Kiểm tra xem custom message có chứa ID truyền vào không
        assertTrue(exception.getCustomMessage().contains(String.valueOf(courtId)));

        // Xác minh query lấy data không bao giờ được chạy
        verify(timeSlotTemplateRepository, never()).getDailyCourtSchedule(anyLong(), any(LocalDate.class));
    }
}