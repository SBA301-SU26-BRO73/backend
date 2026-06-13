package com.sba301.backend.dto.response;

import java.time.OffsetDateTime;

import com.sba301.backend.common.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private Long userId;
    private String email;
    private String phone;
    private UserStatus userStatus;
    private Long branchId;
    private String branchName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
