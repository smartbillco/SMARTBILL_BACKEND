package com.mitocode.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessageResponse {
    private String type;
    private String from;
    private String to;
    private String subject;
    @JsonIgnore  // Oculta este campo en la respuesta JSON
    private Map<String, Object> model;
}
