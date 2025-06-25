package com.uninote.backend.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.entity.RAGEvaluationLog;
import com.uninote.backend.repository.RAGEvaluationLogRepository;

@Service
public class RAGEvaluationService {

    @Autowired
    private RAGEvaluationLogRepository ragEvaluationLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

  
    public RAGEvaluationLog logRAGEvaluation(String promptVariant, String userId, String messageId,
                                            String chatType, String questionText, String aiResponse,
                                            List<Map<String, String>> retrievalChunks,
                                            Map<String, Object> evaluationMetrics,
                                            Double userRating, Long responseTimeMs,
                                            Map<String, Object> additionalData) {
        
        RAGEvaluationLog log = new RAGEvaluationLog(promptVariant, messageId);
        log.setUserId(userId);
        log.setChatType(chatType);
        log.setQuestionText(questionText);
        log.setAiResponse(aiResponse);
        log.setUserRating(userRating);
        log.setResponseTimeMs(responseTimeMs);

        try {
            if (retrievalChunks != null) {
                log.setRetrievalChunks(objectMapper.writeValueAsString(retrievalChunks));
            }
            if (evaluationMetrics != null) {
                log.setEvaluationMetrics(objectMapper.writeValueAsString(evaluationMetrics));
            }
            if (additionalData != null) {
                log.setAdditionalData(objectMapper.writeValueAsString(additionalData));
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing JSON for RAG evaluation log: " + e.getMessage());
        }

        return ragEvaluationLogRepository.save(log);
    }

   
    public RAGEvaluationLog logSimpleRAGEvaluation(String promptVariant, String userId, String messageId,
                                                  String chatType, String questionText, String aiResponse,
                                                  Long responseTimeMs) {
        return logRAGEvaluation(promptVariant, userId, messageId, chatType, questionText, aiResponse,
                               null, null, null, responseTimeMs, null);
    }

    
    public boolean updateUserRating(String messageId, Double userRating) {
        RAGEvaluationLog log = ragEvaluationLogRepository.findByMessageId(messageId);
        if (log != null) {
            log.setUserRating(userRating);
            ragEvaluationLogRepository.save(log);
            return true;
        }
        return false;
    }

    public List<Object[]> getAverageRatingsByVariant(LocalDateTime startDate, LocalDateTime endDate) {
        return ragEvaluationLogRepository.getAverageRatingsByVariant(startDate, endDate);
    }

   
    public List<Object[]> getAverageResponseTimesByVariant(LocalDateTime startDate, LocalDateTime endDate) {
        return ragEvaluationLogRepository.getAverageResponseTimesByVariant(startDate, endDate);
    }

    
    public List<Object[]> getPerformanceByChatType(LocalDateTime startDate, LocalDateTime endDate) {
        return ragEvaluationLogRepository.getPerformanceByChatType(startDate, endDate);
    }

   
    public List<Object[]> getDailyPerformanceTrends(LocalDateTime startDate, LocalDateTime endDate) {
        return ragEvaluationLogRepository.getDailyPerformanceTrends(startDate, endDate);
    }

    public List<Object[]> getTopPerformingVariants(LocalDateTime since, Long minCount) {
        return ragEvaluationLogRepository.getTopPerformingVariants(since, minCount);
    }

   
    public Map<String, Object> getAnalyticsSummary(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> summary = new HashMap<>();
        
        List<Object[]> ratings = getAverageRatingsByVariant(startDate, endDate);
        summary.put("averageRatingsByVariant", ratings);
        
        List<Object[]> responseTimes = getAverageResponseTimesByVariant(startDate, endDate);
        summary.put("averageResponseTimesByVariant", responseTimes);
        
        List<Object[]> chatTypePerformance = getPerformanceByChatType(startDate, endDate);
        summary.put("performanceByChatType", chatTypePerformance);
        
        List<Object[]> dailyTrends = getDailyPerformanceTrends(startDate, endDate);
        summary.put("dailyPerformanceTrends", dailyTrends);
        
        List<RAGEvaluationLog> allLogs = ragEvaluationLogRepository.findByTimestampBetween(startDate, endDate);
        summary.put("totalEvaluations", allLogs.size());
        
        long ratedCount = allLogs.stream().filter(log -> log.getUserRating() != null).count();
        summary.put("ratedEvaluations", ratedCount);
        summary.put("ratingRate", allLogs.size() > 0 ? (double) ratedCount / allLogs.size() : 0.0);
        
        return summary;
    }

   
    public List<RAGEvaluationLog> getEvaluationsByVariant(String promptVariant, LocalDateTime startDate, LocalDateTime endDate) {
        return ragEvaluationLogRepository.findByPromptVariantAndTimestampBetween(promptVariant, startDate, endDate);
    }

   
    public List<RAGEvaluationLog> getEvaluationsByChatType(String chatType, LocalDateTime startDate, LocalDateTime endDate) {
        return ragEvaluationLogRepository.findByChatTypeAndTimestampBetween(chatType, startDate, endDate);
    }

    
    public List<RAGEvaluationLog> getEvaluationsByUser(String userId) {
        return ragEvaluationLogRepository.findByUserId(userId);
    }

   
    public List<RAGEvaluationLog> getRecentEvaluations(int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        List<RAGEvaluationLog> recent = ragEvaluationLogRepository.findByTimestampBetween(weekAgo, LocalDateTime.now());
        return recent.size() > limit ? recent.subList(0, limit) : recent;
    }
} 