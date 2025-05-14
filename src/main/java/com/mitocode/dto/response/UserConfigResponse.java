package com.mitocode.dto.response;

import lombok.Data;

@Data
public class UserConfigResponse {
    private Long id;
    private String currencyKey;   // Clave de la moneda seleccionada
    private String currency;      // Moneda seleccionada
    private String storageType;   // "s3" o "local"
    private String language;      // Idioma seleccionado
    private boolean smsNotifications;
    private boolean emailNotifications;
    private boolean pushNotifications;
}
