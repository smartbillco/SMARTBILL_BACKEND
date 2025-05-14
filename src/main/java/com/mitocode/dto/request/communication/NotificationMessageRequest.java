package com.mitocode.dto.request.communication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessageRequest {
    private String type;
    private String from;
    private List<String> to;
    private String subject;
    private Map<String, Object> model;
}
