package com.sba301.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sba301.backend.entity.CourtType;

public interface CourtTypeRepository extends JpaRepository<CourtType, Long> {

    @Query("SELECT ct FROM CourtType ct WHERE ct.deletedAt IS NULL")
    Page<CourtType> findAllActive(Pageable pageable);

    @Query("SELECT ct FROM CourtType ct WHERE ct.active = :active AND ct.deletedAt IS NULL")
    Page<CourtType> findAllByActive(@Param("active") boolean active, Pageable pageable);

    @Query("SELECT ct FROM CourtType ct WHERE ct.id = :id AND ct.deletedAt IS NULL")
    Optional<CourtType> findByIdActive(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(ct) > 0 THEN TRUE ELSE FALSE END FROM CourtType ct WHERE ct.name = :name AND ct.deletedAt IS NULL")
    boolean existsByNameAndNotDeleted(@Param("name") String name);
}
