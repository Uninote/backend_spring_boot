package com.uninote.backend.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.QuestionnaireResponse;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.QuestionnaireRepository;
import com.uninote.backend.repository.QuestionnaireResponseRepository;
import com.uninote.backend.repository.UserRepository;

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
     * Store questionnaire response in database
     */
    public String storeQuestionnaireResponse(Long questionnaireId, Long userId, Map<String, Object> responses) {
        try {
            // Get questionnaire and user data
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new RuntimeException("Questionnaire not found: " + questionnaireId));
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Generate unique response ID
            String responseId = UUID.randomUUID().toString();

            // Create response data structure
            Map<String, Object> responseData = Map.of(
                "questionnaireId", questionnaireId,
                "userId", userId,
                "responses", responses,
                "completedAt", LocalDateTime.now().toString(),
                "responseId", responseId
            );

            // Store in database
            QuestionnaireResponse dbResponse = new QuestionnaireResponse();
            dbResponse.setQuestionnaire(questionnaire);
            dbResponse.setUser(user);
            dbResponse.setResponseData(convertToJson(responseData));
            dbResponse.setResponseId(responseId);
            dbResponse.setIsComplete(true);
            dbResponse.setCompletedAt(LocalDateTime.now());
            dbResponse.setCreatedAt(LocalDateTime.now());
            
            QuestionnaireResponse savedResponse = questionnaireResponseRepository.save(dbResponse);

                    // Removed System.out.println statements to reduce log noise

            return responseId;

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