package com.sba301.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
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
public class CreateTimeSlotTemplateRequest {

    @NotNull(message = "Court id is required")
    private Long courtId;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotNull(message = "Day of week is required")
    private Short dayOfWeek;

    private Boolean active;
}
