package com.sba301.backend.controller;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.service.BranchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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

        // Cấu trúc response trả về giống với format của bạn
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("message", "Lấy danh sách cơ sở thành công");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}