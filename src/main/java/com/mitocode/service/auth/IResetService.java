package com.mitocode.service.auth;

import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.dto.response.MessageResponseDTO;
import com.mitocode.dto.request.auth.PasswordResetRequest;
import com.mitocode.dto.request.auth.ValidOtpDTO;
import com.mitocode.dto.response.NotificationMessageResponse;
import com.mitocode.model.auth.Reset;
import com.mitocode.service.ICRUD;
import com.mitocode.util.ApiResponseUtil;

import java.util.Map;

public interface IResetService extends ICRUD<Reset, Integer> {

    // Metodo para enviar un OTP para restablecimiento de contraseña
    ApiResponseUtil<NotificationMessageResponse> sendOTPForPasswordReset(PasswordResetRequest passwordResetRequestDTO);

    ApiResponseUtil<NotificationMessageResponse> sendMailForPasswordReset(NotificationMessageRequest sendMailDTO);

    // Metodo para validar el OTP
    ApiResponseUtil<ValidOtpDTO> validateOTP(PasswordResetRequest dto);

    ApiResponseUtil<Map<String, Object>> checkRandom(String random);

    ApiResponseUtil<MessageResponseDTO> resetPassword(String random, PasswordResetRequest requestDTO);


}
