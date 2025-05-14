package com.mitocode.model.file;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mitocode.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long idInvoice;

    @Column
    private String invoiceCode;

    @Column
    private String country; // Almacena el XML como texto

    @Column(columnDefinition = "TEXT")
    private String Xml; // Almacena el XML como texto    @Column

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonManagedReference // Evita la recursión infinita en la serialización
    private User user;
}
