package com.uninote.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.QuestionnaireCriteria;
import com.uninote.backend.repository.QuestionnaireCriteriaRepository;

@Service
public class QuestionnaireCriteriaService {
    @Autowired
    private QuestionnaireCriteriaRepository criteriaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ExpressionParser parser = new SpelExpressionParser();

    public List<QuestionnaireCriteria> getCriteriaForQuestionnaire(Long questionnaireId) {
        return criteriaRepository.findByQuestionnaireId(questionnaireId);
    }

    public boolean matchesCriteria(QuestionnaireCriteria criteria, Object user) {
        String expression = criteria.getExpression();
        
        // Check if this is a SQL query (starts with SELECT, INSERT, etc.)
        if (isSqlQuery(expression)) {
            return evaluateSqlQuery(expression, user);
        } else {
            // Use SpEL for regular expressions
            return evaluateSpelExpression(expression, user);
        }
    }

    /**
     * Check if the expression is a SQL query
     */
    private boolean isSqlQuery(String expression) {
        if (expression == null) return false;
        String trimmed = expression.trim().toUpperCase();
        return trimmed.startsWith("SELECT") || 
               trimmed.startsWith("WITH") ||
               trimmed.startsWith("EXISTS") ||
               trimmed.startsWith("COUNT") ||
               trimmed.startsWith("CASE");
    }

    /**
     * Evaluate a SQL query directly in the database
     */
    private boolean evaluateSqlQuery(String sqlQuery, Object user) {
        try {
            // Replace placeholders with actual values
            String processedQuery = processSqlPlaceholders(sqlQuery, user);
            
            // Execute the query
            Integer result = jdbcTemplate.queryForObject(processedQuery, Integer.class);
            
            // Return true if result is not null and greater than 0
            return result != null && result > 0;
            
        } catch (Exception e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
            return false;
        }
    }

    /**
     * Process SQL placeholders with user data
     */
    private String processSqlPlaceholders(String sqlQuery, Object user) {
        String processed = sqlQuery;
        
        // Replace user placeholders
        if (user instanceof com.uninote.backend.entity.User) {
            com.uninote.backend.entity.User userEntity = (com.uninote.backend.entity.User) user;
            processed = processed.replace("${user.id}", String.valueOf(userEntity.getId()));
            processed = processed.replace("${user.streak}", String.valueOf(userEntity.getStreak()));
            processed = processed.replace("${user.uniscore}", String.valueOf(userEntity.getUniscore()));
            processed = processed.replace("${user.stripeCustomerId}", 
                userEntity.getStripeCustomerId() != null ? "'" + userEntity.getStripeCustomerId() + "'" : "NULL");
            processed = processed.replace("${user.emailVerified}", String.valueOf(userEntity.getEmailVerified()));
            processed = processed.replace("${user.certified}", String.valueOf(userEntity.getCertified()));
            
            if (userEntity.getDepartment() != null) {
                processed = processed.replace("${user.departmentId}", String.valueOf(userEntity.getDepartment().getId()));
            }
            if (userEntity.getUniversity() != null) {
                processed = processed.replace("${user.universityId}", String.valueOf(userEntity.getUniversity().getId()));
            }
        }
        
        return processed;
    }

    /**
     * Evaluate a SpEL expression
     */
    private boolean evaluateSpelExpression(String expression, Object user) {
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("user", user);
        Boolean result = parser.parseExpression(expression).getValue(context, Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    /**
     * Evaluate a criteria query from the questionnaires table
     */
    public boolean evaluateCriteriaQuery(String criteriaQuery, Object user) {
        try {
            // Replace placeholders with actual values
            String processedQuery = processSqlPlaceholders(criteriaQuery, user);
            
            // Execute the query
            Integer result = jdbcTemplate.queryForObject(processedQuery, Integer.class);
            
            // Return true if result is not null and greater than 0
            return result != null && result > 0;
            
        } catch (Exception e) {
            System.err.println("Error executing criteria query: " + e.getMessage());
            return false;
        }
    }
} 