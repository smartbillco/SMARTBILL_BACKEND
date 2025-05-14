package com.mitocode.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AppModuleRequest {

    @NotBlank
    @Size(max = 50)
    private String nameModule;

    @NotBlank
    @Size(max = 150)
    private String descriptionModule;

}
