package com.uninote.backend.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.repository.QuestionnaireRepository;

@Service
public class QuestionnaireWebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireWebSocketService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    /**
     * Send questionnaire to a specific user via WebSocket
     */
    public void sendQuestionnaireToUser(String uuid, Questionnaire questionnaire) {
        String destination = "/topic/questionnaires/" + uuid;
        
        QuestionnairePayload payload = new QuestionnairePayload();
        payload.setQuestionnaireId(questionnaire.getId());
        payload.setName(questionnaire.getName());
        payload.setDescription(questionnaire.getDescription());
        payload.setFirebasePath(questionnaire.getFirebasePath());
        payload.setTriggerTime(questionnaire.getTriggerTime());
        
        // Enhanced logging for user 330
        boolean isTargetUser = uuid != null && uuid.contains("330") || questionnaire.getId() == 1L;
        
        if (isTargetUser) {
            logger.debug("=== SENDING QUESTIONNAIRE TO TARGET USER 330 ===");
            logger.debug("UUID: {}", uuid);
            logger.debug("Destination: {}", destination);
            logger.debug("Questionnaire ID: {}", questionnaire.getId());
            logger.debug("Questionnaire Name: {}", questionnaire.getName());
            logger.debug("Firebase Path: {}", questionnaire.getFirebasePath());
            logger.debug("Payload: {}", payload);
        } else {
            logger.debug("Sending questionnaire to user {} at destination: {}", uuid, destination);
            logger.debug("Questionnaire payload: {}", payload);
        }
        
        try {
            messagingTemplate.convertAndSend(destination, payload);
            if (isTargetUser) {
                logger.debug("=== SUCCESSFULLY SENT QUESTIONNAIRE TO TARGET USER 330 ===");
                logger.debug("Message sent to destination: {}", destination);
            } else {
                logger.debug("Successfully sent questionnaire {} to user {} at destination {}", 
                    questionnaire.getId(), uuid, destination);
            }
        } catch (Exception e) {
            if (isTargetUser) {
                logger.error("=== FAILED TO SEND QUESTIONNAIRE TO TARGET USER 330 ===");
                logger.error("Error details: {}", e.getMessage(), e);
            } else {
                logger.error("Failed to send questionnaire {} to user {} at destination {}: {}", 
                    questionnaire.getId(), uuid, destination, e.getMessage(), e);
            }
        }
    }

    /**
     * Send questionnaire to a specific user by questionnaire ID
     */
    public void sendQuestionnaireToUser(String uuid, Long questionnaireId) {
        logger.debug("Sending questionnaire {} to user {}", questionnaireId, uuid);
        
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(questionnaireId);
        if (questionnaireOpt.isPresent()) {
            sendQuestionnaireToUser(uuid, questionnaireOpt.get());
        } else {
            logger.warn("Questionnaire not found with ID: {}", questionnaireId);
        }
    }

    /**
     * Send questionnaire to multiple users
     */
    public void sendQuestionnaireToUsers(java.util.List<String> uuids, Questionnaire questionnaire) {
        logger.debug("Sending questionnaire {} to {} users", questionnaire.getId(), uuids.size());
        logger.debug("User UUIDs: {}", uuids);
        
        for (String uuid : uuids) {
            sendQuestionnaireToUser(uuid, questionnaire);
        }
        
        logger.debug("Completed sending questionnaire {} to {} users", questionnaire.getId(), uuids.size());
    }

    /**
     * Send questionnaire to multiple users by questionnaire ID
     */
    public void sendQuestionnaireToUsers(java.util.List<String> uuids, Long questionnaireId) {
        logger.debug("Sending questionnaire {} to {} users", questionnaireId, uuids.size());
        
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(questionnaireId);
        if (questionnaireOpt.isPresent()) {
            sendQuestionnaireToUsers(uuids, questionnaireOpt.get());
        } else {
            logger.warn("Questionnaire not found with ID: {}", questionnaireId);
        }
    }

    /**
     * Send questionnaire reminder to a user
     */
    public void sendQuestionnaireReminder(String uuid, Long questionnaireId) {
        String destination = "/topic/questionnaires/" + uuid;
        
        QuestionnaireReminderPayload payload = new QuestionnaireReminderPayload();
        payload.setQuestionnaireId(questionnaireId);
        payload.setMessage("You have a pending questionnaire to complete");
        payload.setReminderType("QUESTIONNAIRE_REMINDER");
        
        logger.debug("Sending questionnaire reminder to user {} for questionnaire {} at destination: {}", 
            uuid, questionnaireId, destination);
        logger.debug("Reminder payload: {}", payload);
        
        try {
            messagingTemplate.convertAndSend(destination, payload);
            logger.debug("Successfully sent questionnaire reminder to user {} for questionnaire {}", 
                uuid, questionnaireId);
        } catch (Exception e) {
            logger.error("Failed to send questionnaire reminder to user {} for questionnaire {}: {}", 
                uuid, questionnaireId, e.getMessage(), e);
        }
    }

    public static class QuestionnairePayload {
        private Long questionnaireId;
        private String name;
        private String description;
        private String firebasePath;
        private java.time.LocalDateTime triggerTime;

        public Long getQuestionnaireId() { return questionnaireId; }
        public void setQuestionnaireId(Long questionnaireId) { this.questionnaireId = questionnaireId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getFirebasePath() { return firebasePath; }
        public void setFirebasePath(String firebasePath) { this.firebasePath = firebasePath; }
        
        public java.time.LocalDateTime getTriggerTime() { return triggerTime; }
        public void setTriggerTime(java.time.LocalDateTime triggerTime) { this.triggerTime = triggerTime; }

        @Override
        public String toString() {
            return "QuestionnairePayload{" +
                    "questionnaireId=" + questionnaireId +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", firebasePath='" + firebasePath + '\'' +
                    ", triggerTime=" + triggerTime +
                    '}';
        }
    }

    public static class QuestionnaireReminderPayload {
        private Long questionnaireId;
        private String message;
        private String reminderType;

        public Long getQuestionnaireId() { return questionnaireId; }
        public void setQuestionnaireId(Long questionnaireId) { this.questionnaireId = questionnaireId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getReminderType() { return reminderType; }
        public void setReminderType(String reminderType) { this.reminderType = reminderType; }

        @Override
        public String toString() {
            return "QuestionnaireReminderPayload{" +
                    "questionnaireId=" + questionnaireId +
                    ", message='" + message + '\'' +
                    ", reminderType='" + reminderType + '\'' +
                    '}';
        }
    }
} 