package com.sba301.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalTime;

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
public class UpdateTimeSlotTemplateRequest {

    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private Short dayOfWeek;
    private Boolean active;
}
