package com.mitocode.model.user;

import com.mitocode.model.file.FileEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUser;

    @Column(length = 150)
    private String address;

    @Column(nullable = false, length = 20, unique = true)
    private String documentNumber;

    @Column(name = "email", nullable = false, length = 80, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, length = 70)
    private String firstName;

    @Column(nullable = false, length = 70)
    private String lastName;

    @Column(nullable = false, length = 60) //123 | Bcrypt
    private String password;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String photo_url;

    @Column(nullable = true, length = 70)
    private String secondLastName;

    @Column(nullable = true, length = 70)
    private String secondName;

    @Column(nullable = false, length = 60, unique = true)
    private String username;

    @ManyToOne
    @JoinColumn(name = "id_doc_type", nullable = false, foreignKey = @ForeignKey(name = "FK_CUSTOMER_DOC_TYPE"))
    private DocType documentType;

    @ManyToOne
    @JoinColumn(name = "id_regime", nullable = false, foreignKey = @ForeignKey(name = "FK_CUSTOMER_REGIME"))
    private Regime regime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "id_user", referencedColumnName = "idUser"),
            inverseJoinColumns = @JoinColumn(name = "id_role", referencedColumnName = "idRole")
    )
    private List<Role> roles;

    @ManyToMany(mappedBy = "users")
    private List<FileEntity> files;
}
