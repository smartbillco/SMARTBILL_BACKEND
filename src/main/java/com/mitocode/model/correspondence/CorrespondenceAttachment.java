package com.mitocode.model.correspondence;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
@Builder
public class CorrespondenceAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 100)
    private String storageType;// s3 o Local

    @Column(length = 100)
    private String fileType;

    @Lob
    private byte[] data; // Guardar el archivo como BLOB

    @Column(length = 500)
    private String fileUrl; // URL o ruta donde está almacenado

    @ManyToOne
    @JoinColumn(name = "correspondence_id", referencedColumnName = "idCorrespondence", nullable = false)
    private Correspondence correspondence;
}