package com.uninote.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.dto.QuestionnaireContentDTO;
import com.uninote.backend.dto.QuestionnaireDTO;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.QuestionnaireService;

@RestController
@RequestMapping("/questionnaires")
public class QuestionnaireController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireController.class);

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private QuestionnaireWebSocketController webSocketController;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new questionnaire
     */
    @PostMapping
    public ResponseEntity<QuestionnaireDTO> createQuestionnaire(
            @RequestBody Map<String, Object> request) {
        logger.debug("=== Creating new questionnaire ===");
        logger.debug("Request body: {}", request);
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized attempt to create questionnaire");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
            String userUid = firebaseAuth.getUid();
            logger.debug("Creating questionnaire for user: {}", userUid);
            
            User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Extract questionnaire metadata and content from request
            @SuppressWarnings("unchecked")
            Map<String, Object> questionnaireData = (Map<String, Object>) request.get("questionnaire");
            @SuppressWarnings("unchecked")
            Map<String, Object> contentData = (Map<String, Object>) request.get("content");

            logger.debug("Questionnaire data: {}", questionnaireData);
            logger.debug("Content data: {}", contentData);

            // Convert to DTOs (you might want to use ObjectMapper for proper conversion)
            QuestionnaireDTO questionnaireDTO = convertMapToQuestionnaireDTO(questionnaireData);
            QuestionnaireContentDTO contentDTO = convertMapToContentDTO(contentData);

            logger.debug("Converted to DTOs - questionnaire: {}, content: {}", 
                questionnaireDTO.getName(), contentDTO != null ? contentDTO.getTitle() : "null");

            // Create questionnaire
            QuestionnaireDTO createdQuestionnaire = questionnaireService.createQuestionnaire(questionnaireDTO, contentDTO);
            logger.debug("Successfully created questionnaire with ID: {}", createdQuestionnaire.getId());

            // Send via WebSocket to target audience (if needed)
            // webSocketController.sendQuestionnaireToTargetAudience(createdQuestionnaire, contentDTO);

            logger.debug("=== Completed questionnaire creation ===");
            return ResponseEntity.ok(createdQuestionnaire);

        } catch (Exception e) {
            logger.error("Error creating questionnaire: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get questionnaire by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getQuestionnaire(@PathVariable Long id) {
        logger.debug("Getting questionnaire by ID: {}", id);
        
        try {
            QuestionnaireDTO questionnaire = questionnaireService.getQuestionnaireById(id);
            if (questionnaire == null) {
                logger.warn("Questionnaire not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            QuestionnaireContentDTO content = questionnaireService.getQuestionnaireContent(id);
            logger.debug("Retrieved questionnaire: {} - {}", questionnaire.getId(), questionnaire.getName());
            logger.debug("Content available: {}", content != null);

            Map<String, Object> response = Map.of(
                "questionnaire", questionnaire,
                "content", content
            );

            logger.debug("Successfully retrieved questionnaire: {}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving questionnaire {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all active questionnaires
     */
    @GetMapping
    public ResponseEntity<List<QuestionnaireDTO>> getAllQuestionnaires() {
        logger.debug("Getting all active questionnaires");
        
        try {
            List<QuestionnaireDTO> questionnaires = questionnaireService.getAllActiveQuestionnaires();
            logger.debug("Found {} active questionnaires", questionnaires.size());
            logger.debug("Questionnaire IDs: {}", questionnaires.stream().map(q -> q.getId()).collect(Collectors.toList()));
            
            return ResponseEntity.ok(questionnaires);

        } catch (Exception e) {
            logger.error("Error retrieving questionnaires: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    

    /**
     * Update questionnaire
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuestionnaireDTO> updateQuestionnaire(
            @PathVariable Long id, @RequestBody QuestionnaireDTO questionnaireDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            QuestionnaireDTO updatedQuestionnaire = questionnaireService.updateQuestionnaire(id, questionnaireDTO);
            if (updatedQuestionnaire == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(updatedQuestionnaire);

        } catch (Exception e) {
            logger.error("Error updating questionnaire", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update questionnaire content
     */
    @PutMapping("/{id}/content")
    public ResponseEntity<String> updateQuestionnaireContent(
            @PathVariable Long id, @RequestBody QuestionnaireContentDTO contentDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            QuestionnaireDTO questionnaire = questionnaireService.getQuestionnaireById(id);
            if (questionnaire == null) {
                return ResponseEntity.notFound().build();
            }

            // Convert contentDTO to JSON string and update in database
            ObjectMapper objectMapper = new ObjectMapper();
            String questionnaireJson = objectMapper.writeValueAsString(contentDTO);
            questionnaireService.updateQuestionnaireJson(id, questionnaireJson);

            return ResponseEntity.ok("Questionnaire content updated successfully");

        } catch (Exception e) {
            logger.error("Error updating questionnaire content", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete questionnaire
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestionnaire(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            questionnaireService.deleteQuestionnaire(id);
            return ResponseEntity.ok("Questionnaire deleted successfully");

        } catch (Exception e) {
            logger.error("Error deleting questionnaire", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Send questionnaire reminder
     */
    @PostMapping("/{id}/remind/{userId}")
    public ResponseEntity<String> sendReminder(@PathVariable Long id, @PathVariable Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            QuestionnaireDTO questionnaire = questionnaireService.getQuestionnaireById(id);
            if (questionnaire == null) {
                return ResponseEntity.notFound().build();
            }

            webSocketController.sendQuestionnaireReminder(userId.toString(), questionnaire);
            return ResponseEntity.ok("Reminder sent successfully");

        } catch (Exception e) {
            logger.error("Error sending reminder", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods for converting Map to DTOs (simplified - you'd use ObjectMapper in production)
    private QuestionnaireDTO convertMapToQuestionnaireDTO(Map<String, Object> data) {
        QuestionnaireDTO dto = new QuestionnaireDTO();
        dto.setId(data.get("id") != null ? Long.valueOf(data.get("id").toString()) : null);
        dto.setName((String) data.get("name"));
        dto.setDescription((String) data.get("description"));
        dto.setFirebasePath((String) data.get("firebasePath"));
        dto.setStatus(data.get("status") != null ? com.uninote.backend.entity.QuestionnaireStatus.valueOf(data.get("status").toString()) : null);
        dto.setTriggerTime(data.get("triggerTime") != null ? java.time.LocalDateTime.parse(data.get("triggerTime").toString()) : null);
        // Parameters as Map
        if (data.get("parameters") instanceof Map) {
            dto.setParameters((Map<String, Object>) data.get("parameters"));
        }
        return dto;
    }

    private QuestionnaireContentDTO convertMapToContentDTO(Map<String, Object> data) {
        QuestionnaireContentDTO dto = new QuestionnaireContentDTO();
        dto.setTitle((String) data.get("title"));
        dto.setDescription((String) data.get("description"));
        dto.setQuestionnaireType((String) data.get("questionnaireType"));
        dto.setVersion((String) data.get("version"));
        @SuppressWarnings("unchecked")
        Map<String, Object> settings = (Map<String, Object>) data.get("settings");
        dto.setSettings(settings);
        @SuppressWarnings("unchecked")
        Map<String, Object> styling = (Map<String, Object>) data.get("styling");
        dto.setStyling(styling);
        @SuppressWarnings("unchecked")
        Map<String, Object> logic = (Map<String, Object>) data.get("logic");
        dto.setLogic(logic);
        // Note: Questions would need more complex conversion
        return dto;
    }
} 