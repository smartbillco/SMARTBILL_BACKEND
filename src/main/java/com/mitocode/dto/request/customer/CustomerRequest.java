package com.mitocode.dto.request.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequest {

    private Integer idDoctype;
    private Integer idCustomer;

    @NotNull
    @Size(min = 3, max = 70, message = "{firstname.size}")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "{firstname.invalid}")
    private String firstName;

    @NotNull
    @Size(min = 3, max = 70, message = "{firstname.size}")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "{firstname.invalid}")
    private String secondName;

    @NotNull
    @Size(min = 3, max = 70, message = "{firstname.size}")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "{firstname.invalid}")
    private String lastName;

    @NotNull
    @Size(min = 3, max = 70, message = "{lastname.size}")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "{lastname.invalid}")
    private String secondLastName;

    @NotNull
    @Size(min = 2, max = 20, message = "{documentType.size}")
    private String documentType;

    @NotNull
    @Size(min = 3, max = 20, message = "{documentNumber.size}")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "{documentNumber.invalid}")
    private String documentNumber;

    @NotNull
    @Size(min = 7, max = 15, message = "{phoneNumber.size}")
    @Pattern(regexp = "^\\+?[0-9]{1,3}?[ ]?[0-9]{7,15}$", message = "{phoneNumber.invalid}")
    private String phoneNumber;

    @NotNull
    @Size(min = 5, max = 150, message = "{address.size}")
    @Pattern(regexp = "^[\\w\\s.,#-]+$", message = "{address.invalid}")
    private String address;

    @NotNull
    @Email(message = "{email.invalid}")
    @Size(max = 80, message = "{email.size}")
    private String email;

    @NotNull
    @Past(message = "{dateOfBirth.past}")
    private LocalDateTime dateOfBirth;

    @NotNull
    @Size(min = 3, max = 80, message = "{regime.size}")
    private String regime;

    private String photo_url;


}