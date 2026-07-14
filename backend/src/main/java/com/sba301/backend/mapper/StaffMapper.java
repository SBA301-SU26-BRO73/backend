package com.sba301.backend.mapper;

import org.springframework.stereotype.Component;

import com.sba301.backend.dto.response.StaffResponse;
import com.sba301.backend.entity.Staff;

@Component
public class StaffMapper {

    public StaffResponse toResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .userId(staff.getUser().getId())
                .email(staff.getUser().getEmail())
                .fullName(staff.getUser().getFullName())
                .phone(staff.getUser().getPhone())
                .userStatus(staff.getUser().getStatus())
                .branchId(staff.getBranch().getId())
                .branchName(staff.getBranch().getName())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}
