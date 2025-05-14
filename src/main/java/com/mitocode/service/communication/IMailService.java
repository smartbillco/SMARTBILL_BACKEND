package com.mitocode.service.communication;

import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.util.ApiResponseUtil;

import java.util.Map;

public interface IMailService {
    ApiResponseUtil<Map<String, Object>> sendMail(NotificationMessageRequest sendMailDTO);
}
