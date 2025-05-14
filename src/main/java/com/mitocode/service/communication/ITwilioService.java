package com.mitocode.service.communication;

import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.dto.response.NotificationMessageResponse;
import com.mitocode.util.ApiResponseUtil;

public interface ITwilioService {

    ApiResponseUtil<NotificationMessageResponse> sendMessage(NotificationMessageRequest notificationMessageRequestDTO);

    ApiResponseUtil<NotificationMessageResponse> sendWhatsAppMessage(NotificationMessageRequest request);
}
