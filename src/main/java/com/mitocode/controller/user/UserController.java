package com.mitocode.controller.user;

import com.mitocode.dto.request.user.UserConfigRequest;
import com.mitocode.dto.request.user.UserUpdateRequest;
import com.mitocode.dto.response.UserConfigResponse;
import com.mitocode.dto.response.UserResponse;
import com.mitocode.model.user.User;
import com.mitocode.model.user.UserConfig;
import com.mitocode.service.impl.user.UserConfigServiceImpl;
import com.mitocode.service.impl.user.UserServiceImpl;
import com.mitocode.service.user.IUserService;
import com.mitocode.util.ApiResponseUtil;
import com.mitocode.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService service;
    private final MapperUtil mapperUtil;
    private final UserConfigServiceImpl userConfigServiceImpl;
    private final UserServiceImpl userServiceImpl;

    @GetMapping
    public ResponseEntity<ApiResponseUtil<List<UserResponse>>> findAll() {
        List<UserResponse> list = mapperUtil.mapList(service.findAll(), UserResponse.class);
        ApiResponseUtil<List<UserResponse>> response = new ApiResponseUtil<>(true, "Usuarios encontrados", list);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<UserResponse>> findById(@PathVariable("id") Integer id) {
        User user = service.findById(id);
        UserResponse userDTO = mapperUtil.map(user, UserResponse.class);
        ApiResponseUtil<UserResponse> response = new ApiResponseUtil<>(true, "Usuario encontrado", userDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponseUtil<UserResponse>> getUserByUsername(@PathVariable("username") String username) {
        Optional<User> user = service.findByUsername(username);
        if (user.isPresent()) {
            UserResponse userDTO = mapperUtil.map(user.get(), UserResponse.class);
            ApiResponseUtil<UserResponse> response = new ApiResponseUtil<>(true, "Usuario encontrado", userDTO);
            return ResponseEntity.ok(response);
        } else {
            ApiResponseUtil<UserResponse> response = new ApiResponseUtil<>(false, "Usuario no encontrado", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponseUtil<UserResponse>> getUserByEmail(@PathVariable String email) {
        Optional<User> user = service.findByEmail(email);
        if (user.isPresent()) {
            UserResponse userDTO = mapperUtil.map(user.get(), UserResponse.class);
            ApiResponseUtil<UserResponse> response = new ApiResponseUtil<>(true, "Usuario encontrado", userDTO);
            return ResponseEntity.ok(response);
        } else {
            ApiResponseUtil<UserResponse> response = new ApiResponseUtil<>(false, "Usuario no encontrado", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<UserResponse>> updateUser(
            @PathVariable("id") Integer userId,
            @Validated @ModelAttribute UserUpdateRequest userRequest,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        ApiResponseUtil<UserResponse> response = userServiceImpl.updateUser(userId, userRequest, photo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/photo")
    public ResponseEntity<ApiResponseUtil<UserResponse>> updateUserPhoto(
            @PathVariable Integer userId,
            @RequestParam("photo") MultipartFile photo) {
        return ResponseEntity.ok(userServiceImpl.updateUserPhoto(userId, photo));
    }

    @DeleteMapping("/photo/{userId}")
    public ResponseEntity<ApiResponseUtil<UserResponse>> deleteUserPhoto(@PathVariable("userId") Integer userId) {
        ApiResponseUtil<UserResponse> response = userServiceImpl.deleteUserPhoto(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<Void>> delete(@PathVariable("id") Integer id) {
        service.delete(id);
        ApiResponseUtil<Void> response = new ApiResponseUtil<>(true, "Usuario eliminado correctamente", null);
        return ResponseEntity.ok(response);
    }

    //CONFIGURACION DEL USUARIO
    @PutMapping("config/update")
    public ResponseEntity<ApiResponseUtil<UserConfigResponse>> updateUserSettings(@Validated @RequestBody UserConfigRequest userConfigRequest) {
        userConfigServiceImpl.updateUserConfig(userConfigRequest);
        ApiResponseUtil<UserConfigResponse> response = new ApiResponseUtil<>(true, "Configuración actualizada correctamente", null);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("config/{id}")
    public ResponseEntity<ApiResponseUtil<UserConfigResponse>> getConfigById(@PathVariable("id") Integer id) {
        Optional<UserConfig> config = userConfigServiceImpl.getUserConfigById(id);
        if (config.isPresent()) {
            UserConfigResponse dto = mapperUtil.map(config.get(), UserConfigResponse.class);
            ApiResponseUtil<UserConfigResponse> response = new ApiResponseUtil<>(true, "Configuración obtenida correctamente", dto);
            return ResponseEntity.ok(response);
        } else {
            ApiResponseUtil<UserConfigResponse> response = new ApiResponseUtil<>(false, "Configuración no encontrada", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("config/username/{username}")
    public ResponseEntity<ApiResponseUtil<UserConfigResponse>> getConfigByUsername(@PathVariable("username") String username) {
        Optional<UserConfig> config = userConfigServiceImpl.getUserConfigByUsername(username);
        if (config.isPresent()) {
            UserConfigResponse dto = mapperUtil.map(config.get(), UserConfigResponse.class);
            ApiResponseUtil<UserConfigResponse> response = new ApiResponseUtil<>(true, "Configuración obtenida correctamente", dto);
            return ResponseEntity.ok(response);
        } else {
            ApiResponseUtil<UserConfigResponse> response = new ApiResponseUtil<>(false, "Configuración no encontrada", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

}
