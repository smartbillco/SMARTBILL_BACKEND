package com.mitocode.controller.communication;

import com.mitocode.service.communication.IGeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/twilio")
@RequiredArgsConstructor
public class TwilioWebhookController {

    private final IGeminiService geminiService;

    @PostMapping("/webhook")
    public void receiveWhatsAppMessage(@RequestParam("From") String from, @RequestParam("Body") String body) {
        //System.out.println("Mensaje recibido de: " + from + " - " + body);
        geminiService.processWhatsAppMessage(from.replace("whatsapp:", ""), body);
    }
}
