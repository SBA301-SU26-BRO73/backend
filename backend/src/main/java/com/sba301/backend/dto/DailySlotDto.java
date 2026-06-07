package com.sba301.backend.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface DailySlotDto {
    LocalTime getStartTime();
    LocalTime getEndTime();
    BigDecimal getPrice();
    String getStatus(); // Nhận 1 trong 4 giá trị: AVAILABLE, BOOKED, HOLDING, EXPIRED
}