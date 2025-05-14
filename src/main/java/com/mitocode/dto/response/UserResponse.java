package com.mitocode.dto.response;

import com.mitocode.model.user.Role;
import lombok.Data;

import java.util.List;

@Data
public class UserResponse {

    private Integer idUser;
    private String address;
    private String documentNumber;
    private String email;
    private String firstName;
    private Boolean enabled;
    private String lastName;
    //private String password;
    private String phoneNumber;
    private String photo_url;
    private String secondLastName;
    private String secondName;
    private String username;
    private String documentType;
    private String regime;
    private List<Role> roles;

}
