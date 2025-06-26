package com.uninote.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.QuestionnaireResponse;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.QuestionnaireRepository;
import com.uninote.backend.repository.QuestionnaireResponseRepository;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class QuestionnaireResponseStorageService {

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private QuestionnaireResponseRepository questionnaireResponseRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate Firebase Storage path based on questionnaire name, user, and timestamp
     * Format: questionnaire-responses/{questionnaire_name}_{date}/{firebase_uid}_{timestamp}.json
     */
    private String generateFirebaseStoragePath(Questionnaire questionnaire, User user) {
        String sanitizedName = questionnaire.getName().replaceAll("[^a-zA-Z0-9_]", "_");
        String date = LocalDateTime.now().toLocalDate().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        return String.format("questionnaire-responses/%s_%s/%s_%s.json", 
            sanitizedName, date, user.getFirebaseUid(), timestamp);
    }

    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    /**
     * Store questionnaire response in database only (Firebase Storage temporarily disabled)
     * Path: questionnaire-responses/{questionnaire_name}_{date}/{firebase_uid}_{timestamp}.json
     */
    public String storeQuestionnaireResponse(Long questionnaireId, Long userId, Map<String, Object> responses) {
        try {
            // Get questionnaire and user data
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new RuntimeException("Questionnaire not found: " + questionnaireId));
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Generate Firebase Storage path (for reference only)
            String firebaseStoragePath = generateFirebaseStoragePath(questionnaire, user);
            String firebaseResponseId = UUID.randomUUID().toString();

            // Create response data structure (for database storage)
            Map<String, Object> responseData = Map.of(
                "questionnaireId", questionnaireId,
                "userId", userId,
                "firebaseUid", user.getFirebaseUid(),
                "responses", responses,
                "completedAt", LocalDateTime.now().toString(),
                "firebaseResponseId", firebaseResponseId
            );

            // Temporarily disable Firebase Storage to prevent native crashes
            System.out.println("⚠️ Firebase Storage temporarily disabled to prevent native crashes");
            System.out.println("Would store at Firebase Storage path: " + firebaseStoragePath);
            boolean firebaseSuccess = false;

            // Store metadata in database (always do this)
            QuestionnaireResponse dbResponse = new QuestionnaireResponse();
            dbResponse.setQuestionnaire(questionnaire);
            dbResponse.setUser(user);
            dbResponse.setResponseData(convertToJson(responseData));
            dbResponse.setFirebaseResponseId(firebaseResponseId);
            dbResponse.setIsComplete(true);
            dbResponse.setCompletedAt(LocalDateTime.now());
            dbResponse.setCreatedAt(LocalDateTime.now());
            
            QuestionnaireResponse savedResponse = questionnaireResponseRepository.save(dbResponse);

            System.out.println("✅ Questionnaire response stored successfully:");
            System.out.println("  Database ID: " + savedResponse.getId());
            System.out.println("  Firebase Response ID: " + firebaseResponseId);
            System.out.println("  User: " + user.getFirebaseUid());
            System.out.println("  Questionnaire: " + questionnaire.getName());
            System.out.println("  Firebase Storage: DISABLED (database only)");

            return firebaseResponseId;

        } catch (Exception e) {
            System.err.println("Error storing questionnaire response: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to store questionnaire response", e);
        }
    }

    /**
     * Get questionnaire response from database
     */
    public QuestionnaireResponse getQuestionnaireResponse(Long responseId) {
        return questionnaireResponseRepository.findById(responseId)
            .orElseThrow(() -> new RuntimeException("Response not found: " + responseId));
    }

    /**
     * Get all responses for a user
     */
    public java.util.List<QuestionnaireResponse> getUserResponses(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return questionnaireResponseRepository.findByUser(user);
    }

    /**
     * Get all responses for a questionnaire
     */
    public java.util.List<QuestionnaireResponse> getQuestionnaireResponses(Long questionnaireId) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
            .orElseThrow(() -> new RuntimeException("Questionnaire not found: " + questionnaireId));
        return questionnaireResponseRepository.findByQuestionnaire(questionnaire);
    }

    /**
     * Check if user has already responded to a questionnaire
     */
    public boolean hasUserResponded(Long userId, Long questionnaireId) {
        return questionnaireResponseRepository.existsByQuestionnaire_IdAndUser_Id(questionnaireId, userId);
    }
} 