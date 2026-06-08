package com.sba301.backend.mapper;

import com.sba301.backend.dto.response.BranchResponse;
import com.sba301.backend.entity.Branch;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public BranchResponse toResponse(Branch branch) {
        if (branch == null) return null;

        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .ward(branch.getWard())
                .city(branch.getCity())
                .phone(branch.getPhone())
                .status(branch.getStatus())
                .createdAt(branch.getCreatedAt() != null ? branch.getCreatedAt().toLocalDateTime() : null)
                .build();
    }
}