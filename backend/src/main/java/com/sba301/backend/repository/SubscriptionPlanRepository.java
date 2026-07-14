package com.sba301.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sba301.backend.entity.SubscriptionPlan;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    @Query(
        value = "SELECT DISTINCT sp FROM SubscriptionPlan sp LEFT JOIN FETCH sp.features WHERE sp.deletedAt IS NULL",
        countQuery = "SELECT COUNT(sp) FROM SubscriptionPlan sp WHERE sp.deletedAt IS NULL"
    )
    Page<SubscriptionPlan> findAllActive(Pageable pageable);

    @Query("SELECT sp FROM SubscriptionPlan sp LEFT JOIN FETCH sp.features WHERE sp.id = :id AND sp.deletedAt IS NULL")
    Optional<SubscriptionPlan> findByIdActive(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN TRUE ELSE FALSE END FROM SubscriptionPlan sp WHERE sp.name = :name AND sp.deletedAt IS NULL")
    boolean existsByNameAndNotDeleted(@Param("name") String name);
}
