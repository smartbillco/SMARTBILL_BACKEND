package com.mitocode.service.impl.communication;

import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.dto.response.NotificationMessageResponse;
import com.mitocode.service.communication.IMailService;
import com.mitocode.util.ApiResponseUtil;
import com.mitocode.util.NotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements IMailService {

    private final NotificationProducer notificationProducer;

    @Override
    public ApiResponseUtil<Map<String, Object>> sendMail(NotificationMessageRequest notificationMessageRequestDTO) {
        try {
            // Construir el mensaje para RabbitMQ
            NotificationMessageResponse message = new NotificationMessageResponse();
            message.setType(notificationMessageRequestDTO.getType());
            message.setFrom(notificationMessageRequestDTO.getFrom());
            message.setTo(String.join(",", notificationMessageRequestDTO.getTo()));
            message.setSubject(notificationMessageRequestDTO.getSubject());
            message.setModel(notificationMessageRequestDTO.getModel());

            // Enviar el mensaje a RabbitMQ
            notificationProducer.sendNotification(notificationMessageRequestDTO, 10000);

            // Respuesta exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Correo enviado exitosamente.");
            return new ApiResponseUtil<>(true, "Solicitud procesada.", response);
        } catch (Exception e) {
            // Manejo de errores
            return new ApiResponseUtil<>(false, "Error al procesar la solicitud.", Map.of("error", e.getMessage()));
        }
    }
}