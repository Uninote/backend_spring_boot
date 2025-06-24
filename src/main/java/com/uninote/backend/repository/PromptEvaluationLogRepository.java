package com.uninote.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.PromptEvaluationLog;

public interface PromptEvaluationLogRepository extends JpaRepository<PromptEvaluationLog, Long> {
    
    List<PromptEvaluationLog> findByPromptVariant(String promptVariant);
    
    List<PromptEvaluationLog> findByUserId(String userId);
    
    List<PromptEvaluationLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT p.promptVariant, COUNT(p) as count " +
           "FROM PromptEvaluationLog p " +
           "WHERE p.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY p.promptVariant")
    List<Object[]> getUsageCountByVariant(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p.promptVariant, COUNT(p) as count " +
           "FROM PromptEvaluationLog p " +
           "WHERE p.userId = :userId " +
           "GROUP BY p.promptVariant")
    List<Object[]> getUsageCountByUser(@Param("userId") String userId);
} 