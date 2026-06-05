package com.sba301.backend.controller;

import com.sba301.backend.entity.Branch;
import com.sba301.backend.service.BranchService;
import com.sba301.backend.util.ResponseUtil; // 1. Import ResponseUtil
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<Map<String, Object>> searchBranches(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address) {

        List<Branch> data = branchService.searchBranches(name, address);

        return ResponseUtil.buildResponse("Lọc danh sách cơ sở thành công", data);
    }
}