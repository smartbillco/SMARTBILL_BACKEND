package com.mitocode.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegimeRequest {

    private Integer idRegime;

    @NotNull
    private String nameRegime;

    @NotNull
    private String descriptionRegime;
}
