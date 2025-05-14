package com.mitocode.controller.auth;

import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.dto.request.auth.PasswordResetRequest;
import com.mitocode.dto.request.auth.ValidOtpDTO;
import com.mitocode.dto.response.MessageResponseDTO;
import com.mitocode.dto.response.NotificationMessageResponse;
import com.mitocode.service.auth.IResetService;
import com.mitocode.service.communication.IMailService;
import com.mitocode.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/reset")
@RequiredArgsConstructor
public class ResetController {

    private final IResetService resetService;
    private final IMailService mailService;

    @PostMapping("/sendOtp")
    public ResponseEntity<ApiResponseUtil<NotificationMessageResponse>> sendOTP(@RequestBody PasswordResetRequest dto) {
        ApiResponseUtil<NotificationMessageResponse> response = resetService.sendOTPForPasswordReset(dto);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/sendMail")
    public ResponseEntity<ApiResponseUtil<NotificationMessageResponse>> sendMail(@RequestBody NotificationMessageRequest dto) {
        ApiResponseUtil<NotificationMessageResponse> response = resetService.sendMailForPasswordReset(dto);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Notificación enviada exitosamente.", response.getData()));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponseUtil<ValidOtpDTO>> validateOTP(@RequestBody PasswordResetRequest dto) {
        ApiResponseUtil<ValidOtpDTO> response = resetService.validateOTP(dto);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/reset-password/{random}")
    public ResponseEntity<ApiResponseUtil<MessageResponseDTO>> resetPassword(@PathVariable("random") String random, @RequestBody PasswordResetRequest requestDTO) {
        ApiResponseUtil<MessageResponseDTO> response = resetService.resetPassword(random, requestDTO);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }


    @GetMapping("/check/{random}")
    public ResponseEntity<ApiResponseUtil<Map<String, Object>>> checkRandom(@PathVariable("random") String random) {
        ApiResponseUtil<Map<String, Object>> response = resetService.checkRandom(random);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }
}