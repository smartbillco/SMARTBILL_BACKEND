package com.mitocode.repo.user;

import com.mitocode.model.user.User;
import com.mitocode.repo.IGenericRepo;

import java.util.Optional;

public interface IUserRepo extends IGenericRepo<User, Integer> {

    // Búsqueda por username
    Optional<User> findByUsername(String username);

    // Búsqueda por número de documento
    Optional<User> findByDocumentNumber(String documentNumber);

    // Búsqueda por email para asegurar que no haya duplicados
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    // Nuevo metodo para verificar la existencia del documentNumber
    boolean existsByDocumentNumber(String documentNumber);

    // Metodo para verificar si un usuario existe por su username
    boolean existsByUsername(String username);

    // Metodo para verificar si un usuario existe por su email
    boolean existsByEmail(String email);
}
