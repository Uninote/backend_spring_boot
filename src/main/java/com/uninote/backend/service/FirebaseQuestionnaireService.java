package com.uninote.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.uninote.backend.dto.QuestionnaireChoiceDTO;
import com.uninote.backend.dto.QuestionnaireContentDTO;
import com.uninote.backend.dto.QuestionnaireQuestionDTO;

@Service
public class FirebaseQuestionnaireService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseQuestionnaireService.class);
    private static final String COLLECTION_NAME = "questionnaires";
    
    private final Firestore firestore;
    
    public FirebaseQuestionnaireService() {
        this.firestore = FirestoreClient.getFirestore();
    }
    
    /**
     * Store questionnaire content in Firebase
     */
    public String storeQuestionnaireContent(QuestionnaireContentDTO content) {
        try {
            // Convert DTO to Map for Firebase storage
            Map<String, Object> documentData = convertContentToMap(content);
            
            // Generate a unique document ID if not provided
            String documentId = content.getQuestionnaireId();
            if (documentId == null || documentId.isEmpty()) {
                documentId = generateDocumentId();
            }
            
            // Store in Firebase
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(documentId);
            ApiFuture<WriteResult> result = docRef.set(documentData);
            
            // Wait for the write to complete
            WriteResult writeResult = result.get();
            logger.info("Questionnaire content stored successfully with ID: {}", documentId);
            
            return documentId;
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error storing questionnaire content in Firebase", e);
            throw new RuntimeException("Failed to store questionnaire content", e);
        }
    }
    
    /**
     * Retrieve questionnaire content from Firebase
     */
    public QuestionnaireContentDTO getQuestionnaireContent(String documentId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(documentId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            
            if (document.exists()) {
                Map<String, Object> data = document.getData();
                return convertMapToContent(data, documentId);
            } else {
                logger.warn("Questionnaire document not found: {}", documentId);
                return null;
            }
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error retrieving questionnaire content from Firebase", e);
            throw new RuntimeException("Failed to retrieve questionnaire content", e);
        }
    }
    
    /**
     * Update questionnaire content in Firebase
     */
    public void updateQuestionnaireContent(String documentId, QuestionnaireContentDTO content) {
        try {
            Map<String, Object> documentData = convertContentToMap(content);
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(documentId);
            ApiFuture<WriteResult> result = docRef.set(documentData, SetOptions.merge());
            
            WriteResult writeResult = result.get();
            logger.info("Questionnaire content updated successfully: {}", documentId);
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error updating questionnaire content in Firebase", e);
            throw new RuntimeException("Failed to update questionnaire content", e);
        }
    }
    
    /**
     * Delete questionnaire content from Firebase
     */
    public void deleteQuestionnaireContent(String documentId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(documentId);
            ApiFuture<WriteResult> result = docRef.delete();
            
            WriteResult writeResult = result.get();
            logger.info("Questionnaire content deleted successfully: {}", documentId);
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error deleting questionnaire content from Firebase", e);
            throw new RuntimeException("Failed to delete questionnaire content", e);
        }
    }
    
    /**
     * Get all questionnaires from Firebase
     */
    public List<QuestionnaireContentDTO> getAllQuestionnaires() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            QuerySnapshot querySnapshot = future.get();
            
            List<QuestionnaireContentDTO> questionnaires = new ArrayList<>();
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                QuestionnaireContentDTO content = convertMapToContent(document.getData(), document.getId());
                questionnaires.add(content);
            }
            
            return questionnaires;
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error retrieving all questionnaires from Firebase", e);
            throw new RuntimeException("Failed to retrieve questionnaires", e);
        }
    }
    
    /**
     * Convert QuestionnaireContentDTO to Map for Firebase storage
     */
    private Map<String, Object> convertContentToMap(QuestionnaireContentDTO content) {
        Map<String, Object> data = new HashMap<>();
        data.put("questionnaireId", content.getQuestionnaireId());
        data.put("title", content.getTitle());
        data.put("description", content.getDescription());
        data.put("questionnaireType", content.getQuestionnaireType());
        data.put("version", content.getVersion());
        data.put("settings", content.getSettings());
        data.put("styling", content.getStyling());
        data.put("logic", content.getLogic());
        
        // Convert questions
        List<Map<String, Object>> questionsList = new ArrayList<>();
        if (content.getQuestions() != null) {
            for (QuestionnaireQuestionDTO question : content.getQuestions()) {
                questionsList.add(convertQuestionToMap(question));
            }
        }
        data.put("questions", questionsList);
        
        return data;
    }
    
    /**
     * Convert QuestionnaireQuestionDTO to Map
     */
    private Map<String, Object> convertQuestionToMap(QuestionnaireQuestionDTO question) {
        Map<String, Object> questionMap = new HashMap<>();
        questionMap.put("questionId", question.getQuestionId());
        questionMap.put("questionText", question.getQuestionText());
        questionMap.put("questionType", question.getQuestionType());
        questionMap.put("order", question.getOrder());
        questionMap.put("isRequired", question.getIsRequired());
        questionMap.put("isVisible", question.getIsVisible());
        questionMap.put("options", question.getOptions());
        questionMap.put("validation", question.getValidation());
        questionMap.put("conditionalLogic", question.getConditionalLogic());
        questionMap.put("styling", question.getStyling());
        questionMap.put("helpText", question.getHelpText());
        questionMap.put("placeholder", question.getPlaceholder());
        
        // Convert choices
        List<Map<String, Object>> choicesList = new ArrayList<>();
        if (question.getChoices() != null) {
            for (QuestionnaireChoiceDTO choice : question.getChoices()) {
                choicesList.add(convertChoiceToMap(choice));
            }
        }
        questionMap.put("choices", choicesList);
        
        return questionMap;
    }
    
    /**
     * Convert QuestionnaireChoiceDTO to Map
     */
    private Map<String, Object> convertChoiceToMap(QuestionnaireChoiceDTO choice) {
        Map<String, Object> choiceMap = new HashMap<>();
        choiceMap.put("choiceId", choice.getChoiceId());
        choiceMap.put("choiceText", choice.getChoiceText());
        choiceMap.put("choiceValue", choice.getChoiceValue());
        choiceMap.put("order", choice.getOrder());
        choiceMap.put("isCorrect", choice.getIsCorrect());
        choiceMap.put("metadata", choice.getMetadata());
        choiceMap.put("styling", choice.getStyling());
        return choiceMap;
    }
    
    /**
     * Convert Map from Firebase to QuestionnaireContentDTO
     */
    private QuestionnaireContentDTO convertMapToContent(Map<String, Object> data, String documentId) {
        QuestionnaireContentDTO content = new QuestionnaireContentDTO();
        content.setQuestionnaireId((String) data.get("questionnaireId"));
        content.setTitle((String) data.get("title"));
        content.setDescription((String) data.get("description"));
        content.setQuestionnaireType((String) data.get("questionnaireType"));
        content.setVersion((String) data.get("version"));
        content.setSettings((Map<String, Object>) data.get("settings"));
        content.setStyling((Map<String, Object>) data.get("styling"));
        content.setLogic((Map<String, Object>) data.get("logic"));
        
        // Convert questions
        List<Map<String, Object>> questionsData = (List<Map<String, Object>>) data.get("questions");
        List<QuestionnaireQuestionDTO> questions = new ArrayList<>();
        if (questionsData != null) {
            for (Map<String, Object> questionData : questionsData) {
                questions.add(convertMapToQuestion(questionData));
            }
        }
        content.setQuestions(questions);
        
        return content;
    }
    
    /**
     * Convert Map to QuestionnaireQuestionDTO
     */
    private QuestionnaireQuestionDTO convertMapToQuestion(Map<String, Object> questionData) {
        QuestionnaireQuestionDTO question = new QuestionnaireQuestionDTO();
        question.setQuestionId((String) questionData.get("questionId"));
        question.setQuestionText((String) questionData.get("questionText"));
        question.setQuestionType((String) questionData.get("questionType"));
        question.setOrder((Integer) questionData.get("order"));
        question.setIsRequired((Boolean) questionData.get("isRequired"));
        question.setIsVisible((Boolean) questionData.get("isVisible"));
        question.setOptions((Map<String, Object>) questionData.get("options"));
        question.setValidation((Map<String, Object>) questionData.get("validation"));
        question.setConditionalLogic((Map<String, Object>) questionData.get("conditionalLogic"));
        question.setStyling((Map<String, Object>) questionData.get("styling"));
        question.setHelpText((String) questionData.get("helpText"));
        question.setPlaceholder((String) questionData.get("placeholder"));
        
        // Convert choices
        List<Map<String, Object>> choicesData = (List<Map<String, Object>>) questionData.get("choices");
        List<QuestionnaireChoiceDTO> choices = new ArrayList<>();
        if (choicesData != null) {
            for (Map<String, Object> choiceData : choicesData) {
                choices.add(convertMapToChoice(choiceData));
            }
        }
        question.setChoices(choices);
        
        return question;
    }
    
    /**
     * Convert Map to QuestionnaireChoiceDTO
     */
    private QuestionnaireChoiceDTO convertMapToChoice(Map<String, Object> choiceData) {
        QuestionnaireChoiceDTO choice = new QuestionnaireChoiceDTO();
        choice.setChoiceId((String) choiceData.get("choiceId"));
        choice.setChoiceText((String) choiceData.get("choiceText"));
        choice.setChoiceValue((String) choiceData.get("choiceValue"));
        choice.setOrder((Integer) choiceData.get("order"));
        choice.setIsCorrect((Boolean) choiceData.get("isCorrect"));
        choice.setMetadata((Map<String, Object>) choiceData.get("metadata"));
        choice.setStyling((Map<String, Object>) choiceData.get("styling"));
        return choice;
    }
    
    /**
     * Generate a unique document ID
     */
    private String generateDocumentId() {
        return "questionnaire_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
} 