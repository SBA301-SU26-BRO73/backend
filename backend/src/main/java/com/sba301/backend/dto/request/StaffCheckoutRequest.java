package com.sba301.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffCheckoutRequest {

    @NotNull(message = "staffUserId must not be null")
    private Long staffUserId;

    @NotNull(message = "bookingId must not be null")
    private Long bookingId;
}
