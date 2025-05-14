package com.mitocode.model.communication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private Long id;
    private String sender; // Puede ser 'user' o 'bot'
    private String text;
    private String time;
    private String userName;
}