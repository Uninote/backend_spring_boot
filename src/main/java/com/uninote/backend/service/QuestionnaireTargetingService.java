package com.uninote.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.Questionnaire;
import com.uninote.backend.repository.QuestionnaireRepository;

@Service
public class QuestionnaireTargetingService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireTargetingService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private QuestionnaireRepository questionnaireRepository;
    
    /**
     * Get users who match the criteria query for a questionnaire
     */
    public List<Long> getTargetUsers(Long questionnaireId) {
        try {
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId).orElse(null);
            if (questionnaire == null || questionnaire.getCriteriaQuery() == null) {
                return List.of(); // No criteria, no target users
            }
            
            String criteriaQuery = questionnaire.getCriteriaQuery().trim();
            if (criteriaQuery.isEmpty()) {
                return List.of();
            }
            
            // Execute the criteria query to get target user IDs
            List<Long> targetUserIds = jdbcTemplate.queryForList(criteriaQuery, Long.class);
            logger.info("Found {} target users for questionnaire {}", targetUserIds.size(), questionnaireId);
            return targetUserIds;
            
        } catch (Exception e) {
            logger.error("Error executing criteria query for questionnaire {}: {}", questionnaireId, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Check if a specific user matches the criteria
     */
    public boolean userMatchesCriteria(Long userId, Long questionnaireId) {
        try {
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId).orElse(null);
            if (questionnaire == null || questionnaire.getCriteriaQuery() == null) {
                return false;
            }
            
            String criteriaQuery = questionnaire.getCriteriaQuery().trim();
            if (criteriaQuery.isEmpty()) {
                return false;
            }
            
            // Replace ${user.id} placeholder with actual user ID
            String processedQuery = criteriaQuery.replace("${user.id}", String.valueOf(userId));
            
            Integer result = jdbcTemplate.queryForObject(processedQuery, Integer.class);
            boolean matches = result != null && result > 0;
            
            logger.debug("User {} matches criteria for questionnaire {}: {}", userId, questionnaireId, matches);
            return matches;
            
        } catch (Exception e) {
            logger.error("Error checking criteria for user {} and questionnaire {}: {}", userId, questionnaireId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all questionnaires a user is eligible for
     */
    public List<Questionnaire> getEligibleQuestionnairesForUser(Long userId) {
        try {
            List<Questionnaire> allActiveQuestionnaires = questionnaireRepository.findByStatus(com.uninote.backend.entity.QuestionnaireStatus.ACTIVE);
            
            return allActiveQuestionnaires.stream()
                    .filter(questionnaire -> userMatchesCriteria(userId, questionnaire.getId()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Error getting eligible questionnaires for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Execute a custom criteria query with user data
     */
    public boolean executeCustomCriteria(String criteriaQuery, Long userId) {
        try {
            if (criteriaQuery == null || criteriaQuery.trim().isEmpty()) {
                return false;
            }
            
            // Replace user placeholders
            String processedQuery = processUserPlaceholders(criteriaQuery, userId);
            
            Integer result = jdbcTemplate.queryForObject(processedQuery, Integer.class);
            return result != null && result > 0;
            
        } catch (Exception e) {
            logger.error("Error executing custom criteria query: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Process user placeholders in SQL queries
     */
    private String processUserPlaceholders(String query, Long userId) {
        String processed = query;
        
        // Replace common user placeholders
        processed = processed.replace("${user.id}", String.valueOf(userId));
        
        // You can add more user data placeholders here
        // processed = processed.replace("${user.departmentId}", getDepartmentId(userId));
        // processed = processed.replace("${user.universityId}", getUniversityId(userId));
        
        return processed;
    }
} 