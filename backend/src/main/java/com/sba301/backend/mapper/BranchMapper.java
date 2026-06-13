package com.sba301.backend.mapper;

import org.springframework.stereotype.Component;

import com.sba301.backend.dto.request.CreateBranchRequest;
import com.sba301.backend.dto.request.UpdateBranchRequest;
import com.sba301.backend.dto.response.BranchResponse;
import com.sba301.backend.entity.Branch;

@Component
public class BranchMapper {

    public Branch toEntity(CreateBranchRequest request) {
        Branch branch = new Branch();
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setWard(request.getWard());
        branch.setCity(request.getCity());
        branch.setPhone(request.getPhone());
        branch.setOpenTime(request.getOpenTime());
        branch.setCloseTime(request.getCloseTime());
        branch.setBankAccountNumber(request.getBankAccountNumber());
        branch.setBankAccountName(request.getBankAccountName());
        branch.setBankName(request.getBankName());
        branch.setBankQrImageUrl(request.getBankQrImageUrl());
        return branch;
    }

    public void updateEntity(Branch branch, UpdateBranchRequest request) {
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setWard(request.getWard());
        branch.setCity(request.getCity());
        branch.setPhone(request.getPhone());
        branch.setOpenTime(request.getOpenTime());
        branch.setCloseTime(request.getCloseTime());
        branch.setBankAccountNumber(request.getBankAccountNumber());
        branch.setBankAccountName(request.getBankAccountName());
        branch.setBankName(request.getBankName());
        branch.setBankQrImageUrl(request.getBankQrImageUrl());
        branch.setStatus(request.getStatus());
    }

    public BranchResponse toResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .adminId(branch.getAdmin().getId())
                .adminName(branch.getAdmin().getEmail())
                .name(branch.getName())
                .address(branch.getAddress())
                .ward(branch.getWard())
                .city(branch.getCity())
                .phone(branch.getPhone())
                .openTime(branch.getOpenTime())
                .closeTime(branch.getCloseTime())
                .bankAccountNumber(branch.getBankAccountNumber())
                .bankAccountName(branch.getBankAccountName())
                .bankName(branch.getBankName())
                .bankQrImageUrl(branch.getBankQrImageUrl())
                .status(branch.getStatus())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }
}