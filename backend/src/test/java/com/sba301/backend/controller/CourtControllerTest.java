package com.sba301.backend.controller;

import com.sba301.backend.dto.DailySlotDto;
import com.sba301.backend.service.CourtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CourtController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
public class CourtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourtService courtService;

    private List<DailySlotDto> mockList;

    // 1. Tạo một class nội bộ implement DailySlotDto để Jackson có thể parse ra JSON bình thường
    static class TestDailySlotDto implements DailySlotDto {
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final BigDecimal price;
        private final String status;

        public TestDailySlotDto(LocalTime startTime, LocalTime endTime, BigDecimal price, String status) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.price = price;
            this.status = status;
        }

        @Override
        public LocalTime getStartTime() { return startTime; }

        @Override
        public LocalTime getEndTime() { return endTime; }

        @Override
        public BigDecimal getPrice() { return price; }

        @Override
        public String getStatus() { return status; }
    }

    @BeforeEach
    void setUp() {
        // 2. Sử dụng class vừa tạo thay vì dùng Mockito.mock()
        DailySlotDto slot1 = new TestDailySlotDto(
                LocalTime.of(8, 0), LocalTime.of(8, 30), new BigDecimal("50000.00"), "AVAILABLE"
        );

        DailySlotDto slot2 = new TestDailySlotDto(
                LocalTime.of(8, 30), LocalTime.of(9, 0), new BigDecimal("50000.00"), "BOOKED"
        );

        mockList = Arrays.asList(slot1, slot2);
    }

    @Test
    void testGetDailyCourtSchedule_WithData_ShouldReturnSuccessMessage() throws Exception {
        Long courtId = 1L;
        LocalDate testDate = LocalDate.of(2026, 6, 8);

        Mockito.when(courtService.getDailyCourtSchedule(eq(courtId), eq(testDate))).thenReturn(mockList);

        mockMvc.perform(get("/api/courts/{courtId}/daily-schedule", courtId)
                        .param("date", testDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách lịch sân thành công"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data[1].status").value("BOOKED"));
    }

    @Test
    void testGetDailyCourtSchedule_NoData_ShouldReturnEmptyList() throws Exception {
        Long courtId = 99L;
        LocalDate testDate = LocalDate.of(2026, 6, 8);

        Mockito.when(courtService.getDailyCourtSchedule(eq(courtId), eq(testDate))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/courts/{courtId}/daily-schedule", courtId)
                        .param("date", testDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách lịch sân thành công"))
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}