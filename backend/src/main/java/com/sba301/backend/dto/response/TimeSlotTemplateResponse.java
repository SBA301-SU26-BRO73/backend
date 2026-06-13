package com.sba301.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;

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
public class TimeSlotTemplateResponse {

    private Long id;
    private Long courtId;
    private String courtName;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private Short dayOfWeek;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
