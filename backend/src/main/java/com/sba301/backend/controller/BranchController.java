package com.sba301.backend.controller;

import com.sba301.backend.dto.request.BranchFilterRequest;
import com.sba301.backend.dto.response.ApiResponse;
import com.sba301.backend.dto.response.BranchResponse;
import com.sba301.backend.service.BranchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("/{id}")
    public ApiResponse<BranchResponse> getBranchById(@PathVariable Long id) {
        BranchResponse data = branchService.getBranchById(id);
        return ApiResponse.success(data, "Lấy thông tin chi tiết cơ sở thành công");
    }

    @GetMapping
    public ApiResponse<Page<BranchResponse>> getAllBranchesPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<BranchResponse> pageData = branchService.getAllActiveBranchesPaginated(pageable);

        return ApiResponse.success(pageData, "Lấy danh sách cơ sở phân trang thành công");
    }

    @GetMapping("/search")
    public ApiResponse<Page<BranchResponse>> searchBranchesPaginated(
            BranchFilterRequest filterDto,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<BranchResponse> pageData = branchService.searchBranchesWithPagination(filterDto, pageable);

//        if (pageData.isEmpty()) {
//            return ApiResponse.success(pageData, "Không tìm thấy cơ sở nào phù hợp");
//        }

        return ApiResponse.success(pageData, "Tìm kiếm và lọc cơ sở thành công");
    }
}