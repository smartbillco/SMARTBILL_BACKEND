package com.mitocode.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DocType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDocType;

    @Column(nullable = false, length = 50)
    private String nameDocument;

    @Column(nullable = false, length = 150)
    private String descriptionDocument;

}
