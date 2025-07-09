package com.uninote.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.dto.QuestionnaireContentDTO;
import com.uninote.backend.dto.QuestionnaireDTO;
import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.entity.QuestionnaireStatus;
import com.uninote.backend.repository.QuestionnaireRepository;
import com.uninote.backend.repository.UserRepository;

@Service
public class QuestionnaireService {
    
    @Autowired
    private QuestionnaireRepository questionnaireRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Create a new questionnaire with content stored in database only
     */
    @Transactional
    public QuestionnaireDTO createQuestionnaire(QuestionnaireDTO questionnaireDTO, QuestionnaireContentDTO contentDTO) {
        try {
            // Create questionnaire entity
            Questionnaire questionnaire = new Questionnaire(questionnaireDTO.getName(), questionnaireDTO.getDescription());
            questionnaire.setStatus(QuestionnaireStatus.ACTIVE);
            questionnaire.setTriggerTime(questionnaireDTO.getTriggerTime());
            
            // Store complete questionnaire JSON in database
            if (contentDTO != null) {
                questionnaire.setQuestionnaireJson(objectMapper.writeValueAsString(contentDTO));
            }
            
            // Store criteria query if provided
            if (questionnaireDTO.getParameters() != null && questionnaireDTO.getParameters().containsKey("criteriaQuery")) {
                questionnaire.setCriteriaQuery((String) questionnaireDTO.getParameters().get("criteriaQuery"));
            }
            
            // Save to database
            Questionnaire savedQuestionnaire = questionnaireRepository.save(questionnaire);
            
            return convertToDTO(savedQuestionnaire);
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create questionnaire", e);
        }
    }
    
    /**
     * Create a questionnaire with JSON stored only in database (no Firebase)
     */
    @Transactional
    public QuestionnaireDTO createQuestionnaireFromJson(QuestionnaireDTO questionnaireDTO, String questionnaireJson, String criteriaQuery) {
        try {
            // Create questionnaire entity
            Questionnaire questionnaire = new Questionnaire(questionnaireDTO.getName(), questionnaireDTO.getDescription());
            questionnaire.setStatus(QuestionnaireStatus.ACTIVE);
            questionnaire.setTriggerTime(questionnaireDTO.getTriggerTime());
            
            // Store complete questionnaire JSON in database
            questionnaire.setQuestionnaireJson(questionnaireJson);
            
            // Store criteria query
            questionnaire.setCriteriaQuery(criteriaQuery);
            
            // Save to database
            Questionnaire savedQuestionnaire = questionnaireRepository.save(questionnaire);
            
            return convertToDTO(savedQuestionnaire);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create questionnaire", e);
        }
    }
    
    /**
     * Get questionnaire by ID with content from database or Firebase
     */
    public QuestionnaireDTO getQuestionnaireById(Long id) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(id);
        if (questionnaireOpt.isPresent()) {
            Questionnaire questionnaire = questionnaireOpt.get();
            return convertToDTO(questionnaire);
        }
        return null;
    }
    
    /**
     * Get questionnaire content from database JSON
     */
    public QuestionnaireContentDTO getQuestionnaireContent(Long questionnaireId) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(questionnaireId);
        if (questionnaireOpt.isPresent()) {
            Questionnaire questionnaire = questionnaireOpt.get();
            
            // Get content from database JSON
            if (questionnaire.getQuestionnaireJson() != null) {
                try {
                    return objectMapper.readValue(questionnaire.getQuestionnaireJson(), QuestionnaireContentDTO.class);
                } catch (JsonProcessingException e) {
                    // Silent error handling
                }
            }
        }
        return null;
    }
    
    /**
     * Get all active questionnaires
     */
    public List<QuestionnaireDTO> getAllActiveQuestionnaires() {
        List<Questionnaire> questionnaires = questionnaireRepository.findByStatus(QuestionnaireStatus.ACTIVE);
        return questionnaires.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Update questionnaire metadata
     */
    @Transactional
    public QuestionnaireDTO updateQuestionnaire(Long id, QuestionnaireDTO questionnaireDTO) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(id);
        if (questionnaireOpt.isPresent()) {
            Questionnaire questionnaire = questionnaireOpt.get();
            
            questionnaire.setName(questionnaireDTO.getName());
            questionnaire.setDescription(questionnaireDTO.getDescription());
            questionnaire.setStatus(questionnaireDTO.getStatus());
            questionnaire.setTriggerTime(questionnaireDTO.getTriggerTime());
            
            // Update criteria query if provided
            if (questionnaireDTO.getParameters() != null && questionnaireDTO.getParameters().containsKey("criteriaQuery")) {
                questionnaire.setCriteriaQuery((String) questionnaireDTO.getParameters().get("criteriaQuery"));
            }
            
            Questionnaire updatedQuestionnaire = questionnaireRepository.save(questionnaire);
            return convertToDTO(updatedQuestionnaire);
        }
        return null;
    }
    
    /**
     * Update questionnaire JSON in database
     */
    @Transactional
    public void updateQuestionnaireJson(Long questionnaireId, String questionnaireJson) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(questionnaireId);
        if (questionnaireOpt.isPresent()) {
            Questionnaire questionnaire = questionnaireOpt.get();
            questionnaire.setQuestionnaireJson(questionnaireJson);
            questionnaireRepository.save(questionnaire);
        }
    }
    
    /**
     * Delete questionnaire
     */
    @Transactional
    public void deleteQuestionnaire(Long id) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(id);
        if (questionnaireOpt.isPresent()) {
            Questionnaire questionnaire = questionnaireOpt.get();
            
            // Delete from database
            questionnaireRepository.delete(questionnaire);
        }
    }
    
    /**
     * Get questionnaires ready to trigger
     */
    public List<QuestionnaireDTO> getQuestionnairesReadyToTrigger() {
        List<Questionnaire> questionnaires = questionnaireRepository.findReadyToTrigger(QuestionnaireStatus.ACTIVE, LocalDateTime.now());
        return questionnaires.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get eligible users for a questionnaire based on criteria
     */
    public List<Long> getEligibleUsers(Long questionnaireId) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(questionnaireId);
        if (questionnaireOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        Questionnaire questionnaire = questionnaireOpt.get();
        String criteriaQuery = questionnaire.getCriteriaQuery();
        
        if (criteriaQuery == null || criteriaQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // Use the criteria service to get eligible user IDs
            // This would need to be implemented in QuestionnaireCriteriaService
            return new ArrayList<>(); // Placeholder - implement based on your criteria logic
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Convert entity to DTO
     */
    private QuestionnaireDTO convertToDTO(Questionnaire questionnaire) {
        QuestionnaireDTO dto = new QuestionnaireDTO();
        dto.setId(questionnaire.getId());
        dto.setName(questionnaire.getName());
        dto.setDescription(questionnaire.getDescription());
        dto.setCreatedAt(questionnaire.getCreatedAt());
        dto.setTriggerTime(questionnaire.getTriggerTime());
        dto.setStatus(questionnaire.getStatus());
        
        // Parse questionnaire JSON if exists
        if (questionnaire.getQuestionnaireJson() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> questionnaireData = objectMapper.readValue(questionnaire.getQuestionnaireJson(), Map.class);
                dto.setParameters(questionnaireData);
            } catch (JsonProcessingException e) {
                // Silent error handling
            }
        }
        
        return dto;
    }
} 