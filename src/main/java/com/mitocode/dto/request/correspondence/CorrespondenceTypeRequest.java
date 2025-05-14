package com.mitocode.dto.request.correspondence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorrespondenceTypeRequest {
    private String name;
    private String description;
}
