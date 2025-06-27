package com.uninote.backend.controller;

import java.util.Map;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.dto.QuestionnaireDTO;
import com.uninote.backend.service.QuestionnaireService;
import com.uninote.backend.service.QuestionnaireResponseStorageService;
import com.uninote.backend.service.QuestionnaireAcknowledgmentService;

@Controller
@RequestMapping("/questionnaire")
public class QuestionnaireResponseController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireResponseController.class);

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private QuestionnaireWebSocketController webSocketController;

    @Autowired
    private QuestionnaireResponseStorageService questionnaireResponseStorageService;

    @Autowired
    private QuestionnaireAcknowledgmentService acknowledgmentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * REST HTTP endpoint for questionnaire response submission
     */
    @PostMapping("/response")
    @ResponseBody
    public Map<String, Object> submitQuestionnaireResponse(@RequestBody Map<String, Object> payload) {
        try {
            logger.info("Received questionnaire response via HTTP: {}", payload);

            Long questionnaireId = Long.valueOf(payload.get("questionnaireId").toString());
            Long userId = Long.valueOf(payload.get("userId").toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> responses = (Map<String, Object>) payload.get("responses");

            // Validate the response
            if (!validateResponse(questionnaireId, userId, responses)) {
                return Map.of(
                    "success", false,
                    "error", "Invalid response data"
                );
            }

            // Store the response in database and Firebase
            String responseId = questionnaireResponseStorageService.storeQuestionnaireResponse(
                questionnaireId, userId, responses
            );
            
            logger.info("Questionnaire response stored successfully with Response ID: {}", responseId);

            // Store completion acknowledgment
            try {
                acknowledgmentService.storeAcknowledgment(questionnaireId, userId, "COMPLETED", payload);
                logger.info("Completion acknowledgment stored for user: {}", userId);
            } catch (Exception e) {
                logger.warn("Could not store completion acknowledgment for user {}: {}", userId, e.getMessage());
            }

            // Send completion notification via WebSocket (if user is online)
            try {
                webSocketController.sendQuestionnaireCompletionNotification(
                    userId.toString(), 
                    questionnaireId, 
                    "Thank you for completing the questionnaire!"
                );
            } catch (Exception e) {
                logger.warn("Could not send WebSocket notification to user {}: {}", userId, e.getMessage());
            }

            logger.info("Questionnaire response processed successfully for user: {}", userId);

            // Return success response
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Questionnaire response submitted successfully",
                "responseId", responseId,
                "timestamp", LocalDateTime.now().toString()
            );

            return response;

        } catch (Exception e) {
            logger.error("Error processing questionnaire response", e);
            return Map.of(
                "success", false,
                "error", "Error processing response: " + e.getMessage()
            );
        }
    }

    /**
     * Handle questionnaire response submission via WebSocket
     */
    @MessageMapping("/questionnaire/response")
    public void handleQuestionnaireResponse(@Payload Map<String, Object> payload) {
        try {
            logger.info("Received questionnaire response via WebSocket: {}", payload);

            Long questionnaireId = Long.valueOf(payload.get("questionnaireId").toString());
            Long userId = Long.valueOf(payload.get("userId").toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> responses = (Map<String, Object>) payload.get("responses");

            // Validate the response
            if (!validateResponse(questionnaireId, userId, responses)) {
                sendErrorResponse(userId, "Invalid response data");
                return;
            }

            // Store the response in database and Firebase
            String responseId = questionnaireResponseStorageService.storeQuestionnaireResponse(
                questionnaireId, userId, responses
            );
            
            logger.info("Questionnaire response stored successfully with Response ID: {}", responseId);

            // Store completion acknowledgment
            try {
                acknowledgmentService.storeAcknowledgment(questionnaireId, userId, "COMPLETED", payload);
                logger.info("Completion acknowledgment stored for user: {}", userId);
            } catch (Exception e) {
                logger.warn("Could not store completion acknowledgment for user {}: {}", userId, e.getMessage());
            }

            // Send completion notification
            webSocketController.sendQuestionnaireCompletionNotification(
                userId.toString(), 
                questionnaireId, 
                "Thank you for completing the questionnaire!"
            );

            logger.info("Questionnaire response processed successfully for user: {}", userId);

        } catch (Exception e) {
            logger.error("Error processing questionnaire response", e);
            // Try to send error response if we have userId
            try {
                Long userId = Long.valueOf(payload.get("userId").toString());
                sendErrorResponse(userId, "Error processing response");
            } catch (Exception ex) {
                logger.error("Could not send error response", ex);
            }
        }
    }

    /**
     * Handle questionnaire acknowledgment
     */
    @MessageMapping("/questionnaire/acknowledge")
    public void handleQuestionnaireAcknowledgment(@Payload Map<String, Object> payload) {
        try {
            logger.info("Received questionnaire acknowledgment: {}", payload);

            Long questionnaireId = Long.valueOf(payload.get("questionnaireId").toString());
            Long userId = Long.valueOf(payload.get("userId").toString());
            String acknowledgmentType = (String) payload.get("type"); // "RECEIVED", "STARTED", "COMPLETED", "DISMISSED"

            // Validate acknowledgment type
            if (!isValidAcknowledgmentType(acknowledgmentType)) {
                logger.warn("Invalid acknowledgment type: {}", acknowledgmentType);
                sendErrorResponse(userId, "Invalid acknowledgment type");
                return;
            }

            // Store the acknowledgment in database
            try {
                acknowledgmentService.storeAcknowledgment(questionnaireId, userId, acknowledgmentType, payload);
                logger.info("✅ Acknowledgment stored successfully: Type={}, User={}, Questionnaire={}", 
                    acknowledgmentType, userId, questionnaireId);
            } catch (Exception e) {
                logger.error("Error storing acknowledgment", e);
                sendErrorResponse(userId, "Error storing acknowledgment");
                return;
            }

            // Send acknowledgment confirmation
            webSocketController.sendQuestionnaireStatusUpdate(
                userId.toString(), 
                questionnaireId, 
                "ACKNOWLEDGED", 
                "Questionnaire acknowledgment received: " + acknowledgmentType
            );

            logger.info("Questionnaire acknowledgment processed successfully for user: {}", userId);

        } catch (Exception e) {
            logger.error("Error processing questionnaire acknowledgment", e);
        }
    }

    /**
     * Handle questionnaire progress update
     */
    @MessageMapping("/questionnaire/progress")
    public void handleQuestionnaireProgress(@Payload Map<String, Object> payload) {
        try {
            logger.info("Received questionnaire progress update: {}", payload);

            Long questionnaireId = Long.valueOf(payload.get("questionnaireId").toString());
            Long userId = Long.valueOf(payload.get("userId").toString());
            Integer currentQuestion = (Integer) payload.get("currentQuestion");
            Integer totalQuestions = (Integer) payload.get("totalQuestions");
            Double progressPercentage = (Double) payload.get("progressPercentage");

            // Store progress acknowledgment
            try {
                acknowledgmentService.storeAcknowledgment(questionnaireId, userId, "PROGRESS_UPDATE", payload);
                logger.info("Progress acknowledgment stored for user: {}", userId);
            } catch (Exception e) {
                logger.warn("Could not store progress acknowledgment for user {}: {}", userId, e.getMessage());
            }

            // Log progress
            logger.info("User {} progress on questionnaire {}: {}/{} ({}%)", 
                userId, questionnaireId, currentQuestion, totalQuestions, progressPercentage);

            // Send progress confirmation
            webSocketController.sendQuestionnaireStatusUpdate(
                userId.toString(), 
                questionnaireId, 
                "PROGRESS_UPDATE", 
                String.format("Progress: %.1f%%", progressPercentage)
            );

        } catch (Exception e) {
            logger.error("Error processing questionnaire progress", e);
        }
    }

    /**
     * Handle questionnaire feedback
     */
    @MessageMapping("/questionnaire/feedback")
    public void handleQuestionnaireFeedback(@Payload Map<String, Object> payload) {
        try {
            logger.info("Received questionnaire feedback: {}", payload);

            Long questionnaireId = Long.valueOf(payload.get("questionnaireId").toString());
            Long userId = Long.valueOf(payload.get("userId").toString());
            String feedbackType = (String) payload.get("feedbackType"); // "BUG_REPORT", "SUGGESTION", "COMPLAINT"
            String feedbackText = (String) payload.get("feedbackText");
            Integer rating = (Integer) payload.get("rating");

            // Store feedback acknowledgment
            try {
                acknowledgmentService.storeAcknowledgment(questionnaireId, userId, "FEEDBACK", payload);
                logger.info("Feedback acknowledgment stored for user: {}", userId);
            } catch (Exception e) {
                logger.warn("Could not store feedback acknowledgment for user {}: {}", userId, e.getMessage());
            }

            // Store feedback (you would implement this in a separate service)
            // storeQuestionnaireFeedback(questionnaireId, userId, feedbackType, feedbackText, rating);

            // Send feedback acknowledgment
            webSocketController.sendQuestionnaireStatusUpdate(
                userId.toString(), 
                questionnaireId, 
                "FEEDBACK_RECEIVED", 
                "Thank you for your feedback!"
            );

            logger.info("Questionnaire feedback processed for user: {}", userId);

        } catch (Exception e) {
            logger.error("Error processing questionnaire feedback", e);
        }
    }

    /**
     * Validate questionnaire response
     */
    private boolean validateResponse(Long questionnaireId, Long userId, Map<String, Object> responses) {
        try {
            // Basic validation - you would implement more comprehensive validation
            if (questionnaireId == null || userId == null || responses == null) {
                return false;
            }

            // Check if questionnaire exists and is active
            QuestionnaireDTO questionnaire = questionnaireService.getQuestionnaireById(questionnaireId);
            if (questionnaire == null || questionnaire.getStatus() != com.uninote.backend.entity.QuestionnaireStatus.ACTIVE) {
                return false;
            }

            // Validate responses structure (basic check)
            if (responses.isEmpty()) {
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("Error validating questionnaire response", e);
            return false;
        }
    }

    /**
     * Validate acknowledgment type
     */
    private boolean isValidAcknowledgmentType(String acknowledgmentType) {
        if (acknowledgmentType == null) {
            return false;
        }
        
        return acknowledgmentType.equals("RECEIVED") || 
               acknowledgmentType.equals("STARTED") || 
               acknowledgmentType.equals("COMPLETED") || 
               acknowledgmentType.equals("DISMISSED") ||
               acknowledgmentType.equals("PROGRESS_UPDATE");
    }

    /**
     * Send error response to user
     */
    private void sendErrorResponse(Long userId, String errorMessage) {
        try {
            webSocketController.sendQuestionnaireStatusUpdate(
                userId.toString(), 
                null, 
                "ERROR", 
                errorMessage
            );
        } catch (Exception e) {
            logger.error("Error sending error response", e);
        }
    }
} 