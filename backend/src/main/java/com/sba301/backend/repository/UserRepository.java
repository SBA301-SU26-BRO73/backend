package com.sba301.backend.repository;

import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.phone = :phone AND u.deletedAt IS NULL")
    boolean existsByPhone(@Param("phone") String phone);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findActiveByEmail(@Param("email") String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    Page<User> findAllActive(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    Page<User> findAllByRole(@Param("role") UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deletedAt IS NULL")
    Page<User> findAllByStatus(@Param("status") UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status AND u.deletedAt IS NULL")
    Page<User> findAllByRoleAndStatus(@Param("role") UserRole role, @Param("status") UserStatus status, Pageable pageable);
}
