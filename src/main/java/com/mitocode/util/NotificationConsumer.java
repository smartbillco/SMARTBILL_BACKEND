package com.mitocode.util;

import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.service.communication.ITwilioService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailUtil emailUtil;
    private final ITwilioService twilioService;

    @RabbitListener(queues = "smartbill_queue")
    public void receiveNotification(NotificationMessageRequest message) {
        System.out.println("Mensaje recibido de RabbitMQ: " + message);

        try {
            if (message.getType() == null) {
                throw new IllegalArgumentException("El campo 'type' no puede ser nulo.");
            }

            // Procesar el mensaje según el tipo de notificación
            switch (message.getType().toUpperCase()) {
                case "EMAIL":
                case "COMPLAINT-EMAIL":
                case "CORRESPONDENCE":
                    sendEmail(message);
                    break;
                case "TWILIO":
                    sendTwilioMessage(message);
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de notificación no soportado: " + message.getType());
            }
        } catch (Exception e) {
            System.err.println("Error al procesar el mensaje: " + e.getMessage());
            throw e;
        }
    }

    private void sendEmail(NotificationMessageRequest message) {
        try {
            message.setType(message.getType().toUpperCase());
            message.setFrom("caam.ingenierias@gmail.com");
            message.setTo(message.getTo());
            message.setSubject(message.getSubject());
            message.setModel(message.getModel());

            emailUtil.sendMail(message);
            //System.out.println("Correo enviado exitosamente a: " + message.getTo());
        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }
    }

    private void sendTwilioMessage(NotificationMessageRequest message) {
        try {
            // Asegurar que obtenemos solo un número como String
            String phoneNumber = message.getTo().isEmpty() ? null : message.getTo().get(0);
            message.setType(message.getType().toUpperCase());
            message.setTo(Collections.singletonList(phoneNumber));
            message.setSubject(message.getSubject());
            message.setModel(message.getModel());

            twilioService.sendWhatsAppMessage(message);
            System.out.println("Mensaje de Twilio enviado exitosamente a: " + phoneNumber);
        } catch (Exception e) {
            System.err.println("Error al enviar mensaje de Twilio: " + e.getMessage());
        }
    }
}