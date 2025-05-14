package com.mitocode.controller.auth;

import com.mitocode.dto.request.user.UserRequest;
import com.mitocode.dto.response.UserResponse;
import com.mitocode.security.*;
import com.mitocode.service.user.IUserService;
import com.mitocode.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController()
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseUtil<UserResponse>> register(
            @ModelAttribute UserRequest dto,
            @RequestParam MultipartFile photo) {

        ApiResponseUtil<UserResponse> response = userService.registerUser(dto, photo);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseUtil<JwtResponse>> login(@RequestBody JwtRequest req) {
        try {
            authenticate(req.getUsername(), req.getPassword());

            userService.findByUsername(req.getUsername()).ifPresent(user -> {
                user.setEnabled(true);
                userService.save(user);
            });

            final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(req.getUsername());
            final String token = jwtTokenUtil.generateToken(userDetails);

            return ResponseEntity.ok(new ApiResponseUtil<>(true, "Inicio de sesión exitoso", new JwtResponse(token)));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(400).body(new ApiResponseUtil<>(false, "Credenciales inválidas.", null));
        } catch (DisabledException e) {
            return ResponseEntity.status(401).body(new ApiResponseUtil<>(false, "El usuario está deshabilitado.", null));
        }
    }

    private void authenticate(String username, String password) {
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("INVALID_CREDENTIALS");
        }
    }
}
