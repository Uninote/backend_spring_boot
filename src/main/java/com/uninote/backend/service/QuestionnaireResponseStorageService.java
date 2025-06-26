package com.uninote.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
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
    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    /**
     * Store questionnaire response in Firebase Storage and database
     * Path: questionnaire-responses/{questionnaire_name}_{date}/{firebase_uid}_{timestamp}.json
     */
    public String storeQuestionnaireResponse(Long questionnaireId, Long userId, Map<String, Object> responses, String sessionId) {
        try {
            // Get questionnaire and user data
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new RuntimeException("Questionnaire not found: " + questionnaireId));
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Generate Firebase Storage path
            String firebaseStoragePath = generateFirebaseStoragePath(questionnaire, user);
            String firebaseResponseId = UUID.randomUUID().toString();

            // Create response data structure
            Map<String, Object> responseData = Map.of(
                "questionnaireId", questionnaireId,
                "userId", userId,
                "firebaseUid", user.getFirebaseUid(),
                "responses", responses,
                "sessionId", sessionId,
                "completedAt", LocalDateTime.now().toString(),
                "firebaseResponseId", firebaseResponseId
            );

            // Try to store in Firebase Storage first
            boolean firebaseSuccess = false;
            try {
                System.out.println("Attempting to store in Firebase Storage at path: " + firebaseStoragePath);
                
                // Convert response data to JSON
                String jsonData = objectMapper.writeValueAsString(responseData);
                
                // Create blob info
                BlobId blobId = BlobId.of("uninote-app.appspot.com", firebaseStoragePath);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("application/json")
                    .build();
                
                // Upload to Firebase Storage
                Blob blob = storage.create(blobInfo, jsonData.getBytes("UTF-8"));
                
                firebaseSuccess = true;
                System.out.println("✅ Successfully stored in Firebase Storage");
                System.out.println("  Storage URL: " + blob.getMediaLink());
                System.out.println("  Storage Path: " + firebaseStoragePath);
                
            } catch (Exception firebaseError) {
                System.out.println("⚠️ Firebase Storage failed: " + firebaseError.getMessage());
                System.out.println("Falling back to database-only storage");
                firebaseSuccess = false;
            }

            // Store metadata in database (always do this)
            QuestionnaireResponse dbResponse = new QuestionnaireResponse();
            dbResponse.setQuestionnaire(questionnaire);
            dbResponse.setUser(user);
            dbResponse.setFirebaseResponseId(firebaseResponseId);
            dbResponse.setSessionId(sessionId);
            dbResponse.setIsComplete(true);
            dbResponse.setCompletedAt(LocalDateTime.now());
            dbResponse.setCreatedAt(LocalDateTime.now());
            
            QuestionnaireResponse savedResponse = questionnaireResponseRepository.save(dbResponse);

            System.out.println("✅ Questionnaire response stored successfully:");
            System.out.println("  Database ID: " + savedResponse.getId());
            System.out.println("  Firebase Response ID: " + firebaseResponseId);
            System.out.println("  User: " + user.getFirebaseUid());
            System.out.println("  Questionnaire: " + questionnaire.getName());
            System.out.println("  Session ID: " + sessionId);
            System.out.println("  Firebase Storage: " + (firebaseSuccess ? "SUCCESS" : "FAILED (database only)"));

            return firebaseResponseId;

        } catch (Exception e) {
            System.err.println("Error storing questionnaire response: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to store questionnaire response", e);
        }
    }

    /**
     * Generate Firebase Storage path based on questionnaire name, user, and timestamp
     * Format: questionnaire-responses/{questionnaire_name}_{date}/{firebase_uid}_{timestamp}.json
     */
    private String generateFirebaseStoragePath(Questionnaire questionnaire, User user) {
        // Extract questionnaire name from name (remove spaces, special chars)
        String questionnaireName = questionnaire.getName()
            .toLowerCase()
            .replaceAll("[^a-zA-Z0-9]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", ""); // Remove leading/trailing underscores

        // Get current date in format dd_MM_yyyy
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy"));
        
        // Get current timestamp for unique filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        return String.format("questionnaire-responses/%s_%s/%s_%s.json", 
            questionnaireName, currentDate, user.getFirebaseUid(), timestamp);
    }

    /**
     * Get questionnaire response from Firebase Storage
     */
    public Map<String, Object> getQuestionnaireResponse(String firebaseStoragePath) {
        try {
            BlobId blobId = BlobId.of("uninote-app.appspot.com", firebaseStoragePath);
            Blob blob = storage.get(blobId);
            
            if (blob == null) {
                System.out.println("Response not found in Firebase Storage: " + firebaseStoragePath);
                return null;
            }
            
            String jsonData = new String(blob.getContent());
            return objectMapper.readValue(jsonData, Map.class);

        } catch (Exception e) {
            System.err.println("Error retrieving questionnaire response from Firebase Storage: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete questionnaire response from Firebase Storage
     */
    public void deleteQuestionnaireResponse(String firebaseStoragePath) {
        try {
            BlobId blobId = BlobId.of("uninote-app.appspot.com", firebaseStoragePath);
            boolean deleted = storage.delete(blobId);
            
            if (deleted) {
                System.out.println("✅ Questionnaire response deleted from Firebase Storage: " + firebaseStoragePath);
            } else {
                System.out.println("⚠️ Response not found in Firebase Storage for deletion: " + firebaseStoragePath);
            }

        } catch (Exception e) {
            System.err.println("Error deleting questionnaire response from Firebase Storage: " + e.getMessage());
            throw new RuntimeException("Failed to delete questionnaire response from Firebase Storage", e);
        }
    }
} 