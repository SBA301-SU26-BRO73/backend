package com.sba301.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "court_types")
public class CourtType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // Bổ sung các thuộc tính khác sau
}