package com.uninote.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
    private QuestionnaireService questionnaireService;

    @Autowired
    private QuestionnaireAcknowledgmentService acknowledgmentService;

    @Autowired
    private ObjectMapper objectMapper;

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
                .collect(Collectors.toList());
        
        logger.debug("Found {} users who meet criteria for questionnaire {} out of {} eligible users", 
            qualifiedUsers.size(), questionnaireId, eligibleUsers.size());
        
        // Filter out users who have acknowledged the questionnaire (RECEIVED, DISMISSED, etc.)
        logger.debug("Checking acknowledgments for {} qualified users", qualifiedUsers.size());
        List<User> usersWithoutAcknowledgments = qualifiedUsers.stream()
                .filter(user -> !hasUserAcknowledgedQuestionnaire(user.getId(), questionnaireId))
                .collect(Collectors.toList());
        
        logger.debug("Found {} users without acknowledgments for questionnaire {} out of {} qualified users", 
            usersWithoutAcknowledgments.size(), questionnaireId, qualifiedUsers.size());
        
        // Filter active users if presence checking is enabled
        List<User> activeUsers = usersWithoutAcknowledgments;
        if (checkPresence) {
            logger.debug("Checking user presence for {} users without acknowledgments", usersWithoutAcknowledgments.size());
            
            // Check presence for each user and log results
            List<User> onlineUsers = new ArrayList<>();
            List<User> offlineUsers = new ArrayList<>();
            
            for (User user : usersWithoutAcknowledgments) {
                boolean isActive = true; // Assuming all users are active for now
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
            boolean hasTargetUser = usersWithoutAcknowledgments.stream().anyMatch(user -> user.getId() == 330L);
            if (hasTargetUser) {
                logger.info("=== PRESENCE CHECK RESULTS FOR TARGET USER 330 ===");
                logger.info("Presence check results: {} online, {} offline out of {} users without acknowledgments", 
                    onlineUsers.size(), offlineUsers.size(), usersWithoutAcknowledgments.size());
            }
            
            if (!offlineUsers.isEmpty()) {
                List<String> offlineUserIds = offlineUsers.stream()
                    .filter(user -> user.getId() == 330L)
                    .map(user -> String.format("ID:%d(Firebase:%s)", user.getId(), user.getFirebaseUid()))
                    .collect(Collectors.toList());
                if (!offlineUserIds.isEmpty()) {
                    logger.info("Target user 330 is OFFLINE (no questionnaire sent): {}", offlineUserIds);
                }
            }
            
        } else {
            logger.debug("Skipping presence check - using all {} users without acknowledgments", usersWithoutAcknowledgments.size());
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
                    
                    questionnaireWebSocketController.sendQuestionnaireToUser(user.getId().toString(), questionnaireDTO, contentDTO);
                    
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
                .collect(Collectors.toList());
        logger.debug("Found {} users who meet criteria for questionnaire {}", qualifiedUsers.size(), questionnaireId);
        
        // Filter out users who have acknowledged the questionnaire (RECEIVED, DISMISSED, etc.)
        logger.debug("Checking acknowledgments for {} qualified users", qualifiedUsers.size());
        List<User> usersWithoutAcknowledgments = qualifiedUsers.stream()
                .filter(user -> !hasUserAcknowledgedQuestionnaire(user.getId(), questionnaireId))
                .collect(Collectors.toList());
        
        logger.debug("Found {} users without acknowledgments for questionnaire {} out of {} qualified users", 
            usersWithoutAcknowledgments.size(), questionnaireId, qualifiedUsers.size());
        
        // Filter active users if presence checking is enabled
        List<User> activeUsers = usersWithoutAcknowledgments;
        if (checkPresence) {
            logger.debug("Checking user presence for {} users without acknowledgments", usersWithoutAcknowledgments.size());
            
            // Check presence for each user and log results
            List<User> onlineUsers = new ArrayList<>();
            List<User> offlineUsers = new ArrayList<>();
            
            for (User user : usersWithoutAcknowledgments) {
                boolean isActive = true; // Assuming all users are active for now
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
            boolean hasTargetUser = usersWithoutAcknowledgments.stream().anyMatch(user -> user.getId() == 330L);
            if (hasTargetUser) {
                logger.info("=== PRESENCE CHECK RESULTS FOR TARGET USER 330 (IMMEDIATE) ===");
                logger.info("Presence check results: {} online, {} offline out of {} users without acknowledgments", 
                    onlineUsers.size(), offlineUsers.size(), usersWithoutAcknowledgments.size());
            }
            
            if (!offlineUsers.isEmpty()) {
                List<String> offlineUserIds = offlineUsers.stream()
                    .filter(user -> user.getId() == 330L)
                    .map(user -> String.format("ID:%d(Firebase:%s)", user.getId(), user.getFirebaseUid()))
                    .collect(Collectors.toList());
                if (!offlineUserIds.isEmpty()) {
                    logger.info("Target user 330 is OFFLINE (no questionnaire sent - immediate): {}", offlineUserIds);
                }
            }
            
        } else {
            logger.debug("Skipping presence check - using all {} users without acknowledgments", usersWithoutAcknowledgments.size());
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
                
                questionnaireWebSocketController.sendQuestionnaireToUser(user.getId().toString(), questionnaireDTO, contentDTO);
                
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
                .collect(Collectors.toList());
        
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
            logger.debug("Parsing criteria query: {}", criteriaQuery);
            
            // Handle "SELECT 1 FROM admin.users WHERE id = X" pattern
            if (criteriaQuery.contains("id = ")) {
                String[] parts = criteriaQuery.split("id = ");
                if (parts.length > 1) {
                    String userIdStr = parts[1].trim();
                    // Remove any trailing parts (like "AND ..." or closing parenthesis)
                    if (userIdStr.contains(" ")) {
                        userIdStr = userIdStr.split(" ")[0];
                    }
                    if (userIdStr.contains(")")) {
                        userIdStr = userIdStr.split("\\)")[0];
                    }
                    
                    try {
                        Long userId = Long.parseLong(userIdStr);
                        logger.debug("Extracted user ID from criteria: {}", userId);
                        return Set.of(userId);
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse user ID from criteria: {}", userIdStr);
                    }
                }
            }
            
            // Handle "SELECT 1 FROM admin.users WHERE user_id = X" pattern (legacy)
            if (criteriaQuery.contains("user_id = ")) {
                String[] parts = criteriaQuery.split("user_id = ");
                if (parts.length > 1) {
                    String userIdStr = parts[1].trim();
                    // Remove any trailing parts
                    if (userIdStr.contains(" ")) {
                        userIdStr = userIdStr.split(" ")[0];
                    }
                    if (userIdStr.contains(")")) {
                        userIdStr = userIdStr.split("\\)")[0];
                    }
                    
                    try {
                        Long userId = Long.parseLong(userIdStr);
                        logger.debug("Extracted user ID from legacy criteria: {}", userId);
                        return Set.of(userId);
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse user ID from legacy criteria: {}", userIdStr);
                    }
                }
            }
            
            // For more complex queries, you might need to parse them differently
            logger.warn("Could not parse criteria query: {}", criteriaQuery);
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
        dto.setStatus(questionnaire.getStatus());
        dto.setCreatedAt(questionnaire.getCreatedAt());
        dto.setTriggerTime(questionnaire.getTriggerTime());
        return dto;
    }
    
    /**
     * Get questionnaire content from database JSON
     */
    private QuestionnaireContentDTO getQuestionnaireContent(Questionnaire questionnaire) {
        try {
            // Try to get content from database JSON
            if (questionnaire.getQuestionnaireJson() != null && !questionnaire.getQuestionnaireJson().trim().isEmpty()) {
                logger.debug("Using stored JSON content for questionnaire {}", questionnaire.getId());
                
                // First try to parse as QuestionnaireContentDTO
                try {
                    return objectMapper.readValue(questionnaire.getQuestionnaireJson(), QuestionnaireContentDTO.class);
                } catch (Exception e) {
                    logger.warn("Could not parse as QuestionnaireContentDTO, trying to parse as generic JSON: {}", e.getMessage());
                    
                    // Try to parse as generic JSON and convert
                    return parseGenericJsonToContent(questionnaire.getQuestionnaireJson(), questionnaire);
                }
            } else {
                logger.warn("No questionnaire JSON content found for questionnaire {}", questionnaire.getId());
                return createFallbackContent(questionnaire);
            }
        } catch (Exception e) {
            logger.error("Error parsing questionnaire JSON for ID {}: {}", questionnaire.getId(), e.getMessage());
            return createFallbackContent(questionnaire);
        }
    }
    
    /**
     * Parse generic JSON and convert to QuestionnaireContentDTO
     */
    private QuestionnaireContentDTO parseGenericJsonToContent(String json, Questionnaire questionnaire) {
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(json, Map.class);
            
            QuestionnaireContentDTO contentDTO = new QuestionnaireContentDTO();
            contentDTO.setQuestionnaireId(questionnaire.getId().toString());
            contentDTO.setTitle((String) jsonMap.getOrDefault("title", questionnaire.getName()));
            contentDTO.setDescription((String) jsonMap.getOrDefault("description", questionnaire.getDescription()));
            contentDTO.setQuestionnaireType("SURVEY");
            contentDTO.setVersion("1.0");
            
            // Try to extract questions if they exist
            if (jsonMap.containsKey("questions") && jsonMap.get("questions") instanceof List) {
                List<QuestionnaireQuestionDTO> questions = new ArrayList<>();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> questionsData = (List<Map<String, Object>>) jsonMap.get("questions");
                
                for (int i = 0; i < questionsData.size(); i++) {
                    Map<String, Object> questionData = questionsData.get(i);
                    QuestionnaireQuestionDTO question = new QuestionnaireQuestionDTO();
                    question.setQuestionId((String) questionData.getOrDefault("id", "q" + (i + 1)));
                    question.setQuestionText((String) questionData.getOrDefault("question", "Question " + (i + 1)));
                    question.setQuestionType((String) questionData.getOrDefault("type", "TEXT"));
                    question.setOrder(i + 1);
                    question.setIsRequired(true);
                    question.setIsVisible(true);
                    
                    // Handle options field - it might be an array or map
                    if (questionData.containsKey("options")) {
                        Object optionsObj = questionData.get("options");
                        if (optionsObj instanceof List) {
                            // Convert array to map format
                            @SuppressWarnings("unchecked")
                            List<String> optionsList = (List<String>) optionsObj;
                            Map<String, Object> optionsMap = new HashMap<>();
                            for (int j = 0; j < optionsList.size(); j++) {
                                optionsMap.put("option" + (j + 1), optionsList.get(j));
                            }
                            question.setOptions(optionsMap);
                        } else if (optionsObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> optionsMap = (Map<String, Object>) optionsObj;
                            question.setOptions(optionsMap);
                        }
                    }
                    
                    questions.add(question);
                }
                contentDTO.setQuestions(questions);
            } else {
                // Create fallback question if no questions found
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
            }
            
            return contentDTO;
            
        } catch (Exception e) {
            logger.error("Error parsing generic JSON for questionnaire {}: {}", questionnaire.getId(), e.getMessage());
            return createFallbackContent(questionnaire);
        }
    }
    
    /**
     * Create fallback content when JSON parsing fails
     */
    private QuestionnaireContentDTO createFallbackContent(Questionnaire questionnaire) {
        QuestionnaireContentDTO contentDTO = new QuestionnaireContentDTO();
        contentDTO.setQuestionnaireId(questionnaire.getId().toString());
        contentDTO.setTitle(questionnaire.getName());
        contentDTO.setDescription(questionnaire.getDescription());
        contentDTO.setQuestionnaireType("SURVEY");
        contentDTO.setVersion("1.0");
        
        // Create a simple fallback question
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
        
        return contentDTO;
    }

    /**
     * Check for eligible questionnaires and send them to users
     * Runs every 30 seconds for testing
     */
    @Scheduled(fixedRate = 30000)
    public void checkAndSendQuestionnaires() {
        logger.info("🔄 Checking for eligible questionnaires...");
        
        try {
            List<Questionnaire> activeQuestionnaires = questionnaireRepository.findByStatus(QuestionnaireStatus.ACTIVE);
            logger.info("Found {} active questionnaires", activeQuestionnaires.size());
            
            for (Questionnaire questionnaire : activeQuestionnaires) {
                logger.info("Processing questionnaire: {}", questionnaire.getName());
                
                List<Long> eligibleUsers = questionnaireService.getEligibleUsers(questionnaire.getId());
                logger.info("Found {} eligible users for questionnaire: {}", eligibleUsers.size(), questionnaire.getName());
                
                for (Long userId : eligibleUsers) {
                    // Only log for user 330
                    if (userId == 330L) {
                        logger.info("=== SENDING QUESTIONNAIRE TO USER 330 ===");
                        logger.info("Questionnaire: {}", questionnaire.getName());
                        logger.info("User ID: {}", userId);
                    }
                    
                    sendQuestionnaireToUser(questionnaire, userId);
                }
            }
        } catch (Exception e) {
            logger.error("Error in questionnaire trigger service", e);
        }
    }

    /**
     * Send questionnaire to a specific user via WebSocket
     */
    private void sendQuestionnaireToUser(Questionnaire questionnaire, Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                if (userId == 330L) {
                    logger.error("User 330 not found in database");
                }
                return;
            }
            
            if (user.getFirebaseUid() == null || user.getFirebaseUid().isEmpty()) {
                if (userId == 330L) {
                    logger.error("User 330 has no Firebase UID");
                }
                return;
            }
            
            if (userId == 330L) {
                logger.info("User 330 found - Firebase UID: {}", user.getFirebaseUid());
            }
            
            // Convert questionnaire to DTO
            QuestionnaireDTO questionnaireDTO = convertToDTO(questionnaire);
            QuestionnaireContentDTO contentDTO = getQuestionnaireContent(questionnaire);
            
            // Send via WebSocket (no presence check)
            questionnaireWebSocketController.sendQuestionnaireToUser(user.getId().toString(), questionnaireDTO, contentDTO);
            
            if (userId == 330L) {
                logger.info("✅ Questionnaire sent to user 330 via WebSocket");
                logger.info("Topic: /topic/questionnaire/{}", user.getFirebaseUid());
            }
            
        } catch (Exception e) {
            if (userId == 330L) {
                logger.error("Error sending questionnaire to user 330: {}", e.getMessage());
            } else {
                logger.error("Error sending questionnaire to user {}: {}", userId, e.getMessage());
            }
        }
    }

    /**
     * Check if user has acknowledged a questionnaire (RECEIVED, DISMISSED, etc.)
     */
    private boolean hasUserAcknowledgedQuestionnaire(Long userId, Long questionnaireId) {
        try {
            // Check for RECEIVED acknowledgment
            boolean hasReceived = acknowledgmentService.hasUserAcknowledged(userId, questionnaireId, "RECEIVED");
            if (hasReceived) {
                logger.debug("User {} has RECEIVED acknowledgment for questionnaire {}", userId, questionnaireId);
                return true;
            }
            
            // Check for DISMISSED acknowledgment
            boolean hasDismissed = acknowledgmentService.hasUserAcknowledged(userId, questionnaireId, "DISMISSED");
            if (hasDismissed) {
                logger.debug("User {} has DISMISSED acknowledgment for questionnaire {}", userId, questionnaireId);
                return true;
            }
            
            // Check for COMPLETED acknowledgment
            boolean hasCompleted = acknowledgmentService.hasUserAcknowledged(userId, questionnaireId, "COMPLETED");
            if (hasCompleted) {
                logger.debug("User {} has COMPLETED acknowledgment for questionnaire {}", userId, questionnaireId);
                return true;
            }
            
            logger.debug("User {} has no blocking acknowledgments for questionnaire {}", userId, questionnaireId);
            return false;
            
        } catch (Exception e) {
            logger.warn("Error checking acknowledgments for user {} and questionnaire {}: {}", userId, questionnaireId, e.getMessage());
            return false; // If there's an error, allow the questionnaire to be sent
        }
    }
}