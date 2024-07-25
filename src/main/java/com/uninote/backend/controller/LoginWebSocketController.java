package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LoginWebSocketController {

    private final SimpMessagingTemplate template;

    @Autowired
    public LoginWebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void sendLoginNotification(String userId, String message) {
        String destination = "/topic/login/" + userId;
        this.template.convertAndSend(destination, message);
    }
}
