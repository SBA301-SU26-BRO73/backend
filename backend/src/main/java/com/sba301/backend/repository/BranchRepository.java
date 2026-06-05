package com.sba301.backend.repository;
import com.sba301.backend.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    // Spring Data JPA tự động generate câu query SQL từ tên hàm
    // Lấy branch theo trạng thái và sắp xếp mới nhất lên đầu
    List<Branch> findByStatusOrderByCreatedAtDesc(String status);
}