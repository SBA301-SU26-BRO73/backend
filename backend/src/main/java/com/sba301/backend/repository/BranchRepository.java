package com.sba301.backend.repository;
import com.sba301.backend.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    // Spring Data JPA tự động generate câu query SQL từ tên hàm
    // Lấy branch theo trạng thái và sắp xếp mới nhất lên đầu
    List<Branch> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT b FROM Branch b WHERE " +
            "(:name IS NULL OR :name = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:address IS NULL OR :address = '' OR LOWER(b.address) LIKE LOWER(CONCAT('%', :address, '%'))) AND " +
            "b.status = 'ACTIVE' " +
            "ORDER BY b.createdAt DESC")
    List<Branch> searchBranches(@Param("name") String name, @Param("address") String address);
}