package com.sba301.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sba301.backend.common.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBookingResponse {

    private Long id;
    private Long courtId;
    private String courtName;
    private LocalDate date;
    private BookingStatus status;
    private BigDecimal totalPrice;
}
