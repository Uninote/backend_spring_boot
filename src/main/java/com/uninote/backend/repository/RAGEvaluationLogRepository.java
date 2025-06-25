package com.uninote.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.RAGEvaluationLog;

public interface RAGEvaluationLogRepository extends JpaRepository<RAGEvaluationLog, Long> {
    
    // Find by prompt variant
    List<RAGEvaluationLog> findByPromptVariant(String promptVariant);
    
    // Find by user ID
    List<RAGEvaluationLog> findByUserId(String userId);
    
    // Find by chat type
    List<RAGEvaluationLog> findByChatType(String chatType);
    
    // Find by message ID
    RAGEvaluationLog findByMessageId(String messageId);
    
    // Find by timestamp range
    List<RAGEvaluationLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    // Find by prompt variant and timestamp range
    List<RAGEvaluationLog> findByPromptVariantAndTimestampBetween(String promptVariant, LocalDateTime start, LocalDateTime end);
    
    // Find by chat type and timestamp range
    List<RAGEvaluationLog> findByChatTypeAndTimestampBetween(String chatType, LocalDateTime start, LocalDateTime end);
    
    // Find rated evaluations (where user_rating is not null)
    List<RAGEvaluationLog> findByUserRatingIsNotNull();
    
    // Find by prompt variant with ratings
    List<RAGEvaluationLog> findByPromptVariantAndUserRatingIsNotNull(String promptVariant);
    
    // Custom queries for analytics
    
    // Get average ratings by prompt variant
    @Query("SELECT r.promptVariant, AVG(r.userRating) as avgRating, COUNT(r) as totalCount " +
           "FROM RAGEvaluationLog r " +
           "WHERE r.userRating IS NOT NULL " +
           "AND r.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY r.promptVariant " +
           "ORDER BY avgRating DESC")
    List<Object[]> getAverageRatingsByVariant(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    // Get average response times by prompt variant
    @Query("SELECT r.promptVariant, AVG(r.responseTimeMs) as avgResponseTime, COUNT(r) as totalCount " +
           "FROM RAGEvaluationLog r " +
           "WHERE r.responseTimeMs IS NOT NULL " +
           "AND r.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY r.promptVariant " +
           "ORDER BY avgResponseTime ASC")
    List<Object[]> getAverageResponseTimesByVariant(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Get performance by chat type
    @Query("SELECT r.chatType, r.promptVariant, AVG(r.userRating) as avgRating, " +
           "AVG(r.responseTimeMs) as avgResponseTime, COUNT(r) as totalCount " +
           "FROM RAGEvaluationLog r " +
           "WHERE r.userRating IS NOT NULL AND r.responseTimeMs IS NOT NULL " +
           "AND r.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY r.chatType, r.promptVariant " +
           "ORDER BY r.chatType, avgRating DESC")
    List<Object[]> getPerformanceByChatType(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    // Get daily performance trends
    @Query("SELECT YEAR(r.timestamp) as year, MONTH(r.timestamp) as month, DAY(r.timestamp) as day, " +
           "r.promptVariant, AVG(r.userRating) as avgRating, COUNT(r) as totalCount " +
           "FROM RAGEvaluationLog r " +
           "WHERE r.userRating IS NOT NULL " +
           "AND r.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(r.timestamp), MONTH(r.timestamp), DAY(r.timestamp), r.promptVariant " +
           "ORDER BY year DESC, month DESC, day DESC, avgRating DESC")
    List<Object[]> getDailyPerformanceTrends(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    // Get top performing variants
    @Query("SELECT r.promptVariant, AVG(r.userRating) as avgRating, COUNT(r) as totalCount " +
           "FROM RAGEvaluationLog r " +
           "WHERE r.userRating IS NOT NULL " +
           "AND r.timestamp >= :since " +
           "GROUP BY r.promptVariant " +
           "HAVING COUNT(r) >= :minCount " +
           "ORDER BY avgRating DESC")
    List<Object[]> getTopPerformingVariants(@Param("since") LocalDateTime since, 
                                           @Param("minCount") Long minCount);
} 