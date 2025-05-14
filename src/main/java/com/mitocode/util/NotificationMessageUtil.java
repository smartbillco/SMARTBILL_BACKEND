package com.mitocode.util;

import com.mitocode.dto.request.communication.NotificationMessageRequest;

import java.util.Collections;
import java.util.Map;

public class NotificationMessageUtil {

    private static final String DEFAULT_SENDER = "caam.ingenierias@gmail.com";

    private NotificationMessageUtil() {
    }

    public static NotificationMessageRequest createEmailNotification(String recipient, String subject, Map<String, Object> model) {
        return createNotification("EMAIL", recipient, subject, model);
    }

    public static NotificationMessageRequest createSmsNotification(String recipient, String subject, Map<String, Object> model) {
        return createNotification("TWILIO", recipient, subject, model);
    }

    private static NotificationMessageRequest createNotification(String type, String recipient, String subject, Map<String, Object> model) {
        NotificationMessageRequest notification = new NotificationMessageRequest();
        notification.setType(type);
        notification.setFrom(DEFAULT_SENDER);
        notification.setTo(Collections.singletonList(recipient));
        notification.setSubject(subject);
        notification.setModel(model);
        return notification;
    }
}
