package com.sba301.backend.controller;

import com.sba301.backend.dto.request.BranchFilterDTO;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.service.BranchService;
import com.sba301.backend.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBranches() {
        List<Branch> data = branchService.getAllActiveBranches();
        return ResponseUtil.buildResponse("Lấy danh sách cơ sở thành công", data);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchBranches(BranchFilterDTO filterDto) {
        List<Branch> data = branchService.searchAndFilterBranches(filterDto);
        if (data.isEmpty()) {
            return ResponseUtil.buildResponse("Không tìm thấy cơ sở nào phù hợp", data);
        }
        return ResponseUtil.buildResponse("Tìm kiếm và lọc cơ sở thành công", data);
    }
}