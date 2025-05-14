package com.mitocode.dto.request.correspondence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorrespondenceSubtypeRequest {
    private String name;
    private String description;
    private Long typeId;
}
