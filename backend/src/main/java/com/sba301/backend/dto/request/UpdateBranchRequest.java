package com.sba301.backend.dto.request;

import java.time.LocalTime;

import com.sba301.backend.entity.BranchStatus;

import jakarta.validation.constraints.NotBlank;
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
public class UpdateBranchRequest {

    @NotNull(message = "Admin id is required")
    private Long adminId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String ward;

    @NotBlank(message = "City is required")
    private String city;

    private String phone;

    @NotNull(message = "Open time is required")
    private LocalTime openTime;

    @NotNull(message = "Close time is required")
    private LocalTime closeTime;

    private String bankAccountNumber;
    private String bankAccountName;
    private String bankName;
    private String bankQrImageUrl;

    @NotNull(message = "Status is required")
    private BranchStatus status;
}
