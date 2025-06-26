package com.uninote.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.dto.QuestionnaireContentDTO;
import com.uninote.backend.dto.QuestionnaireDTO;
import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.QuestionnaireRepository;
import com.uninote.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class QuestionnaireWebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireWebSocketService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send questionnaire to a specific user via WebSocket
     */
    public void sendQuestionnaireToUser(Long questionnaireId, Long userId) {
        try {
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new RuntimeException("Questionnaire not found: " + questionnaireId));
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Create questionnaire DTO
            QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
            questionnaireDTO.setId(questionnaire.getId());
            questionnaireDTO.setName(questionnaire.getName());
            questionnaireDTO.setDescription(questionnaire.getDescription());
            questionnaireDTO.setStatus(questionnaire.getStatus());
            questionnaireDTO.setCreatedAt(questionnaire.getCreatedAt());
            questionnaireDTO.setTriggerTime(questionnaire.getTriggerTime());

            // Create content DTO
            QuestionnaireContentDTO contentDTO = new QuestionnaireContentDTO();
            contentDTO.setQuestionnaireId(questionnaire.getId().toString());
            contentDTO.setTitle(questionnaire.getName());
            contentDTO.setDescription(questionnaire.getDescription());
            contentDTO.setQuestionnaireType("SURVEY");
            contentDTO.setVersion("1.0");

            // Create payload
            QuestionnaireWebSocketPayload payload = new QuestionnaireWebSocketPayload();
            payload.setType("QUESTIONNAIRE");
            payload.setAction("NEW_QUESTIONNAIRE");
            payload.setTimestamp(System.currentTimeMillis());
            payload.setQuestionnaire(questionnaireDTO);
            payload.setContent(contentDTO);

            // Convert to JSON
            String jsonPayload = objectMapper.writeValueAsString(payload);

            // Send to user's topic using user ID
            String destination = "/topic/questionnaires/" + userId;
            messagingTemplate.convertAndSend(destination, jsonPayload);

            logger.info("Questionnaire {} sent to user {} at destination: {}", 
                questionnaireId, userId, destination);

        } catch (Exception e) {
            logger.error("Error sending questionnaire to user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send questionnaire", e);
        }
    }

    /**
     * Send questionnaire completion notification
     */
    public void sendCompletionNotification(Long userId, Long questionnaireId) {
        try {
            Map<String, Object> notification = Map.of(
                "type", "QUESTIONNAIRE_COMPLETION",
                "questionnaireId", questionnaireId,
                "userId", userId,
                "timestamp", System.currentTimeMillis(),
                "message", "Questionnaire completed successfully"
            );

            String destination = "/topic/questionnaires/" + userId;
            messagingTemplate.convertAndSend(destination, notification);

            logger.info("Completion notification sent to user {} for questionnaire {}", 
                userId, questionnaireId);

        } catch (Exception e) {
            logger.error("Error sending completion notification: {}", e.getMessage(), e);
        }
    }

    /**
     * WebSocket payload for questionnaire messages
     */
    public static class QuestionnaireWebSocketPayload {
        private String type;
        private String action;
        private long timestamp;
        private QuestionnaireDTO questionnaire;
        private QuestionnaireContentDTO content;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public QuestionnaireDTO getQuestionnaire() { return questionnaire; }
        public void setQuestionnaire(QuestionnaireDTO questionnaire) { this.questionnaire = questionnaire; }

        public QuestionnaireContentDTO getContent() { return content; }
        public void setContent(QuestionnaireContentDTO content) { this.content = content; }

        @Override
        public String toString() {
            return "QuestionnaireWebSocketPayload{" +
                "type='" + type + '\'' +
                ", action='" + action + '\'' +
                ", timestamp=" + timestamp +
                ", questionnaire=" + questionnaire +
                ", content=" + content +
                '}';
        }
    }
} 