package com.sba301.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sba301.backend.entity.Court;

public interface CourtRepository extends JpaRepository<Court, Long> {

    Optional<Court> findByIdAndDeletedAtIsNull(Long id);

    Page<Court> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Court> findAllByBranchAdminIdAndDeletedAtIsNull(Long adminId, Pageable pageable);

    boolean existsByBranchIdAndNameIgnoreCaseAndDeletedAtIsNull(Long branchId, String name);

    boolean existsByBranchIdAndNameIgnoreCaseAndDeletedAtIsNullAndIdNot(
            Long branchId, String name, Long id);

    List<Court> findAllByBranchIdAndDeletedAtIsNull(Long branchId);
}
