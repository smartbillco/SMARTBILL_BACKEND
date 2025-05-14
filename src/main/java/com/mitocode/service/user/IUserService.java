package com.mitocode.service.user;

import com.mitocode.dto.request.user.UserRequest;
import com.mitocode.dto.response.UserResponse;
import com.mitocode.model.user.User;
import com.mitocode.service.ICRUD;
import com.mitocode.util.ApiResponseUtil;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface IUserService extends ICRUD<User, Integer> {

    ApiResponseUtil<UserResponse> registerUser(UserRequest dto, MultipartFile photo);

    ApiResponseUtil<UserResponse> updateUserPhoto(Integer userId, MultipartFile photo);

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
