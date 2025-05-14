package com.mitocode.service.user;

import com.mitocode.model.user.Role;
import com.mitocode.service.ICRUD;

import java.util.Optional;

public interface IRoleService extends ICRUD<Role, Integer> {

    Optional<Role> findByName(String name);  // Metodo para buscar por nombre de rol
}
