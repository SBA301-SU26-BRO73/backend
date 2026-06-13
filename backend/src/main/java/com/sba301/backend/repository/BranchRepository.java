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

    boolean existsByNameAndDeletedAtIsNull(String name);

    @Query("SELECT b FROM Branch b WHERE " +
            "(:name IS NULL OR :name = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:address IS NULL OR :address = '' OR LOWER(b.address) LIKE LOWER(CONCAT('%', :address, '%'))) AND " +
            "(:ward IS NULL OR :ward = '' OR LOWER(b.ward) LIKE LOWER(CONCAT('%', :ward, '%'))) AND " +
            "(:city IS NULL OR :city = '' OR LOWER(b.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
            "(:phone IS NULL OR :phone = '' OR b.phone LIKE CONCAT('%', :phone, '%')) AND " +
            "(:status IS NULL OR b.status = :status)")
    Page<Branch> searchBranches(
            @Param("name") String name,
            @Param("address") String address,
            @Param("ward") String ward,
            @Param("city") String city,
            @Param("phone") String phone,
            @Param("status") BranchStatus status,
            Pageable pageable);
}
