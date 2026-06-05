package com.sba301.backend.service;

import com.sba301.backend.entity.Branch;
import com.sba301.backend.repository.BranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    // Constructor Injection
    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<Branch> getAllActiveBranches() {
        // Trả về danh sách các cơ sở đang có trạng thái ACTIVE
        return branchRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
    }

    public List<Branch> searchBranches(String name, String address) {
        // Nếu frontend truyền chuỗi rỗng hoặc có khoảng trắng, trim() lại cho sạch
        String searchName = (name != null) ? name.trim() : null;
        String searchAddress = (address != null) ? address.trim() : null;

        return branchRepository.searchBranches(searchName, searchAddress);
    }
}