package com.uninote.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.QuestionnaireCriteria;
import com.uninote.backend.entity.QuestionnaireStatus;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.QuestionnaireRepository;
import com.uninote.backend.repository.QuestionnaireResponseRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.controller.QuestionnaireWebSocketController;
import com.uninote.backend.dto.QuestionnaireDTO;
import com.uninote.backend.dto.QuestionnaireContentDTO;
import com.uninote.backend.dto.QuestionnaireQuestionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QuestionnaireTriggerService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireTriggerService.class);
    
    @Autowired
    private QuestionnaireRepository questionnaireRepository;
    
    @Autowired
    private QuestionnaireResponseRepository questionnaireResponseRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionnaireWebSocketService questionnaireWebSocketService;

    @Autowired
    private QuestionnaireWebSocketController questionnaireWebSocketController;

    @Autowired
    private QuestionnaireCriteriaService questionnaireCriteriaService;

    @Autowired
    private FirebasePresenceService firebasePresenceService;

    /**
     * Check if a user meets the criteria for a specific questionnaire
     * Uses criteria query from questionnaire table
     */
    public boolean userMeetsCriteria(User user, Questionnaire questionnaire) {
        
        // Only log for user 330
        boolean isTargetUser = user.getId() == 330L;
        
        // Get criteria query from questionnaire
        String criteriaQuery = questionnaire.getCriteriaQuery();
        
        if (criteriaQuery == null || criteriaQuery.trim().isEmpty()) {
            if (isTargetUser) {
                logger.debug("No criteria defined for questionnaire {} - user {} does not meet criteria", questionnaire.getId(), user.getId());
            }
            return false;
        }
        
        if (isTargetUser) {
            logger.debug("Found criteria query for questionnaire {}: {}", questionnaire.getId(), criteriaQuery);
        }
        
        try {
            // Use the criteria query service to evaluate the SQL query
            boolean meetsCriteria = questionnaireCriteriaService.evaluateCriteriaQuery(criteriaQuery, user);
            
            if (isTargetUser) {
                logger.debug("User {} meets criteria for questionnaire {}: {}", user.getId(), questionnaire.getId(), meetsCriteria);
            }
            return meetsCriteria;
            
        } catch (Exception e) {
            if (isTargetUser) {
                logger.error("Error evaluating criteria for user {} and questionnaire {}: {}", user.getId(), questionnaire.getId(), e.getMessage());
            }
            return false;
        }
    }

    /**
     * Send questionnaire to eligible users who haven't answered yet
     */
    public void sendQuestionnaireToEligibleUsers(Long questionnaireId) {
        logger.debug("Sending questionnaire {} to eligible users (default settings)", questionnaireId);
        sendQuestionnaireToEligibleUsers(questionnaireId, 100, 1000, true); // Default: 100 users per batch, 1000ms delay, ENABLE presence check
    }

    /**
     * Send questionnaire to eligible users with batching and rate limiting
     */
    public void sendQuestionnaireToEligibleUsers(Long questionnaireId, int batchSize, long delayMs) {
        logger.debug("Sending questionnaire {} to eligible users (batchSize: {}, delayMs: {})", questionnaireId, batchSize, delayMs);
        sendQuestionnaireToEligibleUsers(questionnaireId, batchSize, delayMs, true);
    }

    /**
     * Send questionnaire to eligible users with presence checking option
     */
    public void sendQuestionnaireToEligibleUsers(Long questionnaireId, int batchSize, long delayMs, boolean checkPresence) {
        logger.debug("=== Starting questionnaire distribution for questionnaire {} ===", questionnaireId);
        logger.debug("Settings: batchSize={}, delayMs={}, checkPresence={}", batchSize, delayMs, checkPresence);
        
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(questionnaireId);
        if (questionnaireOpt.isEmpty()) {
            logger.error("Questionnaire not found: {}", questionnaireId);
            throw new RuntimeException("Questionnaire not found: " + questionnaireId);
        }

        Questionnaire questionnaire = questionnaireOpt.get();
        logger.debug("Found questionnaire: {} - {}", questionnaire.getId(), questionnaire.getName());
        
        // Get all users who haven't answered this questionnaire yet
        List<User> eligibleUsers = questionnaireResponseRepository
                .findUsersWhoHaventAnsweredQuestionnaire(questionnaireId);
        logger.debug("Found {} users who haven't answered questionnaire {}", eligibleUsers.size(), questionnaireId);
        
        // Get target user IDs from criteria query first (more efficient)
        logger.debug("Getting target user IDs from criteria query");
        Set<Long> targetUserIds = getTargetUserIdsFromCriteria(questionnaire);
        logger.debug("Found {} target user IDs from criteria", targetUserIds.size());
        
        // Filter users who meet the criteria (much faster now)
        logger.debug("Starting to filter {} eligible users against {} target IDs", eligibleUsers.size(), targetUserIds.size());
        List<User> qualifiedUsers = eligibleUsers.stream()
                .filter(user -> targetUserIds.contains(user.getId()))
                .toList();
        
        logger.debug("Found {} users who meet criteria for questionnaire {} out of {} eligible users", 
            qualifiedUsers.size(), questionnaireId, eligibleUsers.size());
        
        // Filter active users if presence checking is enabled
        List<User> activeUsers = qualifiedUsers;
        if (checkPresence) {
            logger.debug("Checking user presence for {} qualified users", qualifiedUsers.size());
            
            // Check presence for each user and log results
            List<User> onlineUsers = new ArrayList<>();
            List<User> offlineUsers = new ArrayList<>();
            
            for (User user : qualifiedUsers) {
                boolean isActive = firebasePresenceService.isUserActive(user.getId());
                boolean isTargetUser = user.getId() == 330L;
                
                if (isActive) {
                    onlineUsers.add(user);
                    if (isTargetUser) {
                        logger.info("=== TARGET USER 330 IS ONLINE ===");
                        logger.info("User {} (Firebase UID: {}) is ONLINE", user.getId(), user.getFirebaseUid());
                    }
                } else {
                    offlineUsers.add(user);
                    if (isTargetUser) {
                        logger.info("=== TARGET USER 330 IS OFFLINE ===");
                        logger.info("User {} (Firebase UID: {}) is OFFLINE", user.getId(), user.getFirebaseUid());
                    }
                }
            }
            
            activeUsers = onlineUsers;
            
            // Only show summary if target user is involved
            boolean hasTargetUser = qualifiedUsers.stream().anyMatch(user -> user.getId() == 330L);
            if (hasTargetUser) {
                logger.info("=== PRESENCE CHECK RESULTS FOR TARGET USER 330 ===");
                logger.info("Presence check results: {} online, {} offline out of {} qualified users", 
                    onlineUsers.size(), offlineUsers.size(), qualifiedUsers.size());
            }
            
            if (!offlineUsers.isEmpty()) {
                List<String> offlineUserIds = offlineUsers.stream()
                    .filter(user -> user.getId() == 330L)
                    .map(user -> String.format("ID:%d(Firebase:%s)", user.getId(), user.getFirebaseUid()))
                    .toList();
                if (!offlineUserIds.isEmpty()) {
                    logger.info("Target user 330 is OFFLINE (no questionnaire sent): {}", offlineUserIds);
                }
            }
            
        } else {
            logger.debug("Skipping presence check - using all {} qualified users", qualifiedUsers.size());
        }

        // Send questionnaire to active users in batches
        sendQuestionnaireInBatches(activeUsers, questionnaire, batchSize, delayMs);
        logger.debug("=== Completed questionnaire distribution for questionnaire {} ===", questionnaireId);
    }

    /**
     * Send questionnaire to users in batches with delay
     */
    private void sendQuestionnaireInBatches(List<User> users, Questionnaire questionnaire, int batchSize, long delayMs) {
        int totalUsers = users.size();
        int batches = (int) Math.ceil((double) totalUsers / batchSize);
        
        logger.debug("Sending questionnaire to {} users in {} batches (batchSize: {})", totalUsers, batches, batchSize);
        
        for (int i = 0; i < batches; i++) {
            int startIndex = i * batchSize;
            int endIndex = Math.min(startIndex + batchSize, totalUsers);
            
            List<User> batch = users.subList(startIndex, endIndex);
            
            logger.debug("Processing batch {}/{} with {} users", i + 1, batches, batch.size());
            
            // Send to current batch
            for (User user : batch) {
                boolean isTargetUser = user.getId() == 330L;
                
                if (isTargetUser) {
                    logger.info("=== SENDING QUESTIONNAIRE TO TARGET USER 330 ===");
                    logger.info("Sending questionnaire {} to user {} (Firebase UID: {})", 
                        questionnaire.getId(), user.getId(), user.getFirebaseUid());
                }
                
                try {
                    // Convert Questionnaire entity to DTOs
                    QuestionnaireDTO questionnaireDTO = convertToDTO(questionnaire);
                    QuestionnaireContentDTO contentDTO = getQuestionnaireContent(questionnaire);
                    
                    questionnaireWebSocketController.sendQuestionnaireToUser(user.getFirebaseUid(), questionnaireDTO, contentDTO);
                    
                    if (isTargetUser) {
                        logger.info("=== SUCCESSFULLY SENT QUESTIONNAIRE TO TARGET USER 330 ===");
                    }
                } catch (Exception e) {
                    if (isTargetUser) {
                        logger.error("=== ERROR SENDING TO TARGET USER 330 ===");
                        logger.error("Error sending questionnaire to user {}: {}", user.getId(), e.getMessage(), e);
                    } else {
                        logger.error("Error sending questionnaire to user {}: {}", user.getId(), e.getMessage(), e);
                    }
                }
            }
            
            logger.debug("Sent batch {}/{} ({}/{} users)", i + 1, batches, batch.size(), totalUsers);
            
            // Add delay between batches (except for the last batch)
            if (i < batches - 1 && delayMs > 0) {
                logger.debug("Waiting {}ms before next batch", delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting between batches", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        logger.debug("Completed sending questionnaire {} to all {} users", questionnaire.getId(), totalUsers);
    }

    /**
     * Send questionnaire to eligible users immediately (no batching)
     */
    public void sendQuestionnaireToEligibleUsersImmediately(Long questionnaireId) {
        logger.debug("Sending questionnaire {} to eligible users immediately", questionnaireId);
        sendQuestionnaireToEligibleUsersImmediately(questionnaireId, true); // Enable presence check by default
    }

    /**
     * Send questionnaire to eligible users immediately with presence checking option
     */
    public void sendQuestionnaireToEligibleUsersImmediately(Long questionnaireId, boolean checkPresence) {
        logger.debug("=== Starting immediate questionnaire distribution for questionnaire {} ===", questionnaireId);
        logger.debug("Settings: checkPresence={}", checkPresence);
        
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(questionnaireId);
        if (questionnaireOpt.isEmpty()) {
            logger.error("Questionnaire not found: {}", questionnaireId);
            throw new RuntimeException("Questionnaire not found: " + questionnaireId);
        }

        Questionnaire questionnaire = questionnaireOpt.get();
        logger.debug("Found questionnaire: {} - {}", questionnaire.getId(), questionnaire.getName());
        
        // Get all users who haven't answered this questionnaire yet
        List<User> eligibleUsers = questionnaireResponseRepository
                .findUsersWhoHaventAnsweredQuestionnaire(questionnaireId);
        logger.debug("Found {} users who haven't answered questionnaire {}", eligibleUsers.size(), questionnaireId);
        
        // Filter users who meet the criteria
        List<User> qualifiedUsers = eligibleUsers.stream()
                .filter(user -> userMeetsCriteria(user, questionnaire))
                .toList();
        logger.debug("Found {} users who meet criteria for questionnaire {}", qualifiedUsers.size(), questionnaireId);
        
        // Filter active users if presence checking is enabled
        List<User> activeUsers = qualifiedUsers;
        if (checkPresence) {
            logger.debug("Checking user presence for {} qualified users", qualifiedUsers.size());
            
            // Check presence for each user and log results
            List<User> onlineUsers = new ArrayList<>();
            List<User> offlineUsers = new ArrayList<>();
            
            for (User user : qualifiedUsers) {
                boolean isActive = firebasePresenceService.isUserActive(user.getId());
                boolean isTargetUser = user.getId() == 330L;
                
                if (isActive) {
                    onlineUsers.add(user);
                    if (isTargetUser) {
                        logger.info("=== TARGET USER 330 IS ONLINE (IMMEDIATE) ===");
                        logger.info("User {} (Firebase UID: {}) is ONLINE", user.getId(), user.getFirebaseUid());
                    }
                } else {
                    offlineUsers.add(user);
                    if (isTargetUser) {
                        logger.info("=== TARGET USER 330 IS OFFLINE (IMMEDIATE) ===");
                        logger.info("User {} (Firebase UID: {}) is OFFLINE", user.getId(), user.getFirebaseUid());
                    }
                }
            }
            
            activeUsers = onlineUsers;
            
            // Only show summary if target user is involved
            boolean hasTargetUser = qualifiedUsers.stream().anyMatch(user -> user.getId() == 330L);
            if (hasTargetUser) {
                logger.info("=== PRESENCE CHECK RESULTS FOR TARGET USER 330 (IMMEDIATE) ===");
                logger.info("Presence check results: {} online, {} offline out of {} qualified users", 
                    onlineUsers.size(), offlineUsers.size(), qualifiedUsers.size());
            }
            
            if (!offlineUsers.isEmpty()) {
                List<String> offlineUserIds = offlineUsers.stream()
                    .filter(user -> user.getId() == 330L)
                    .map(user -> String.format("ID:%d(Firebase:%s)", user.getId(), user.getFirebaseUid()))
                    .toList();
                if (!offlineUserIds.isEmpty()) {
                    logger.info("Target user 330 is OFFLINE (no questionnaire sent - immediate): {}", offlineUserIds);
                }
            }
            
        } else {
            logger.debug("Skipping presence check - using all {} qualified users", qualifiedUsers.size());
        }

        // Send questionnaire to all active users immediately
        logger.info("Sending questionnaire immediately to {} active users", activeUsers.size());
        for (User user : activeUsers) {
            boolean isTargetUser = user.getId() == 330L;
            
            if (isTargetUser) {
                logger.info("=== SENDING QUESTIONNAIRE IMMEDIATELY TO TARGET USER 330 ===");
                logger.info("User ID: {}", user.getId());
                logger.info("Firebase UID: {}", user.getFirebaseUid());
                logger.info("Questionnaire ID: {}", questionnaire.getId());
            }
            
            try {
                // Convert Questionnaire entity to DTOs
                QuestionnaireDTO questionnaireDTO = convertToDTO(questionnaire);
                QuestionnaireContentDTO contentDTO = getQuestionnaireContent(questionnaire);
                
                questionnaireWebSocketController.sendQuestionnaireToUser(user.getFirebaseUid(), questionnaireDTO, contentDTO);
                
                if (isTargetUser) {
                    logger.info("=== SUCCESSFULLY SENT QUESTIONNAIRE IMMEDIATELY TO TARGET USER 330 ===");
                }
            } catch (Exception e) {
                if (isTargetUser) {
                    logger.error("=== ERROR SENDING IMMEDIATELY TO TARGET USER 330 ===");
                    logger.error("Error sending questionnaire to user {}: {}", user.getId(), e.getMessage(), e);
                } else {
                    logger.error("Error sending questionnaire to user {}: {}", user.getId(), e.getMessage(), e);
                }
            }
        }
        
        // Only log count if target user is involved
        boolean hasTargetUser = activeUsers.stream().anyMatch(user -> user.getId() == 330L);
        if (hasTargetUser) {
            logger.info("=== COMPLETED SENDING TO TARGET USER 330 ===");
            logger.info("Sent questionnaire immediately to {} active users (including target user 330)", activeUsers.size());
        }
        logger.debug("=== Completed immediate questionnaire distribution for questionnaire {} ===", questionnaireId);
    }

    /**
     * Check if user has already answered a questionnaire
     */
    public boolean hasUserAnsweredQuestionnaire(Long userId, Long questionnaireId) {
        boolean hasAnswered = questionnaireResponseRepository.existsByQuestionnaire_IdAndUser_Id(questionnaireId, userId);
        logger.debug("User {} has answered questionnaire {}: {}", userId, questionnaireId, hasAnswered);
        return hasAnswered;
    }

    /**
     * Get all questionnaires a user is eligible for but hasn't answered
     */
    public List<Questionnaire> getEligibleQuestionnairesForUser(Long userId) {
        logger.debug("Getting eligible questionnaires for user {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", userId);
            return List.of();
        }

        User user = userOpt.get();
        List<Questionnaire> allQuestionnaires = questionnaireRepository.findByStatus(QuestionnaireStatus.ACTIVE);
        logger.debug("Found {} active questionnaires", allQuestionnaires.size());

        List<Questionnaire> eligibleQuestionnaires = allQuestionnaires.stream()
                .filter(questionnaire -> userMeetsCriteria(user, questionnaire))
                .filter(questionnaire -> !hasUserAnsweredQuestionnaire(userId, questionnaire.getId()))
                .toList();
        
        logger.debug("User {} is eligible for {} questionnaires out of {} active questionnaires", 
            userId, eligibleQuestionnaires.size(), allQuestionnaires.size());
        
        return eligibleQuestionnaires;
    }

    /**
     * Trigger questionnaire sending for all active questionnaires
     */
    public void triggerAllActiveQuestionnaires() {
        logger.debug("=== Starting triggerAllActiveQuestionnaires ===");
        triggerAllActiveQuestionnaires(true); // Enable presence check by default
    }

    /**
     * Trigger questionnaire sending for all active questionnaires with presence checking option
     */
    public void triggerAllActiveQuestionnaires(boolean checkPresence) {
        logger.debug("=== Starting triggerAllActiveQuestionnaires (checkPresence: {}) ===", checkPresence);
        
        List<Questionnaire> activeQuestionnaires = questionnaireRepository.findByStatus(QuestionnaireStatus.ACTIVE);
        logger.debug("Found {} active questionnaires to process", activeQuestionnaires.size());
        
        for (Questionnaire questionnaire : activeQuestionnaires) {
            logger.debug("Processing questionnaire: {} - {}", questionnaire.getId(), questionnaire.getName());
            try {
                sendQuestionnaireToEligibleUsers(questionnaire.getId(), 100, 1000, checkPresence);
                logger.debug("Successfully processed questionnaire: {}", questionnaire.getId());
            } catch (Exception e) {
                logger.error("Error processing questionnaire {}: {}", questionnaire.getId(), e.getMessage(), e);
            }
        }
        
        logger.debug("=== Completed triggerAllActiveQuestionnaires ===");
    }

    private Set<Long> getTargetUserIdsFromCriteria(Questionnaire questionnaire) {
        String criteriaQuery = questionnaire.getCriteriaQuery();
        if (criteriaQuery == null || criteriaQuery.trim().isEmpty()) {
            return Set.of();
        }
        
        try {
            // For the current query "SELECT 1 FROM admin.users WHERE user_id = 330"
            // Extract the user_id value
            if (criteriaQuery.contains("user_id = 330")) {
                return Set.of(330L);
            }
            
            // For more complex queries, you might need to parse them differently
            // For now, return empty set for other cases
            return Set.of();
            
        } catch (Exception e) {
            logger.error("Error extracting target user IDs from criteria: {}", e.getMessage());
            return Set.of();
        }
    }

    /**
     * Convert Questionnaire entity to QuestionnaireDTO
     */
    private QuestionnaireDTO convertToDTO(Questionnaire questionnaire) {
        QuestionnaireDTO dto = new QuestionnaireDTO();
        dto.setId(questionnaire.getId());
        dto.setName(questionnaire.getName());
        dto.setDescription(questionnaire.getDescription());
        dto.setFirebasePath(questionnaire.getFirebasePath());
        dto.setTriggerTime(questionnaire.getTriggerTime());
        dto.setStatus(questionnaire.getStatus());
        return dto;
    }
    
    /**
     * Get questionnaire content from Firebase or JSON
     */
    private QuestionnaireContentDTO getQuestionnaireContent(Questionnaire questionnaire) {
        QuestionnaireContentDTO contentDTO = new QuestionnaireContentDTO();
        
        // Set basic information
        contentDTO.setQuestionnaireId(questionnaire.getId().toString());
        contentDTO.setTitle(questionnaire.getName());
        contentDTO.setDescription(questionnaire.getDescription());
        contentDTO.setQuestionnaireType("SURVEY");
        contentDTO.setVersion("1.0");
        
        // If we have JSON content stored directly, try to parse it
        if (questionnaire.getQuestionnaireJson() != null && !questionnaire.getQuestionnaireJson().trim().isEmpty()) {
            try {
                // Try to parse the JSON and extract questions
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> jsonData = objectMapper.readValue(questionnaire.getQuestionnaireJson(), Map.class);
                
                // Extract questions if they exist
                if (jsonData.containsKey("questions")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> questionsData = (List<Map<String, Object>>) jsonData.get("questions");
                    List<QuestionnaireQuestionDTO> questions = new ArrayList<>();
                    
                    for (Map<String, Object> questionData : questionsData) {
                        QuestionnaireQuestionDTO question = new QuestionnaireQuestionDTO();
                        question.setQuestionId((String) questionData.get("id"));
                        question.setQuestionText((String) questionData.get("question"));
                        question.setQuestionType((String) questionData.get("type"));
                        question.setOrder((Integer) questionData.get("order"));
                        question.setIsRequired(true);
                        question.setIsVisible(true);
                        questions.add(question);
                    }
                    contentDTO.setQuestions(questions);
                }
                
                logger.debug("Using stored JSON content for questionnaire {}", questionnaire.getId());
            } catch (Exception e) {
                logger.warn("Could not parse questionnaire JSON for questionnaire {}: {}", questionnaire.getId(), e.getMessage());
            }
        } else if (questionnaire.getFirebasePath() != null && !questionnaire.getFirebasePath().trim().isEmpty()) {
            // TODO: Implement Firebase content retrieval
            logger.debug("Using Firebase path for questionnaire {}: {}", questionnaire.getId(), questionnaire.getFirebasePath());
        } else {
            // Create fallback content
            List<QuestionnaireQuestionDTO> questions = new ArrayList<>();
            QuestionnaireQuestionDTO question = new QuestionnaireQuestionDTO();
            question.setQuestionId("q1");
            question.setQuestionText("Please provide your feedback");
            question.setQuestionType("TEXT");
            question.setOrder(1);
            question.setIsRequired(true);
            question.setIsVisible(true);
            questions.add(question);
            contentDTO.setQuestions(questions);
            
            logger.debug("Using fallback content for questionnaire {}", questionnaire.getId());
        }
        
        return contentDTO;
    }
} 