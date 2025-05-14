package com.mitocode.model.correspondence;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table
public class CorrespondenceDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 100)
    private String email;
}