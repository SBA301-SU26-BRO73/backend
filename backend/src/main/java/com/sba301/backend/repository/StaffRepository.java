package com.sba301.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sba301.backend.entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByIdAndDeletedAtIsNull(Long id);

    Page<Staff> findAllByBranchIdAndDeletedAtIsNull(Long branchId, Pageable pageable);

    boolean existsByUserIdAndDeletedAtIsNull(Long userId);

    Optional<Staff> findByUser_IdAndDeletedAtIsNull(Long userId);
}
