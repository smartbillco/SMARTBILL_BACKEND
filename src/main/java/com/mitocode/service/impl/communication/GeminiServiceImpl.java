package com.mitocode.service.impl.communication;

import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.service.communication.IGeminiService;
import com.mitocode.service.communication.ITwilioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.http.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements IGeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent";

    private final ITwilioService twilioService;


    @Override
    public String getAiResponse(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            JSONObject request = new JSONObject();
            request.put("contents", Collections.singletonList(
                    new JSONObject().put("role", "user").put("parts", Collections.singletonList(
                            new JSONObject().put("text", userMessage)
                    ))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject jsonResponse = new JSONObject(response.getBody());

                JSONArray candidates = jsonResponse.optJSONArray("candidates");
                if (candidates != null && candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    JSONObject content = firstCandidate.optJSONObject("content");

                    if (content != null && content.length() > 0) {
                        JSONArray parts = content.optJSONArray("parts");
                        if (parts != null && parts.length() > 0) {
                            return parts.getJSONObject(0).optString("text", "No response text found.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al procesar la respuesta de la IA.";
        }
        return "No se pudo obtener una respuesta.";
    }

    @Override
    public void processWhatsAppMessage(String userNumber, String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            JSONObject request = new JSONObject();
            request.put("contents", Collections.singletonList(
                    new JSONObject().put("role", "user").put("parts", Collections.singletonList(
                            new JSONObject().put("text", userMessage)
                    ))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject jsonResponse = new JSONObject(response.getBody());

                JSONArray candidates = jsonResponse.optJSONArray("candidates");
                if (candidates != null && candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    JSONObject content = firstCandidate.optJSONObject("content");

                    if (content != null && content.length() > 0) {
                        JSONArray parts = content.optJSONArray("parts");
                        if (parts != null && parts.length() > 0) {
                            String botResponse = parts.getJSONObject(0).optString("text", "No response text found.");

                            // Enviar respuesta por WhatsApp
                            //twilioService.sendWhatsAppMessage(userNumber, botResponse);

                            Map<String, Object> model = new HashMap<>();
                            model.put("message", botResponse);
                            // Crear la solicitud de notificación para RabbitMQ
                            NotificationMessageRequest notificationMessage = new NotificationMessageRequest();
                            notificationMessage.setType("twilio");
                            notificationMessage.setTo(Collections.singletonList(userNumber));
                            notificationMessage.setSubject("Mensaje De ChatBot");
                            notificationMessage.setModel(model);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> model = new HashMap<>();
            model.put("message", "Error procesando la respuesta de la IA.");

            NotificationMessageRequest notificationMessage = new NotificationMessageRequest();
            notificationMessage.setType("twilio");
            notificationMessage.setTo(Collections.singletonList(userNumber));
            notificationMessage.setSubject("Mensaje De ChatBot");
            notificationMessage.setModel(model);
            //twilioService.sendWhatsAppMessage(userNumber, "Error procesando la respuesta de la IA.");
        }
    }


}
