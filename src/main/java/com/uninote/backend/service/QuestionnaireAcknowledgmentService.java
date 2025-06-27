package com.uninote.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.QuestionnaireAcknowledgment;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.QuestionnaireAcknowledgmentRepository;
import com.uninote.backend.repository.QuestionnaireRepository;
import com.uninote.backend.repository.UserRepository;

@Service
public class QuestionnaireAcknowledgmentService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireAcknowledgmentService.class);

    @Autowired
    private QuestionnaireAcknowledgmentRepository acknowledgmentRepository;

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Store a questionnaire acknowledgment
     */
    public QuestionnaireAcknowledgment storeAcknowledgment(Long questionnaireId, Long userId, 
                                                         String acknowledgmentType, Map<String, Object> additionalData) {
        try {
            logger.debug("Storing acknowledgment for questionnaire {} and user {} with type: {}", 
                questionnaireId, userId, acknowledgmentType);

            // Get questionnaire and user
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new RuntimeException("Questionnaire not found: " + questionnaireId));
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Create acknowledgment
            QuestionnaireAcknowledgment acknowledgment = new QuestionnaireAcknowledgment(questionnaire, user, acknowledgmentType);
            
            // Set additional data if provided
            if (additionalData != null && !additionalData.isEmpty()) {
                acknowledgment.setAcknowledgmentData(convertToJson(additionalData));
                
                // Extract common fields from additional data
                if (additionalData.containsKey("sessionId")) {
                    acknowledgment.setSessionId((String) additionalData.get("sessionId"));
                }
                if (additionalData.containsKey("clientInfo")) {
                    acknowledgment.setClientInfo((String) additionalData.get("clientInfo"));
                }
                if (additionalData.containsKey("ipAddress")) {
                    acknowledgment.setIpAddress((String) additionalData.get("ipAddress"));
                }
            }

            // Save acknowledgment
            QuestionnaireAcknowledgment savedAcknowledgment = acknowledgmentRepository.save(acknowledgment);
            
            logger.info("✅ Acknowledgment stored successfully: ID={}, Type={}, User={}, Questionnaire={}", 
                savedAcknowledgment.getId(), acknowledgmentType, userId, questionnaireId);

            return savedAcknowledgment;

        } catch (Exception e) {
            logger.error("Error storing questionnaire acknowledgment", e);
            throw new RuntimeException("Failed to store acknowledgment", e);
        }
    }

    /**
     * Store a simple acknowledgment without additional data
     */
    public QuestionnaireAcknowledgment storeAcknowledgment(Long questionnaireId, Long userId, String acknowledgmentType) {
        return storeAcknowledgment(questionnaireId, userId, acknowledgmentType, null);
    }

    /**
     * Get all acknowledgments for a user
     */
    public List<QuestionnaireAcknowledgment> getUserAcknowledgments(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return acknowledgmentRepository.findByUser(user);
    }

    /**
     * Get all acknowledgments for a questionnaire
     */
    public List<QuestionnaireAcknowledgment> getQuestionnaireAcknowledgments(Long questionnaireId) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
            .orElseThrow(() -> new RuntimeException("Questionnaire not found: " + questionnaireId));
        return acknowledgmentRepository.findByQuestionnaire(questionnaire);
    }

    /**
     * Get acknowledgments for a specific user and questionnaire
     */
    public List<QuestionnaireAcknowledgment> getUserQuestionnaireAcknowledgments(Long userId, Long questionnaireId) {
        return acknowledgmentRepository.findByQuestionnaire_IdAndUser_Id(questionnaireId, userId);
    }

    /**
     * Get latest acknowledgment for a user and questionnaire
     */
    public Optional<QuestionnaireAcknowledgment> getLatestAcknowledgment(Long userId, Long questionnaireId) {
        return acknowledgmentRepository.findFirstByQuestionnaire_IdAndUser_IdOrderByAcknowledgedAtDesc(questionnaireId, userId);
    }

    /**
     * Check if user has acknowledged a questionnaire with specific type
     */
    public boolean hasUserAcknowledged(Long userId, Long questionnaireId, String acknowledgmentType) {
        return acknowledgmentRepository.existsByQuestionnaire_IdAndUser_IdAndAcknowledgmentType(questionnaireId, userId, acknowledgmentType);
    }

    /**
     * Check if user has received a questionnaire
     */
    public boolean hasUserReceived(Long userId, Long questionnaireId) {
        return hasUserAcknowledged(userId, questionnaireId, "RECEIVED");
    }

    /**
     * Check if user has started a questionnaire
     */
    public boolean hasUserStarted(Long userId, Long questionnaireId) {
        return hasUserAcknowledged(userId, questionnaireId, "STARTED");
    }

    /**
     * Check if user has completed a questionnaire
     */
    public boolean hasUserCompleted(Long userId, Long questionnaireId) {
        return hasUserAcknowledged(userId, questionnaireId, "COMPLETED");
    }

    /**
     * Check if user has dismissed a questionnaire
     */
    public boolean hasUserDismissed(Long userId, Long questionnaireId) {
        return hasUserAcknowledged(userId, questionnaireId, "DISMISSED");
    }

    /**
     * Get acknowledgment statistics for a questionnaire
     */
    public Map<String, Long> getQuestionnaireAcknowledgmentStatistics(Long questionnaireId) {
        List<Object[]> statistics = acknowledgmentRepository.getAcknowledgmentStatisticsForQuestionnaire(questionnaireId);
        Map<String, Long> result = new java.util.HashMap<>();
        
        for (Object[] stat : statistics) {
            String type = (String) stat[0];
            Long count = (Long) stat[1];
            result.put(type, count);
        }
        
        return result;
    }

    /**
     * Get acknowledgment statistics for a user
     */
    public Map<String, Long> getUserAcknowledgmentStatistics(Long userId) {
        List<Object[]> statistics = acknowledgmentRepository.getAcknowledgmentStatisticsForUser(userId);
        Map<String, Long> result = new java.util.HashMap<>();
        
        for (Object[] stat : statistics) {
            String type = (String) stat[0];
            Long count = (Long) stat[1];
            result.put(type, count);
        }
        
        return result;
    }

    /**
     * Get users who have acknowledged a specific questionnaire
     */
    public List<User> getUsersWhoAcknowledgedQuestionnaire(Long questionnaireId) {
        return acknowledgmentRepository.findUsersWhoAcknowledgedQuestionnaire(questionnaireId);
    }

    /**
     * Get users who have acknowledged a specific questionnaire with specific type
     */
    public List<User> getUsersWhoAcknowledgedQuestionnaireWithType(Long questionnaireId, String acknowledgmentType) {
        return acknowledgmentRepository.findUsersWhoAcknowledgedQuestionnaireWithType(questionnaireId, acknowledgmentType);
    }

    /**
     * Get acknowledgments within a date range
     */
    public List<QuestionnaireAcknowledgment> getAcknowledgmentsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return acknowledgmentRepository.findByAcknowledgedAtBetween(startDate, endDate);
    }

    /**
     * Get acknowledgments by session ID
     */
    public List<QuestionnaireAcknowledgment> getAcknowledgmentsBySession(String sessionId) {
        return acknowledgmentRepository.findBySessionId(sessionId);
    }

    /**
     * Delete acknowledgment by ID
     */
    public void deleteAcknowledgment(Long acknowledgmentId) {
        acknowledgmentRepository.deleteById(acknowledgmentId);
        logger.info("Deleted acknowledgment with ID: {}", acknowledgmentId);
    }

    /**
     * Delete all acknowledgments for a user and questionnaire
     */
    public void deleteUserQuestionnaireAcknowledgments(Long userId, Long questionnaireId) {
        List<QuestionnaireAcknowledgment> acknowledgments = getUserQuestionnaireAcknowledgments(userId, questionnaireId);
        acknowledgmentRepository.deleteAll(acknowledgments);
        logger.info("Deleted {} acknowledgments for user {} and questionnaire {}", 
            acknowledgments.size(), userId, questionnaireId);
    }

    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            logger.warn("Failed to convert acknowledgment data to JSON", e);
            return "{}";
        }
    }

    /**
     * Parse JSON string to Map
     */
    public Map<String, Object> parseAcknowledgmentData(String jsonData) {
        try {
            if (jsonData == null || jsonData.trim().isEmpty()) {
                return new java.util.HashMap<>();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(jsonData, Map.class);
            return data;
        } catch (Exception e) {
            logger.warn("Failed to parse acknowledgment data JSON", e);
            return new java.util.HashMap<>();
        }
    }
} 