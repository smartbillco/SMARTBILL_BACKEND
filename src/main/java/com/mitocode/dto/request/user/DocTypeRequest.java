package com.mitocode.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocTypeRequest {

    private Integer idDocType;

    @NotNull
    private String nameDocumentType;

    @NotNull
    private String descriptionDocumentType;
}
