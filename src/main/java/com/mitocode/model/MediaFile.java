package com.mitocode.model;

import com.mitocode.model.user.DocType;
import com.mitocode.model.user.Regime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer idCustomer;

    // inicio de llaves database
    @ManyToOne
    @JoinColumn(name = "id_regime", nullable = false, foreignKey = @ForeignKey(name = "FK_CUSTOMER_REGIME"))
    private Regime regime;

    @ManyToOne
    @JoinColumn(name = "id_doc_type", nullable = false, foreignKey = @ForeignKey(name = "FK_CUSTOMER_DOC_TYPE"))
    private DocType docType;
    // Fin de llaves database

    @Column(nullable = false, length = 70)
    private String firstName;

    @Column(nullable = false, length = 70)
    private String lastName;

    @Column(nullable = false, length = 20)
    private String documentNumber;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(length = 150)
    private String address;

    @Column(nullable = false, length = 80)
    private String email;

    @Column(nullable = false)
    private LocalDateTime dateOfBirth;

    /*
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)*/
    private Integer idFile;

    @Column(length = 50, nullable = false)
    private String fileName;

    @Column(length = 20, nullable = false)
    private String fileType;

    @Column(nullable = false)
    private byte[] content;
}
