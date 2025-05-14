package com.mitocode.service.communication;

public interface IGeminiService {
    String getAiResponse(String userMessage);

    void processWhatsAppMessage(String userNumber, String userMessage);
}