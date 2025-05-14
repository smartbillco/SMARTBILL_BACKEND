package com.mitocode.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "smartbill_queue";
    public static final String EXCHANGE_NAME = "smartbill_exchange";
    public static final String ROUTING_KEY = "smartbill_routingkey";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true); // Cola duradera
    }

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct"); // Tipo de intercambiador subyacente
        return new CustomExchange(EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

    @Bean
    public Binding binding(Queue queue, CustomExchange delayedExchange) {
        return BindingBuilder.bind(queue).to(delayedExchange).with(ROUTING_KEY).noargs();
    }
}