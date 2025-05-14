package com.mitocode.dto.request.user;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserConfigRequest {

    @NotBlank(message = "La clave de la moneda no puede estar vacía")
    private String currencyKey;   // Clave de la moneda seleccionada

    @NotBlank(message = "La moneda no puede estar vacía")
    private String currency;      // Moneda seleccionada

    @NotBlank(message = "El tipo de almacenamiento no puede estar vacío")
    private String storageType;   // "s3" o "local"

    @NotBlank(message = "El idioma no puede estar vacío")
    private String language;      // Idioma seleccionado

    private boolean smsNotifications;
    private boolean emailNotifications;
    private boolean pushNotifications;
}
