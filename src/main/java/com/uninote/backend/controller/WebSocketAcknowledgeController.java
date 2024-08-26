package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.uninote.backend.entity.Badge;
import com.uninote.backend.entity.BadgeNotification;
import com.uninote.backend.payload.AcknowledgmentPayload;
import com.uninote.backend.repository.BadgeNotificationRepository;

@Controller
public class WebSocketAcknowledgeController {

    @Autowired
    private BadgeNotificationRepository badgeNotificationRepository;

    @MessageMapping("/acknowledge")
    public void handleAcknowledgment(@Payload AcknowledgmentPayload payload) {
        BadgeNotification notification = badgeNotificationRepository.findById(payload.getNotificationId())
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        notification.markAsDelivered();
        badgeNotificationRepository.save(notification);
    }
}


