package com.sba301.backend.dto.response;

import java.math.BigDecimal;
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
public class StaffCheckoutResponse {

    private Long bookingId;
    private String courtName;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private OffsetDateTime completedAt;
}
