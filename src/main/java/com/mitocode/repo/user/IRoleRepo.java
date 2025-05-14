package com.mitocode.repo.user;

import com.mitocode.model.user.Role;
import com.mitocode.repo.IGenericRepo;

import java.util.Optional;

public interface IRoleRepo extends IGenericRepo<Role, Integer> {
    Optional<Role> findByName(String name);  // Metodo para buscar por nombre de rol
}
