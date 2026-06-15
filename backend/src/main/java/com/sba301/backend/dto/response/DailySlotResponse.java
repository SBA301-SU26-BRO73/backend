package com.sba301.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
public class DailySlotResponse {
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private String status;
}