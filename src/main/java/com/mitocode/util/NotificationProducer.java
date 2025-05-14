package com.mitocode.util;

import com.mitocode.config.RabbitMQConfig;
import com.mitocode.dto.request.communication.NotificationMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public Boolean sendNotification(NotificationMessageRequest message, long delayInMillis) {
        try {
            // Crear las propiedades del mensaje
            MessageProperties properties = new MessageProperties();
            properties.setHeader("x-delay", delayInMillis); // Retraso en milisegundos

            // Crear el mensaje
            Message amqpMessage = rabbitTemplate.getMessageConverter().toMessage(message, properties);

            // Enviar el mensaje al intercambiador retrasado
            rabbitTemplate.send(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, amqpMessage);
            System.out.println("Mensaje enviado con retraso de " + delayInMillis + " ms: " + message);

            return true; // Indica que el mensaje se envió correctamente
        } catch (Exception e) {
            System.err.println("Error al enviar mensaje a RabbitMQ: " + e.getMessage());
            return false; // Indica que hubo un error
        }
    }

}