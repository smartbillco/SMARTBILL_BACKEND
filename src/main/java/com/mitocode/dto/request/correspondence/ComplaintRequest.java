package com.mitocode.dto.request.correspondence;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComplaintRequest {

    @NotBlank
    private String name;

    @Email(message = "{email.invalid}")
    @NotBlank
    private String email;

    @NotBlank
    private String subject;

    @NotBlank
    private String complaintType;

    @NotBlank
    private String message;
}
