package com.sba301.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sba301.backend.entity.CourtType;

public interface CourtTypeRepository extends JpaRepository<CourtType, Long> {
}
