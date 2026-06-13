package com.sba301.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.sba301.backend.common.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffScheduleResponse {

    private Long bookingId;
    private Long courtId;
    private String courtName;
    private String customerName;
    private String guestPhone;
    private BookingStatus status;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal totalPrice;
    private int slotCount;
}
