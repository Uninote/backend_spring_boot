package com.uninote.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.QuestionnaireResponse;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.QuestionnaireRepository;
import com.uninote.backend.repository.QuestionnaireResponseRepository;
import com.uninote.backend.repository.UserRepository;

@Service
public class QuestionnaireResponseStorageService {

    @Autowired
    private FirebaseDatabase firebaseDatabase;

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private QuestionnaireResponseRepository questionnaireResponseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Store questionnaire response in both Firebase and database
     * Path: /questionnaires/{questionnaire_name}_{date}/users/{firebase_uid}.json
     */
    public String storeQuestionnaireResponse(Long questionnaireId, Long userId, Map<String, Object> responses, String sessionId) {
        try {
            // Get questionnaire and user data
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new RuntimeException("Questionnaire not found: " + questionnaireId));
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Generate Firebase path
            String firebasePath = generateFirebasePath(questionnaire);
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

            // Try to store in Firebase first
            boolean firebaseSuccess = false;
            try {
                System.out.println("Attempting to store in Firebase at path: " + firebasePath + "/users/" + user.getFirebaseUid());
                
                DatabaseReference responseRef = firebaseDatabase.getReference(firebasePath + "/users/" + user.getFirebaseUid());
                
                CompletableFuture<Void> future = new CompletableFuture<>();
                
                responseRef.setValue(responseData, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        future.completeExceptionally(new RuntimeException("Failed to store response in Firebase: " + databaseError.getMessage()));
                    } else {
                        future.complete(null);
                    }
                });

                // Wait for Firebase operation to complete
                future.get(10, TimeUnit.SECONDS);
                firebaseSuccess = true;
                System.out.println("✅ Successfully stored in Firebase");
                
            } catch (Exception firebaseError) {
                System.out.println("⚠️ Firebase storage failed: " + firebaseError.getMessage());
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
     * Generate Firebase path based on questionnaire name and current date
     * Format: /questionnaires/{questionnaire_name}_{date}
     */
    private String generateFirebasePath(Questionnaire questionnaire) {
        // Extract questionnaire name from name (remove spaces, special chars)
        String questionnaireName = questionnaire.getName()
            .toLowerCase()
            .replaceAll("[^a-zA-Z0-9]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", ""); // Remove leading/trailing underscores

        // Get current date in format dd_MM_yyyy
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy"));

        return "/questionnaires/" + questionnaireName + "_" + currentDate;
    }

    /**
     * Get questionnaire response from Firebase
     */
    public Map<String, Object> getQuestionnaireResponse(String firebasePath, String firebaseUid) {
        try {
            DatabaseReference responseRef = firebaseDatabase.getReference(firebasePath + "/users/" + firebaseUid);
            
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            
            responseRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                        future.complete(data);
                    } else {
                        future.complete(null);
                    }
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    future.completeExceptionally(new RuntimeException("Failed to get response: " + databaseError.getMessage()));
                }
            });

            return future.get(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.err.println("Error retrieving questionnaire response: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all responses for a specific questionnaire path
     */
    public Map<String, Object> getAllResponsesForQuestionnaire(String firebasePath) {
        try {
            DatabaseReference questionnaireRef = firebaseDatabase.getReference(firebasePath);
            
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            
            questionnaireRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                        future.complete(data);
                    } else {
                        future.complete(null);
                    }
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    future.completeExceptionally(new RuntimeException("Failed to get responses: " + databaseError.getMessage()));
                }
            });

            return future.get(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.err.println("Error retrieving all responses: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete questionnaire response from Firebase
     */
    public void deleteQuestionnaireResponse(String firebasePath, String firebaseUid) {
        try {
            DatabaseReference responseRef = firebaseDatabase.getReference(firebasePath + "/users/" + firebaseUid);
            
            CompletableFuture<Void> future = new CompletableFuture<>();
            
            responseRef.removeValue((databaseError, databaseReference) -> {
                if (databaseError != null) {
                    future.completeExceptionally(new RuntimeException("Failed to delete response: " + databaseError.getMessage()));
                } else {
                    future.complete(null);
                }
            });

            future.get(10, TimeUnit.SECONDS);
            System.out.println("Questionnaire response deleted successfully: " + firebasePath + "/users/" + firebaseUid);

        } catch (Exception e) {
            System.err.println("Error deleting questionnaire response: " + e.getMessage());
            throw new RuntimeException("Failed to delete questionnaire response", e);
        }
    }
} 