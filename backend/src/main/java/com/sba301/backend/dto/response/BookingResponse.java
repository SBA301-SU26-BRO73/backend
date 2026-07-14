package com.sba301.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.sba301.backend.common.enums.BookingStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookingResponse {

    private Long id;

    private Long courtId;
    private String courtName;

    private Long customerId;
    private String customerEmail;

    private String guestPhone;

    private LocalDate date;

    private BookingStatus status;

    private BigDecimal totalPrice;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}