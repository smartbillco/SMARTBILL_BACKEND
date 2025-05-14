package com.mitocode.dto.request.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ValidOtpDTO {
    private boolean valid;
    private String message;
    private String token;
}
