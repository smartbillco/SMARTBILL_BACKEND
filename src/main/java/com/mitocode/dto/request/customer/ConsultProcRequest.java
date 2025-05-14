package com.mitocode.dto.request.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsultProcRequest {
    private Integer quantity;
    private String  consultdate;
}
