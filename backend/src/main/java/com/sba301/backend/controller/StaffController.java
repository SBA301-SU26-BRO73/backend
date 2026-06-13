package com.sba301.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sba301.backend.dto.request.CreateStaffRequest;
import com.sba301.backend.dto.request.UpdateStaffRequest;
import com.sba301.backend.dto.response.ApiResponse;
import com.sba301.backend.dto.response.StaffResponse;
import com.sba301.backend.service.StaffService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<StaffResponse>> create(@Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(staffService.create(request), "Staff created successfully", 201));
    }

    @GetMapping("/branches/{branchId}/staff")
    public ResponseEntity<ApiResponse<Page<StaffResponse>>> getByBranch(@PathVariable Long branchId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(staffService.getByBranch(branchId, pageable)));
    }

    @GetMapping("/staff/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(staffService.getById(id)));
    }

    @PatchMapping("/staff/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateStaffRequest request) {
        return ResponseEntity.ok(ApiResponse.success(staffService.update(id, request), "Staff updated successfully"));
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        staffService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
