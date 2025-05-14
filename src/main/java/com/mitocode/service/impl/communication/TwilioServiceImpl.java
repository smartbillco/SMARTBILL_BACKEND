package com.mitocode.service.impl.communication;

import com.mitocode.config.TwilioConfig;
import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.dto.response.NotificationMessageResponse;
import com.mitocode.service.communication.ITwilioService;
import com.mitocode.util.ApiResponseUtil;
import com.mitocode.util.NotificationProducer;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TwilioServiceImpl implements ITwilioService {

    private final TwilioConfig twilioConfig;
    private final NotificationProducer notificationProducer;

    // Metodo para enviar mensaje de WhatsApp
    @Override
    public ApiResponseUtil<NotificationMessageResponse> sendWhatsAppMessage(NotificationMessageRequest request) {
        try {
            // Asegurar que obtenemos solo un número como String
            String phoneNumber = request.getTo().isEmpty() ? null : request.getTo().get(0);

            if (phoneNumber == null) {
                return new ApiResponseUtil<>(false, "Número de teléfono no válido", null);
            }

            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + phoneNumber),
                    new PhoneNumber("whatsapp:" + twilioConfig.getTrialNumber()),
                    request.getModel().get("message").toString()
            ).create();

            NotificationMessageResponse response = new NotificationMessageResponse();
            response.setType("TWILIO");
            response.setTo(phoneNumber);
            response.setSubject("Envío de WhatsApp");
            response.setModel(request.getModel());

            return new ApiResponseUtil<>(true, "Mensaje enviado con éxito", response);

        } catch (ApiException e) {
            NotificationMessageResponse response = new NotificationMessageResponse();
            response.setType("TWILIO");
            response.setTo(String.valueOf(request.getTo()));
            response.setSubject("Error en envío de WhatsApp");
            response.setModel(request.getModel());

            return new ApiResponseUtil<>(false, "Error: " + e.getMessage(), response);
        }
    }

    // Metodo para enviar mensaje SMS con RabbitMQ
    @Override
    public ApiResponseUtil<NotificationMessageResponse> sendMessage(NotificationMessageRequest request) {

        // Asegurar que obtenemos solo un número como String
        String phoneNumber = request.getTo().isEmpty() ? null : request.getTo().get(0);

        if (phoneNumber == null) {
            return new ApiResponseUtil<>(false, "Número de teléfono no válido", null);
        }

        // Construir el mensaje para RabbitMQ
        NotificationMessageResponse message = new NotificationMessageResponse();
        message.setType("TWILIO");
        message.setTo(phoneNumber);
        message.setSubject("Envío de SMS");
        message.setModel(request.getModel());

        // Enviar el mensaje a RabbitMQ con un delay de 10 segundos (10000 ms)
        notificationProducer.sendNotification(request, 10000);
        return new ApiResponseUtil<>(true, "Mensaje enviado con éxito", message);
    }
}
