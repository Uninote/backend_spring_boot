package com.uninote.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.controller.QuestionnaireWebSocketController;
import com.uninote.backend.dto.QuestionnaireContentDTO;
import com.uninote.backend.dto.QuestionnaireDTO;
import com.uninote.backend.dto.QuestionnaireQuestionDTO;
import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.QuestionnaireStatus;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.QuestionnaireRepository;
import com.uninote.backend.repository.QuestionnaireResponseRepository;
import com.uninote.backend.repository.UserRepository;

@Service
public class QuestionnaireTriggerService {
    
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
        
        // Get criteria query from questionnaire
        String criteriaQuery = questionnaire.getCriteriaQuery();
        
        if (criteriaQuery == null || criteriaQuery.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Use the criteria query service to evaluate the SQL query
            boolean meetsCriteria = questionnaireCriteriaService.evaluateCriteriaQuery(criteriaQuery, user);
            return meetsCriteria;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Send questionnaire to eligible users who haven't answered yet
     */
    public void sendQuestionnaireToEligibleUsers(Long questionnaireId) {
        sendQuestionnaireToEligibleUsers(questionnaireId, 100, 1000, true); // Default: 100 users per batch, 1000ms delay, ENABLE presence check
    }

    /**
     * Send questionnaire to eligible users with batching and rate limiting
     */
    public void sendQuestionnaireToEligibleUsers(Long questionnaireId, int batchSize, long delayMs) {
        sendQuestionnaireToEligibleUsers(questionnaireId, batchSize, delayMs, true);
    }

    /**
     * Send questionnaire to eligible users with presence checking option
     */
    public void sendQuestionnaireToEligibleUsers(Long questionnaireId, int batchSize, long delayMs, boolean checkPresence) {
        
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(questionnaireId);
        if (questionnaireOpt.isEmpty()) {
            throw new RuntimeException("Questionnaire not found: " + questionnaireId);
        }

        Questionnaire questionnaire = questionnaireOpt.get();
        
        // Get all users who haven't answered this questionnaire yet
        List<User> eligibleUsers = questionnaireResponseRepository
                .findUsersWhoHaventAnsweredQuestionnaire(questionnaireId);
            
            // Get target user IDs from criteria query first (more efficient)
            Set<Long> targetUserIds = getTargetUserIdsFromCriteria(questionnaire);
            
            // Filter users who meet the criteria (much faster now)
            List<User> qualifiedUsers = eligibleUsers.stream()
                    .filter(user -> targetUserIds.contains(user.getId()))
                    .collect(Collectors.toList());
            
            // Filter out users who have acknowledged the questionnaire (RECEIVED, DISMISSED, etc.)
            List<User> usersWithoutAcknowledgments = qualifiedUsers.stream()
                    .filter(user -> !hasUserAcknowledgedQuestionnaire(user.getId(), questionnaireId))
                    .collect(Collectors.toList());
            
            // Filter active users if presence checking is enabled
            List<User> activeUsers = usersWithoutAcknowledgments;
            if (checkPresence) {
                
                // Check presence for each user and log results
                List<User> onlineUsers = new ArrayList<>();
                List<User> offlineUsers = new ArrayList<>();
                
                for (User user : usersWithoutAcknowledgments) {
                    boolean isActive = true; // Assuming all users are active for now
                    
                    if (isActive) {
                        onlineUsers.add(user);
                    } else {
                        offlineUsers.add(user);
                    }
                }
                
                activeUsers = onlineUsers;
            }

            // Send questionnaire to active users in batches
            sendQuestionnaireInBatches(activeUsers, questionnaire, batchSize, delayMs);
    }

    /**
     * Send questionnaire to users in batches with delay
     */
    private void sendQuestionnaireInBatches(List<User> users, Questionnaire questionnaire, int batchSize, long delayMs) {
        int totalUsers = users.size();
        int batches = (int) Math.ceil((double) totalUsers / batchSize);
        
        for (int i = 0; i < batches; i++) {
            int startIndex = i * batchSize;
            int endIndex = Math.min(startIndex + batchSize, totalUsers);
            
            List<User> batch = users.subList(startIndex, endIndex);
            
            // Send to current batch
            for (User user : batch) {
                try {
                    // Convert Questionnaire entity to DTOs
                    QuestionnaireDTO questionnaireDTO = convertToDTO(questionnaire);
                    QuestionnaireContentDTO contentDTO = getQuestionnaireContent(questionnaire);
                    
                    questionnaireWebSocketController.sendQuestionnaireToUser(user.getId().toString(), questionnaireDTO, contentDTO);
                } catch (Exception e) {
                    // Silent error handling
                }
            }
            
            // Add delay between batches (except for the last batch)
            if (i < batches - 1 && delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Send questionnaire to users by IDs in batches with delay (more memory efficient)
     */
    private void sendQuestionnaireInBatchesByIds(List<Long> userIds, Questionnaire questionnaire, int batchSize, long delayMs) {
        int totalUsers = userIds.size();
        int batches = (int) Math.ceil((double) totalUsers / batchSize);
        
        for (int i = 0; i < batches; i++) {
            int startIndex = i * batchSize;
            int endIndex = Math.min(startIndex + batchSize, totalUsers);
            
            List<Long> batch = userIds.subList(startIndex, endIndex);
            
            // Send to current batch
            for (Long userId : batch) {
                try {
                    // Convert Questionnaire entity to DTOs
                    QuestionnaireDTO questionnaireDTO = convertToDTO(questionnaire);
                    QuestionnaireContentDTO contentDTO = getQuestionnaireContent(questionnaire);
                    
                    questionnaireWebSocketController.sendQuestionnaireToUser(userId.toString(), questionnaireDTO, contentDTO);
                } catch (Exception e) {
                    // Silent error handling
                }
            }
            
            // Add delay between batches (except for the last batch)
            if (i < batches - 1 && delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Send questionnaire to eligible users immediately (no batching)
     */
    public void sendQuestionnaireToEligibleUsersImmediately(Long questionnaireId) {
        sendQuestionnaireToEligibleUsersImmediately(questionnaireId, true); // Enable presence check by default
    }

    /**
     * Send questionnaire to eligible users immediately with presence checking option
     */
    public void sendQuestionnaireToEligibleUsersImmediately(Long questionnaireId, boolean checkPresence) {
        
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(questionnaireId);
        if (questionnaireOpt.isEmpty()) {
            throw new RuntimeException("Questionnaire not found: " + questionnaireId);
        }

        Questionnaire questionnaire = questionnaireOpt.get();
        
        // Get all users who haven't answered this questionnaire yet
        List<User> eligibleUsers = questionnaireResponseRepository
                .findUsersWhoHaventAnsweredQuestionnaire(questionnaireId);
        
        // Filter users who meet the criteria
        List<User> qualifiedUsers = eligibleUsers.stream()
                .filter(user -> userMeetsCriteria(user, questionnaire))
                .collect(Collectors.toList());
        
        // Filter out users who have acknowledged the questionnaire (RECEIVED, DISMISSED, etc.)
        List<User> usersWithoutAcknowledgments = qualifiedUsers.stream()
                .filter(user -> !hasUserAcknowledgedQuestionnaire(user.getId(), questionnaireId))
                .collect(Collectors.toList());
        
        // Filter active users if presence checking is enabled
        List<User> activeUsers = usersWithoutAcknowledgments;
        if (checkPresence) {
            
            // Check presence for each user and log results
            List<User> onlineUsers = new ArrayList<>();
            List<User> offlineUsers = new ArrayList<>();
            
            for (User user : usersWithoutAcknowledgments) {
                boolean isActive = true; // Assuming all users are active for now
                
                if (isActive) {
                    onlineUsers.add(user);
                } else {
                    offlineUsers.add(user);
                }
            }
            
            activeUsers = onlineUsers;
        }

        // Send questionnaire to all active users immediately
        for (User user : activeUsers) {
            try {
                // Convert Questionnaire entity to DTOs
                QuestionnaireDTO questionnaireDTO = convertToDTO(questionnaire);
                QuestionnaireContentDTO contentDTO = getQuestionnaireContent(questionnaire);
                
                questionnaireWebSocketController.sendQuestionnaireToUser(user.getId().toString(), questionnaireDTO, contentDTO);
            } catch (Exception e) {
                // Silent error handling
            }
        }
    }

    /**
     * Check if user has already answered a questionnaire
     */
    public boolean hasUserAnsweredQuestionnaire(Long userId, Long questionnaireId) {
        boolean hasAnswered = questionnaireResponseRepository.existsByQuestionnaire_IdAndUser_Id(questionnaireId, userId);
        return hasAnswered;
    }

    /**
     * Get all questionnaires a user is eligible for but hasn't answered
     */
    public List<Questionnaire> getEligibleQuestionnairesForUser(Long userId) {
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return List.of();
        }

        User user = userOpt.get();
        List<Questionnaire> allQuestionnaires = questionnaireRepository.findByStatus(QuestionnaireStatus.ACTIVE);

        List<Questionnaire> eligibleQuestionnaires = allQuestionnaires.stream()
                .filter(questionnaire -> userMeetsCriteria(user, questionnaire))
                .filter(questionnaire -> !hasUserAnsweredQuestionnaire(userId, questionnaire.getId()))
                .collect(Collectors.toList());
        
        return eligibleQuestionnaires;
    }

    /**
     * Trigger questionnaire sending for all active questionnaires
     */
    public void triggerAllActiveQuestionnaires() {
        triggerAllActiveQuestionnaires(true); // Enable presence check by default
    }

    /**
     * Trigger questionnaire sending for all active questionnaires with presence checking option
     */
    public void triggerAllActiveQuestionnaires(boolean checkPresence) {
        
        List<Questionnaire> activeQuestionnaires = questionnaireRepository.findByStatus(QuestionnaireStatus.ACTIVE);
        
        for (Questionnaire questionnaire : activeQuestionnaires) {
            try {
                sendQuestionnaireToEligibleUsers(questionnaire.getId(), 100, 1000, checkPresence);
            } catch (Exception e) {
                // Silent error handling
            }
        }
    }

    private Set<Long> getTargetUserIdsFromCriteria(Questionnaire questionnaire) {
        String criteriaQuery = questionnaire.getCriteriaQuery();
        if (criteriaQuery == null || criteriaQuery.trim().isEmpty()) {
            return Set.of();
        }
        
        try {
            
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
                        return Set.of(userId);
                    } catch (NumberFormatException e) {
                        // Silent error handling
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
                        return Set.of(userId);
                    } catch (NumberFormatException e) {
                        // Silent error handling
                    }
                }
            }
            
            // For more complex queries, you might need to parse them differently
            return Set.of();
            
        } catch (Exception e) {
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
                
                // First try to parse as QuestionnaireContentDTO
                try {
                    return objectMapper.readValue(questionnaire.getQuestionnaireJson(), QuestionnaireContentDTO.class);
                } catch (Exception e) {
                    
                    // Try to parse as generic JSON and convert
                    return parseGenericJsonToContent(questionnaire.getQuestionnaireJson(), questionnaire);
                }
            } else {
                return createFallbackContent(questionnaire);
            }
        } catch (Exception e) {
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
     * DISABLED: This was causing memory issues due to frequent execution
     * Runs every 30 seconds for testing
     */
    // @Scheduled(fixedRate = 30000)
    public void checkAndSendQuestionnaires() {
        
        try {
            List<Questionnaire> activeQuestionnaires = questionnaireRepository.findByStatus(QuestionnaireStatus.ACTIVE);
            
            for (Questionnaire questionnaire : activeQuestionnaires) {
                
                List<Long> eligibleUsers = questionnaireService.getEligibleUsers(questionnaire.getId());
                
                for (Long userId : eligibleUsers) {
                    sendQuestionnaireToUser(questionnaire, userId);
                }
            }
        } catch (Exception e) {
            // Silent error handling
        }
    }

    /**
     * Send questionnaire to a specific user via WebSocket
     */
    private void sendQuestionnaireToUser(Questionnaire questionnaire, Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return;
            }
            
            if (user.getFirebaseUid() == null || user.getFirebaseUid().isEmpty()) {
                return;
            }
            
            // Convert questionnaire to DTO
            QuestionnaireDTO questionnaireDTO = convertToDTO(questionnaire);
            QuestionnaireContentDTO contentDTO = getQuestionnaireContent(questionnaire);
            
            // Send via WebSocket (no presence check)
            questionnaireWebSocketController.sendQuestionnaireToUser(user.getId().toString(), questionnaireDTO, contentDTO);
            
        } catch (Exception e) {
            // Silent error handling
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
                return true;
            }
            
            // Check for DISMISSED acknowledgment
            boolean hasDismissed = acknowledgmentService.hasUserAcknowledged(userId, questionnaireId, "DISMISSED");
            if (hasDismissed) {
                return true;
            }
            
            // Check for COMPLETED acknowledgment
            boolean hasCompleted = acknowledgmentService.hasUserAcknowledged(userId, questionnaireId, "COMPLETED");
            if (hasCompleted) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            return false; // If there's an error, allow the questionnaire to be sent
        }
    }
}