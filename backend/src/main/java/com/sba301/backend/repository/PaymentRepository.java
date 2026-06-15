package com.sba301.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sba301.backend.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
