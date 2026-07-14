package com.sba301.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.entity.Branch;

public interface BranchRepository extends JpaRepository<Branch, Long>, JpaSpecificationExecutor<Branch> {

    Optional<Branch> findByIdAndStatusAndDeletedAtIsNull(Long id, BranchStatus status);

    Page<Branch> findAllByStatusAndDeletedAtIsNull(BranchStatus status, Pageable pageable);

    Page<Branch> findAllByAdminIdAndStatusAndDeletedAtIsNull(Long adminId, BranchStatus status, Pageable pageable);

    boolean existsByNameAndDeletedAtIsNull(String name);
}
