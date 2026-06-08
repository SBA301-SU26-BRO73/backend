package com.sba301.backend.repository.projection;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface DailySlotProjection {
    LocalTime getStartTime();
    LocalTime getEndTime();
    BigDecimal getPrice();
    String getStatus(); // Nhận 1 trong 4 giá trị: AVAILABLE, BOOKED, HOLDING, EXPIRED
}