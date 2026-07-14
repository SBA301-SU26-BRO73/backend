package com.sba301.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffCheckinRequest {

    @NotNull(message = "staffUserId must not be null")
    private Long staffUserId;

    @NotBlank(message = "checkinCode must not be blank")
    private String checkinCode;
}
