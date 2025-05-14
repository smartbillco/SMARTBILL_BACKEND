package com.mitocode.controller.communication;

import com.mitocode.model.communication.ChatMessage;
import com.mitocode.service.communication.IGeminiService;
import com.mitocode.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/chatbots")
@RequiredArgsConstructor
public class ChatBotController {

    private final IGeminiService geminiService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @PostMapping(value = "/message")
    public ResponseEntity<ApiResponseUtil<ChatMessage>> handleChatMessage(@RequestBody ChatMessage message) {
        //System.out.println("📩 Mensaje recibido: " + message);

        String botReply = geminiService.getAiResponse(message.getText());
        ChatMessage botResponse = new ChatMessage(
                message.getId() + 1,
                "bot",
                botReply,
                LocalTime.now().format(TIME_FORMATTER),
                "ChatBot Gemini"
        );

        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Respuesta exitosa", botResponse));
    }
}
