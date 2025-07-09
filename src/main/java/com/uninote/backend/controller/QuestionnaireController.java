package com.uninote.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
import com.uninote.backend.entity.QuestionnaireStatus;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.QuestionnaireService;

@RestController
@RequestMapping("/questionnaires")
public class QuestionnaireController {

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
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
            String userUid = firebaseAuth.getUid();
            
            User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Extract questionnaire metadata and content from request
            @SuppressWarnings("unchecked")
            Map<String, Object> questionnaireData = (Map<String, Object>) request.get("questionnaire");
            @SuppressWarnings("unchecked")
            Map<String, Object> contentData = (Map<String, Object>) request.get("content");

            // Convert to DTOs (you might want to use ObjectMapper for proper conversion)
            QuestionnaireDTO questionnaireDTO = convertMapToQuestionnaireDTO(questionnaireData);
            QuestionnaireContentDTO contentDTO = convertMapToContentDTO(contentData);

            // Create questionnaire
            QuestionnaireDTO createdQuestionnaire = questionnaireService.createQuestionnaire(questionnaireDTO, contentDTO);

            // Send via WebSocket to target audience (if needed)
            // webSocketController.sendQuestionnaireToTargetAudience(createdQuestionnaire, contentDTO);

            return ResponseEntity.ok(createdQuestionnaire);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get questionnaire by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getQuestionnaire(@PathVariable Long id) {
        
        try {
            QuestionnaireDTO questionnaire = questionnaireService.getQuestionnaireById(id);
            if (questionnaire == null) {
                return ResponseEntity.notFound().build();
            }

            QuestionnaireContentDTO content = questionnaireService.getQuestionnaireContent(id);

            Map<String, Object> response = Map.of(
                "questionnaire", questionnaire,
                "content", content
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all active questionnaires
     */
    @GetMapping
    public ResponseEntity<List<QuestionnaireDTO>> getAllQuestionnaires() {
        
        try {
            List<QuestionnaireDTO> questionnaires = questionnaireService.getAllActiveQuestionnaires();
            
            return ResponseEntity.ok(questionnaires);

        } catch (Exception e) {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods for converting Map to DTOs (simplified - you'd use ObjectMapper in production)
    private QuestionnaireDTO convertMapToQuestionnaireDTO(Map<String, Object> data) {
        QuestionnaireDTO dto = new QuestionnaireDTO();
        dto.setId(data.get("id") != null ? Long.valueOf(data.get("id").toString()) : null);
        dto.setName((String) data.get("name"));
        dto.setDescription((String) data.get("description"));
        dto.setStatus(QuestionnaireStatus.ACTIVE);
        dto.setTriggerTime(LocalDateTime.now());
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