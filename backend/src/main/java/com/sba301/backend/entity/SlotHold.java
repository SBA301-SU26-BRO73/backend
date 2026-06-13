package com.sba301.backend.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "slot_holds",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_slot_holds_court_date_start",
                columnNames = {"court_id", "date", "slot_start"}))
@Getter
@Setter
@NoArgsConstructor
public class SlotHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Column(name = "guest_phone", length = 20)
    private String guestPhone;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "slot_start", nullable = false)
    private LocalTime slotStart;

    @Column(name = "slot_end", nullable = false)
    private LocalTime slotEnd;

    @Column(name = "expired_at", nullable = false)
    private OffsetDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
