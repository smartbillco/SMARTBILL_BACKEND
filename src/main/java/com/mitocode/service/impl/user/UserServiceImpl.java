package com.mitocode.service.impl.user;

import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.dto.request.user.UserRequest;
import com.mitocode.dto.request.user.UserUpdateRequest;
import com.mitocode.dto.response.NotificationMessageResponse;
import com.mitocode.dto.response.UserResponse;
import com.mitocode.exception.*;
import com.mitocode.exception.user.DocumentAlreadyRegisteredException;
import com.mitocode.exception.user.EmailAlreadyExistsException;
import com.mitocode.exception.user.InvalidDocumentTypeException;
import com.mitocode.exception.user.UsernameAlreadyExistsException;
import com.mitocode.model.user.*;
import com.mitocode.repo.*;
import com.mitocode.repo.user.*;
import com.mitocode.service.communication.ITwilioService;
import com.mitocode.service.file.IFileUploadService;
import com.mitocode.service.user.IUserService;
import com.mitocode.service.impl.CRUDImpl;
import com.mitocode.util.ApiResponseUtil;
import com.mitocode.util.MapperUtil;
import com.mitocode.util.NotificationMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends CRUDImpl<User, Integer> implements IUserService {

    private final IUserRepo repo;
    private final IDocTypeRepo docTypeRepo;
    private final IRegimeRepo regimeRepo;
    private final IRoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final MapperUtil mapperUtil;
    private final IFileUploadService fileUploadService;
    private final ITwilioService twilioService;
    private final IUserConfigRepo userConfigRepo;

    @Override
    protected IGenericRepo<User, Integer> getRepo() {
        return repo;
    }

    @Override
    public ApiResponseUtil<UserResponse> registerUser(UserRequest dto, MultipartFile photo) {
        try {
            if (repo.existsByDocumentNumber(dto.getDocumentNumber())) {
                throw new DocumentAlreadyRegisteredException("Error: El número de documento ya está en uso");
            }

            if (repo.existsByUsername(dto.getUsername())) {
                throw new UsernameAlreadyExistsException("Error: El nombre de usuario ya está en uso");
            }

            if (repo.existsByEmail(dto.getEmail())) {
                throw new EmailAlreadyExistsException("Error: El correo electrónico ya está en uso");
            }

            DocType documentType = docTypeRepo.findByNameDocument(dto.getDocumentType());
            Regime regime = regimeRepo.findByNameRegime(dto.getRegime());

            if (documentType == null || regime == null) {
                throw new InvalidDocumentTypeException("Error: Tipo de documento o régimen inválido");
            }

            Role roleUser = roleRepo.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Error: El rol 'USER' no se encontró"));

            // Mapea el DTO a una entidad User
            User userToSave = mapperUtil.map(dto, User.class);
            userToSave.setDocumentType(documentType);
            userToSave.setRegime(regime);
            userToSave.setPassword(passwordEncoder.encode(dto.getPassword()));
            userToSave.setRoles(Collections.singletonList(roleUser));

            // Guarda el usuario
            User savedUser = repo.save(userToSave);

            // Luego, creamos la configuración por defecto
            UserConfig defaultConfig = new UserConfig();
            defaultConfig.setUser(savedUser);
            defaultConfig.setCurrencyKey("USD");
            defaultConfig.setCurrency("Dólar");
            defaultConfig.setStorageType("local");
            defaultConfig.setLanguage("es");
            defaultConfig.setSmsNotifications(true);
            defaultConfig.setEmailNotifications(true);
            defaultConfig.setPushNotifications(false);

            // Guardamos la configuración
            userConfigRepo.save(defaultConfig);

            // Subir la foto si se proporciona
            if (photo != null && !photo.isEmpty()) {
                Optional<UserConfig> optionalUserConfig = userConfigRepo.findByUserUsername(userToSave.getUsername());
                if (optionalUserConfig.isEmpty()) {
                    return new ApiResponseUtil<>(false, "Error: configuracion para el usuario no encontrada", null);
                }
                UserConfig userConfig = optionalUserConfig.get();
                String photoUrl = fileUploadService.uploadImage(photo, dto.getUsername());
                savedUser.setPhoto_url(photoUrl);
                repo.save(savedUser);  // Actualizamos el usuario con la URL de la foto
            }

            // Preparar el mensaje de bienvenida para Twilio
            Map<String, Object> model = new HashMap<>();
            model.put("message", "¡Hola " + dto.getUsername() + "! 🎉 Bienvenido a SmartBill. Tu cuenta ha sido creada con éxito.");

            NotificationMessageRequest notificationMessage = NotificationMessageUtil.createSmsNotification(
                    dto.getPhoneNumber(), "Mensaje De Bienvenida!", model);

            // Enviar mensaje de bienvenida por WhatsApp
            try {
                ApiResponseUtil<NotificationMessageResponse> messageResponse = twilioService.sendMessage(notificationMessage);

                if (!messageResponse.isSuccess()) {
                    return new ApiResponseUtil<>(true,
                            "Usuario registrado, pero hubo un problema al enviar el mensaje de bienvenida.",
                            mapperUtil.map(savedUser, UserResponse.class));
                }
            } catch (Exception e) {
                return new ApiResponseUtil<>(true,
                        "Usuario registrado, pero hubo un error al enviar el mensaje de bienvenida.",
                        mapperUtil.map(savedUser, UserResponse.class));
            }

            // Convierte el usuario guardado de nuevo a DTO
            UserResponse savedUserDTO = mapperUtil.map(savedUser, UserResponse.class);

            return new ApiResponseUtil<>(true, "Usuario registrado exitosamente", savedUserDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponseUtil<>(false, "Error interno del servidor: " + e.getMessage(), null);
        }
    }

    @Transactional
    public ApiResponseUtil<UserResponse> updateUser(Integer userId, UserUpdateRequest userRequest, MultipartFile photo) {
        // Buscar usuario por ID
        User existingUser = repo.findById(userId)
                .orElseThrow(() -> new ModelNotFoundException("Usuario con ID " + userId + " no encontrado"));

        // Validar que el nuevo username, email y documento no sean de otro usuario
        if (!existingUser.getDocumentNumber().equals(userRequest.getDocumentNumber()) &&
                repo.existsByDocumentNumber(userRequest.getDocumentNumber())) {
            throw new DocumentAlreadyRegisteredException("Error: El número de documento ya está en uso");
        }

        if (!existingUser.getUsername().equals(userRequest.getUsername()) &&
                repo.existsByUsername(userRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Error: El nombre de usuario ya está en uso");
        }

        if (!existingUser.getEmail().equals(userRequest.getEmail()) &&
                repo.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Error: El correo electrónico ya está en uso");
        }

        // Obtener tipos de documento y régimen
        DocType documentType = docTypeRepo.findByNameDocument(userRequest.getDocumentType());
        Regime regime = regimeRepo.findByNameRegime(userRequest.getRegime());

        if (documentType == null || regime == null) {
            throw new InvalidDocumentTypeException("Error: Tipo de documento o régimen inválido");
        }

        // Actualizar datos del usuario
        existingUser.setFirstName(userRequest.getFirstName());
        existingUser.setSecondName(userRequest.getSecondName());
        existingUser.setLastName(userRequest.getLastName());
        existingUser.setSecondLastName(userRequest.getSecondLastName());
        existingUser.setUsername(userRequest.getUsername());
        existingUser.setAddress(userRequest.getAddress());
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setPhoneNumber(userRequest.getPhoneNumber());
        existingUser.setDocumentNumber(userRequest.getDocumentNumber());
        existingUser.setDocumentType(documentType);
        existingUser.setRegime(regime);

        // Subir nueva foto si se proporciona
        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileUploadService.uploadImage(photo, userRequest.getUsername());
            existingUser.setPhoto_url(photoUrl);
        }

        // Guardar cambios en la base de datos
        User updatedUser = repo.save(existingUser);

        // Convertir a DTO para la respuesta
        UserResponse updatedUserDTO = mapperUtil.map(updatedUser, UserResponse.class);

        return new ApiResponseUtil<>(true, "Usuario actualizado correctamente", updatedUserDTO);
    }

    @Transactional
    public ApiResponseUtil<UserResponse> updateUserPhoto(Integer userId, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            return new ApiResponseUtil<>(false, "No se proporcionó ninguna foto", null);
        }

        // Buscar usuario por ID
        User user = repo.findById(userId)
                .orElseThrow(() -> new ModelNotFoundException("Usuario con ID " + userId + " no encontrado"));

        // Subir la nueva foto
        String photoUrl = fileUploadService.uploadImage(photo, user.getUsername());
        user.setPhoto_url(photoUrl);

        // Guardar el cambio
        User updatedUser = repo.save(user);

        // Retornar la respuesta
        UserResponse userResponse = mapperUtil.map(updatedUser, UserResponse.class);
        return new ApiResponseUtil<>(true, "Foto de usuario actualizada correctamente", userResponse);
    }

    @Transactional
    public ApiResponseUtil<UserResponse> deleteUserPhoto(Integer userId) {
        // Buscar usuario por ID
        User user = repo.findById(userId)
                .orElseThrow(() -> new ModelNotFoundException("Usuario con ID " + userId + " no encontrado"));

        // Eliminar la imagen del almacenamiento (opcional si manejas archivos físicos/remotos)
        if (user.getPhoto_url() != null) {
            fileUploadService.deleteImageByUrl(user.getPhoto_url());
        }

        // Eliminar la URL de la foto
        user.setPhoto_url(null);

        // Guardar los cambios
        User updatedUser = repo.save(user);

        // Retornar la respuesta
        return new ApiResponseUtil<>(true, "Foto de perfil eliminada correctamente", null);
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return repo.existsByDocumentNumber(documentNumber);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repo.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repo.existsByEmail(email);
    }

    @Override
    public Optional<User> findByDocumentNumber(String documentNumber) {
        return repo.findByDocumentNumber(documentNumber)
                .or(() -> {
                    throw new ModelNotFoundException("Usuario con documento " + documentNumber + " no encontrado");
                });
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username)
                .or(() -> {
                    throw new ModelNotFoundException("Usuario con nombre de usuario " + username + " no encontrado");
                });
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email)
                .or(() -> {
                    throw new ModelNotFoundException("Usuario con email " + email + " no encontrado");
                });
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return repo.findByPhoneNumber(phoneNumber)
                .or(() -> {
                    throw new ModelNotFoundException("Usuario con número de teléfono " + phoneNumber + " no encontrado");
                });
    }
}
