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

    /**
     * Enhanced RAG evaluation logging with comprehensive metrics
     */
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

        // Extract additional data from the additionalData map
        if (additionalData != null) {
            log.setChatUuid((String) additionalData.get("chatUuid"));
            log.setIsFirstMessage((Integer) additionalData.get("isFirstMessage"));
            log.setSessionId((String) additionalData.get("sessionId"));
            log.setUserAgent((String) additionalData.get("userAgent"));
            log.setIpAddress((String) additionalData.get("ipAddress"));
            log.setErrorOccurred((Integer) additionalData.get("errorOccurred"));
            log.setErrorMessage((String) additionalData.get("errorMessage"));
            log.setRetrievalTimeMs((Long) additionalData.get("retrievalTimeMs"));
            log.setGenerationTimeMs((Long) additionalData.get("generationTimeMs"));
            log.setModelName((String) additionalData.get("modelName"));
            log.setModelDeployment((String) additionalData.get("modelDeployment"));
            log.setTemperature((Double) additionalData.get("temperature"));
            log.setMaxTokens((Integer) additionalData.get("maxTokens"));
            log.setTopP((Double) additionalData.get("topP"));
            log.setFrequencyPenalty((Double) additionalData.get("frequencyPenalty"));
            log.setPresencePenalty((Double) additionalData.get("presencePenalty"));
            log.setInputTokens((Integer) additionalData.get("inputTokens"));
            log.setOutputTokens((Integer) additionalData.get("outputTokens"));
            log.setTotalTokens((Integer) additionalData.get("totalTokens"));
            log.setSystemPromptLength((Integer) additionalData.get("systemPromptLength"));
        }

        // Calculate chunk statistics
        if (retrievalChunks != null && !retrievalChunks.isEmpty()) {
            log.setChunkCount(retrievalChunks.size());
            
            // Extract unique resource IDs from chunks
            try {
                List<String> resourceIds = retrievalChunks.stream()
                    .map(chunk -> chunk.get("resource_id"))
                    .filter(id -> id != null)
                    .distinct()
                    .toList();
                log.setChunkSources(objectMapper.writeValueAsString(resourceIds));
            } catch (JsonProcessingException e) {
                log.setChunkSources("[]");
            }
        } else {
            log.setChunkCount(0);
            log.setChunkSources("[]");
        }

        // Calculate text lengths
        if (questionText != null) {
            log.setQuestionLength(questionText.length());
        }
        if (aiResponse != null) {
            log.setResponseLength(aiResponse.length());
        }

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

    /**
     * Enhanced RAG evaluation logging with all parameters
     */
    public RAGEvaluationLog logRAGEvaluationEnhanced(String promptVariant, String userId, String messageId,
                                                    String chatType, String questionText, String aiResponse,
                                                    List<Map<String, String>> retrievalChunks,
                                                    Map<String, Object> evaluationMetrics,
                                                    Double userRating, Long responseTimeMs,
                                                    String chatUuid, Boolean isFirstMessage,
                                                    String sessionId, String userAgent, String ipAddress,
                                                    Boolean errorOccurred, String errorMessage,
                                                    Long retrievalTimeMs, Long generationTimeMs,
                                                    String modelName, String modelDeployment,
                                                    Double temperature, Integer maxTokens,
                                                    Double topP, Double frequencyPenalty, Double presencePenalty,
                                                    Integer inputTokens, Integer outputTokens, Integer totalTokens,
                                                    Integer systemPromptLength) {
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("chatUuid", chatUuid);
        additionalData.put("isFirstMessage", isFirstMessage);
        additionalData.put("sessionId", sessionId);
        additionalData.put("userAgent", userAgent);
        additionalData.put("ipAddress", ipAddress);
        additionalData.put("errorOccurred", errorOccurred);
        additionalData.put("errorMessage", errorMessage);
        additionalData.put("retrievalTimeMs", retrievalTimeMs);
        additionalData.put("generationTimeMs", generationTimeMs);
        additionalData.put("modelName", modelName);
        additionalData.put("modelDeployment", modelDeployment);
        additionalData.put("temperature", temperature);
        additionalData.put("maxTokens", maxTokens);
        additionalData.put("topP", topP);
        additionalData.put("frequencyPenalty", frequencyPenalty);
        additionalData.put("presencePenalty", presencePenalty);
        additionalData.put("inputTokens", inputTokens);
        additionalData.put("outputTokens", outputTokens);
        additionalData.put("totalTokens", totalTokens);
        additionalData.put("systemPromptLength", systemPromptLength);

        return logRAGEvaluation(promptVariant, userId, messageId, chatType, questionText, aiResponse,
                               retrievalChunks, evaluationMetrics, userRating, responseTimeMs, additionalData);
    }

    /**
     * Simple RAG evaluation logging (backward compatibility)
     */
    public RAGEvaluationLog logSimpleRAGEvaluation(String promptVariant, String userId, String messageId,
                                                  String chatType, String questionText, String aiResponse,
                                                  Long responseTimeMs) {
        return logRAGEvaluation(promptVariant, userId, messageId, chatType, questionText, aiResponse,
                               null, null, null, responseTimeMs, null);
    }

    /**
     * Update user rating for an existing evaluation
     */
    public boolean updateUserRating(String messageId, Double userRating) {
        RAGEvaluationLog log = ragEvaluationLogRepository.findByMessageId(messageId);
        if (log != null) {
            log.setUserRating(userRating);
            ragEvaluationLogRepository.save(log);
            return true;
        }
        return false;
    }

    /**
     * Update evaluation metrics for an existing evaluation
     */
    public boolean updateEvaluationMetrics(String messageId, Map<String, Object> evaluationMetrics) {
        RAGEvaluationLog log = ragEvaluationLogRepository.findByMessageId(messageId);
        if (log != null) {
            try {
                log.setEvaluationMetrics(objectMapper.writeValueAsString(evaluationMetrics));
                ragEvaluationLogRepository.save(log);
                return true;
            } catch (JsonProcessingException e) {
                System.err.println("Error serializing evaluation metrics: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    // Existing analytics methods
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

    
    private Integer convertToOracleBoolean(Boolean bool) {
        if (bool == null) {
            return null;
        }
        return bool ? 1 : 0;
    }
} 