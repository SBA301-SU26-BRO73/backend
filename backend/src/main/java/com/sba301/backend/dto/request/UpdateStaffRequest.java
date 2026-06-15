package com.sba301.backend.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffRequest {

    @Pattern(regexp = "^[0-9]{9,11}$", message = "Phone must be 9-11 digits")
    private String phone;

    private Long branchId;
}
