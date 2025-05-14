package com.mitocode.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_config")
public class UserConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String currencyKey;   // Clave de la moneda seleccionada
    private String currency;      // Moneda seleccionada
    private String storageType;   // "s3" o "local"
    private String language;      // Idioma seleccionado

    private boolean smsNotifications;
    private boolean emailNotifications;
    private boolean pushNotifications;

}
