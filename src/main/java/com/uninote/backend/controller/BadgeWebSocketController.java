package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
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

    public void sendBadgeNotification(String userId, Long badge) {
        logger.debug("Sending badge notification to userId: {} with badgeId: {}", userId, badge);
        String destination = "/topic/badges/" + userId;
        this.template.convertAndSend(destination, badge);
        logger.debug("Notification sent to destination: {}", destination);
    }
}
