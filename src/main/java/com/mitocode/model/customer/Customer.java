package com.mitocode.model.customer;

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
public class Customer {

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
    private String secondName;

    @Column(nullable = false, length = 70)
    private String lastName;

    @Column(nullable = false, length = 70)
    private String secondLastName;



    @Column(nullable = false, length = 20, unique = true)
    private String documentNumber;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(length = 150)
    private String address;

    @Column(nullable = false, length = 80)
    private String email;

    @Column(nullable = false)
    private LocalDateTime dateOfBirth;


    @Column(nullable = false, length = 10000000)
    private String photo_url;
}