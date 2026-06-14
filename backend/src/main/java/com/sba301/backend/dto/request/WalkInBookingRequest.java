package com.sba301.backend.dto.request;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkInBookingRequest {

    @NotNull(message = "staffUserId must not be null")
    private Long staffUserId;

    @NotNull(message = "courtId must not be null")
    private Long courtId;

    @NotBlank(message = "guestPhone must not be blank")
    @Pattern(regexp = "^[0-9]{9,11}$", message = "guestPhone must be 9-11 digits")
    private String guestPhone;

    @NotEmpty(message = "slotStarts must not be empty")
    private List<@NotNull LocalTime> slotStarts;
}
