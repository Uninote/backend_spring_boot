package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.uninote.backend.payload.BadgeNotificationPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class BadgeWebSocketController {

    private final SimpMessagingTemplate template;

    private static final Logger logger = LoggerFactory.getLogger(BadgeWebSocketController.class);

    @Autowired
    public BadgeWebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void sendBadgeNotification(Long userId, Long notificationId, Long badgeId) {
        String destination = "/topic/badges/" + userId;
        BadgeNotificationPayload payload = new BadgeNotificationPayload(notificationId, badgeId);
        this.template.convertAndSend(destination, payload);
    }
}
