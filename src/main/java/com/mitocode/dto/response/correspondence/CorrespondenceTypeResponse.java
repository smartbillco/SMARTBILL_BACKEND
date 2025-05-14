package com.mitocode.dto.response.correspondence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorrespondenceTypeResponse {
    private Long id;
    private String name;
    private String description;
}
