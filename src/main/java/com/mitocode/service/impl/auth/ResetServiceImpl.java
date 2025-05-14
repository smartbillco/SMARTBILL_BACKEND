package com.mitocode.service.impl.auth;

import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.dto.request.auth.PasswordResetRequest;
import com.mitocode.dto.request.auth.ValidOtpDTO;
import com.mitocode.dto.response.MessageResponseDTO;
import com.mitocode.dto.response.NotificationMessageResponse;
import com.mitocode.dto.response.OtpStatus;
import com.mitocode.model.auth.Reset;
import com.mitocode.model.user.User;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.repo.auth.IResetRepo;
import com.mitocode.repo.user.IUserRepo;
import com.mitocode.service.communication.IMailService;
import com.mitocode.service.auth.IResetService;
import com.mitocode.service.communication.ITwilioService;
import com.mitocode.service.impl.CRUDImpl;
import com.mitocode.util.ApiResponseUtil;
import com.mitocode.exception.ModelNotFoundException;
import com.mitocode.util.NotificationMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResetServiceImpl extends CRUDImpl<Reset, Integer> implements IResetService {

    private final IResetRepo repo;
    private final ITwilioService twilioService;
    private final IUserRepo userRepository;
    private final LoginServiceImpl loginService;
    private final IMailService mailService;

    @Override
    protected IGenericRepo<Reset, Integer> getRepo() {
        return repo;
    }

    private final Map<String, String> otpMap = new HashMap<>();

    @Override
    public ApiResponseUtil<NotificationMessageResponse> sendOTPForPasswordReset(PasswordResetRequest passwordResetRequestDTO) {
        User user = userRepository.findByUsername(passwordResetRequestDTO.getUsername())
                .orElseThrow(() -> new ModelNotFoundException("No se encontro el usuario con el nombre de usuario: " + passwordResetRequestDTO.getUsername()));

        String otp = generateOTP();

        // Construir el modelo del mensaje
        Map<String, Object> model = new HashMap<>();
        model.put("message", "Estimado cliente, su código OTP es: " + otp);

        /*
            Crear la solicitud de notificación para RabbitMQ
        NotificationMessageRequest notificationMessage = new NotificationMessageRequest();
        notificationMessage.setType("twilio");
        notificationMessage.setTo(Collections.singletonList(user.getPhoneNumber()));
        notificationMessage.setSubject("Código de verificación OTP");
        notificationMessage.setModel(model);
         */

        NotificationMessageRequest notificationMessage = NotificationMessageUtil.createSmsNotification(
                user.getPhoneNumber(),
                "Código de verificación OTP",
                model
        );

        // Enviar el mensaje a RabbitMQ a traves de twilioService
        return twilioService.sendMessage(notificationMessage);
    }

    @Override
    public ApiResponseUtil<NotificationMessageResponse> sendMailForPasswordReset(NotificationMessageRequest sendMailDTO) {
        final int EXPIRATION_TIME = 2; // Tiempo de expiración en minutos

        String username = sendMailDTO.getTo().isEmpty() ? null : sendMailDTO.getTo().get(0);
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new ModelNotFoundException("No se encontro el usuario con nombre: " + username);
        }

        User user = userOptional.get();

        // Crear y persistir la entidad Reset
        Reset reset = new Reset();
        reset.setRandom(UUID.randomUUID().toString());
        reset.setUser(user);
        reset.setExpiration(EXPIRATION_TIME);
        repo.save(reset);

        // Construir el mensaje de notificación
        Map<String, Object> model = new HashMap<>();
        String url = "http://localhost:4200/forgot/" + reset.getRandom();
        model.put("user", user.getUsername());
        model.put("resetUrl", url);

        NotificationMessageRequest notificationMessage = NotificationMessageUtil.createEmailNotification(
                user.getEmail(),
                "RESET PASSWORD SMARTBILL",
                model
        );

        // Enviar el mensaje a RabbitMQ a través de MailServiceImpl
        mailService.sendMail(notificationMessage);

        // Construir la respuesta
        NotificationMessageResponse response = new NotificationMessageResponse(
                notificationMessage.getType(),
                notificationMessage.getFrom(),
                user.getEmail(),
                notificationMessage.getSubject(),
                notificationMessage.getModel()
        );

        return new ApiResponseUtil<>(true, "Correo enviado exitosamente.", response);
    }

    private String generateOTP() {
        return new DecimalFormat("000000").format(new Random().nextInt(999999));
    }

    @Override
    public ApiResponseUtil<ValidOtpDTO> validateOTP(PasswordResetRequest dto) {
        String userInputOtp = dto.getOnetimepassword();
        String username = dto.getUsername();
        String storedOtp = otpMap.get(username);
        if (storedOtp == null) {
            return new ApiResponseUtil<>(false, "No se ha encontrado un OTP para el usuario: " + username, null);
        }

        if (userInputOtp.equals(storedOtp)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ModelNotFoundException("Usuario no encontrado."));

            Reset reset = new Reset();
            reset.setRandom(UUID.randomUUID().toString());
            reset.setUser(user);
            reset.setExpiration(10); // Tiempo de expiración en minutos
            repo.save(reset);

            return new ApiResponseUtil<>(true, "OTP válido.", new ValidOtpDTO(true, "OTP válido.", reset.getRandom()));
        } else {
            return new ApiResponseUtil<>(false, "OTP inválido.", null);
        }
    }

    @Scheduled(fixedRate = 10000)
    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        List<Reset> expiredTokens = repo.findAllByExpirationDateBefore(now);
        if (!expiredTokens.isEmpty()) {
            repo.deleteAll(expiredTokens);
            System.out.println(expiredTokens.size() + " tokens expirados eliminados.");
        }
    }

    @Override
    public ApiResponseUtil<Map<String, Object>> checkRandom(String random) {
        try {
            Reset rm = repo.findByRandom(random);
            if (rm != null && rm.getId() > 0) {
                if (!rm.isExpired()) {
                    return new ApiResponseUtil<>(true, "El token es válido.", Map.of("message", "El token es válido."));
                } else {
                    //expiredTokenException
                    return new ApiResponseUtil<>(false, "El token ha expirado.", Map.of("error", "El token ha expirado."));
                }
            } else {
                //ModelNotFoundException
                return new ApiResponseUtil<>(false, "No se encontró el token.", Map.of("error", "No se encontró el token."));
            }
        } catch (Exception e) {
            return new ApiResponseUtil<>(false, "Error al comprobar el token.", Map.of("error", e.getMessage()));
        }
    }

    @Override
    public ApiResponseUtil<MessageResponseDTO> resetPassword(String random, PasswordResetRequest requestDTO) {
        Reset reset = repo.findByRandom(random);
        if (reset == null || reset.isExpired()) {
            //InvalidTokenException
            return new ApiResponseUtil<>(false, "Token inválido o expirado.", null);
        }
        loginService.changePassword(requestDTO.getNewPassword(), reset.getUser().getUsername());
        repo.delete(reset);

        return new ApiResponseUtil<>(true, "Contraseña restablecida con éxito.", new MessageResponseDTO(OtpStatus.DELIVERED, "Contraseña restablecida con éxito."));
    }
}