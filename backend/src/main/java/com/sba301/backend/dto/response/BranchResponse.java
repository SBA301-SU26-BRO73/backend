package com.sba301.backend.dto.response;

import java.time.LocalTime;
import java.time.OffsetDateTime;

import com.sba301.backend.entity.BranchStatus;

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
public class BranchResponse {

    private Long id;
    private Long adminId;
    private String adminName;
    private String name;
    private String address;
    private String ward;
    private String city;
    private String phone;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankName;
    private String bankQrImageUrl;
    private BranchStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
