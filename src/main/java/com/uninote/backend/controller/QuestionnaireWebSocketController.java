package com.uninote.backend.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.uninote.backend.dto.QuestionnaireContentDTO;
import com.uninote.backend.dto.QuestionnaireDTO;
import com.uninote.backend.service.QuestionnaireService;

@Controller
public class QuestionnaireWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireWebSocketController.class);
    
    private final SimpMessagingTemplate template;
    private final QuestionnaireService questionnaireService;

    @Autowired
    public QuestionnaireWebSocketController(SimpMessagingTemplate template, QuestionnaireService questionnaireService) {
        this.template = template;
        this.questionnaireService = questionnaireService;
        logger.info("QuestionnaireWebSocketController initialized");
    }

    /**
     * Send a questionnaire to a specific user
     */
    public void sendQuestionnaireToUser(String uuid, QuestionnaireDTO questionnaire, QuestionnaireContentDTO content) {
        String destination = "/topic/questionnaires/" + uuid;
        
        // Create payload with null checks
        Map<String, Object> payload = Map.of(
            "type", "QUESTIONNAIRE",
            "action", "NEW_QUESTIONNAIRE",
            "questionnaire", questionnaire != null ? questionnaire : new QuestionnaireDTO(),
            "content", content != null ? content : new QuestionnaireContentDTO(),
            "timestamp", System.currentTimeMillis()
        );
        
        logger.debug("Attempting to send questionnaire to user {} at destination: {}", uuid, destination);
        logger.debug("Payload: {}", payload);
        
        try {
            this.template.convertAndSend(destination, payload);
            logger.info("Successfully sent questionnaire {} to user {} at destination {}", 
                questionnaire != null ? questionnaire.getId() : "null", uuid, destination);
        } catch (Exception e) {
            logger.error("Failed to send questionnaire to user {} at destination {}: {}", uuid, destination, e.getMessage(), e);
        }
    }

    /**
     * Send a questionnaire to multiple users
     */
    public void sendQuestionnaireToUsers(List<String> uuids, QuestionnaireDTO questionnaire, QuestionnaireContentDTO content) {
        logger.info("Sending questionnaire {} to {} users: {}", questionnaire.getId(), uuids.size(), uuids);
        for (String uuid : uuids) {
            sendQuestionnaireToUser(uuid, questionnaire, content);
        }
    }

    /**
     * Send a questionnaire to all users (broadcast)
     */
    public void broadcastQuestionnaire(QuestionnaireDTO questionnaire, QuestionnaireContentDTO content) {
        String destination = "/topic/questionnaires/broadcast";
        Map<String, Object> payload = Map.of(
            "type", "QUESTIONNAIRE",
            "action", "BROADCAST_QUESTIONNAIRE",
            "questionnaire", questionnaire,
            "content", content,
            "timestamp", System.currentTimeMillis()
        );
        
        logger.debug("Broadcasting questionnaire {} to all users at destination: {}", questionnaire.getId(), destination);
        logger.debug("Broadcast payload: {}", payload);
        
        try {
            this.template.convertAndSend(destination, payload);
            logger.info("Successfully broadcasted questionnaire {} to all users", questionnaire.getId());
        } catch (Exception e) {
            logger.error("Failed to broadcast questionnaire {}: {}", questionnaire.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send questionnaire reminder to a user
     */
    public void sendQuestionnaireReminder(String uuid, QuestionnaireDTO questionnaire) {
        String destination = "/topic/questionnaire/" + uuid;
        Map<String, Object> payload = Map.of(
            "type", "QUESTIONNAIRE",
            "action", "REMINDER",
            "questionnaire", questionnaire,
            "message", "You have a pending questionnaire to complete",
            "timestamp", System.currentTimeMillis()
        );
        
        logger.debug("Sending reminder to user {} for questionnaire {} at destination: {}", uuid, questionnaire.getId(), destination);
        logger.debug("Reminder payload: {}", payload);
        
        try {
            this.template.convertAndSend(destination, payload);
            logger.info("Successfully sent questionnaire reminder to user {}", uuid);
        } catch (Exception e) {
            logger.error("Failed to send reminder to user {}: {}", uuid, e.getMessage(), e);
        }
    }

    /**
     * Send questionnaire completion notification
     */
    public void sendQuestionnaireCompletionNotification(String uuid, Long questionnaireId, String message) {
        String destination = "/topic/questionnaire/" + uuid;
        Map<String, Object> payload = Map.of(
            "type", "QUESTIONNAIRE",
            "action", "COMPLETION_NOTIFICATION",
            "questionnaireId", questionnaireId,
            "message", message,
            "timestamp", System.currentTimeMillis()
        );
        
        logger.debug("Sending completion notification to user {} for questionnaire {} at destination: {}", uuid, questionnaireId, destination);
        logger.debug("Completion payload: {}", payload);
        
        try {
            this.template.convertAndSend(destination, payload);
            logger.info("Successfully sent completion notification to user {} for questionnaire {}", uuid, questionnaireId);
        } catch (Exception e) {
            logger.error("Failed to send completion notification to user {}: {}", uuid, e.getMessage(), e);
        }
    }

    /**
     * Send questionnaire expiration notification
     */
    public void sendQuestionnaireExpirationNotification(String uuid, QuestionnaireDTO questionnaire) {
        String destination = "/topic/questionnaire/" + uuid;
        Map<String, Object> payload = Map.of(
            "type", "QUESTIONNAIRE",
            "action", "EXPIRATION_NOTIFICATION",
            "questionnaire", questionnaire,
            "message", "A questionnaire has expired",
            "timestamp", System.currentTimeMillis()
        );
        
        logger.debug("Sending expiration notification to user {} for questionnaire {} at destination: {}", uuid, questionnaire.getId(), destination);
        logger.debug("Expiration payload: {}", payload);
        
        try {
            this.template.convertAndSend(destination, payload);
            logger.info("Successfully sent expiration notification to user {} for questionnaire {}", uuid, questionnaire.getId());
        } catch (Exception e) {
            logger.error("Failed to send expiration notification to user {}: {}", uuid, e.getMessage(), e);
        }
    }

    /**
     * Send questionnaire status update
     */
    public void sendQuestionnaireStatusUpdate(String uuid, Long questionnaireId, String status, String message) {
        String destination = "/topic/questionnaire/" + uuid;
        Map<String, Object> payload = Map.of(
            "type", "QUESTIONNAIRE",
            "action", "STATUS_UPDATE",
            "questionnaireId", questionnaireId,
            "status", status,
            "message", message,
            "timestamp", System.currentTimeMillis()
        );
        
        logger.debug("Sending status update to user {} for questionnaire {} at destination: {}", uuid, questionnaireId, destination);
        logger.debug("Status update payload: {}", payload);
        
        try {
            this.template.convertAndSend(destination, payload);
            logger.info("Successfully sent status update to user {} for questionnaire {}: {}", uuid, questionnaireId, status);
        } catch (Exception e) {
            logger.error("Failed to send status update to user {}: {}", uuid, e.getMessage(), e);
        }
    }

    /**
     * Send questionnaire analytics to creator
     */
    public void sendQuestionnaireAnalytics(String creatorUuid, Long questionnaireId, Map<String, Object> analytics) {
        String destination = "/topic/questionnaires/analytics/" + creatorUuid;
        Map<String, Object> payload = Map.of(
            "type", "QUESTIONNAIRE",
            "action", "ANALYTICS_UPDATE",
            "questionnaireId", questionnaireId,
            "analytics", analytics,
            "timestamp", System.currentTimeMillis()
        );
        
        logger.debug("Sending analytics update to creator {} for questionnaire {} at destination: {}", creatorUuid, questionnaireId, destination);
        logger.debug("Analytics payload: {}", payload);
        
        try {
            this.template.convertAndSend(destination, payload);
            logger.info("Successfully sent analytics update to creator {} for questionnaire {}", creatorUuid, questionnaireId);
        } catch (Exception e) {
            logger.error("Failed to send analytics update to creator {}: {}", creatorUuid, e.getMessage(), e);
        }
    }
} 