package com.sba301.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import com.sba301.backend.common.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkInBookingResponse {

    private Long bookingId;
    private Long courtId;
    private String courtName;
    private String guestPhone;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int slotCount;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String checkinCode;
    private OffsetDateTime checkedInAt;
    private Long paymentId;
}
