package com.uninote.backend.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.entity.RAGEvaluationLog;
import com.uninote.backend.service.RAGEvaluationService;

@RestController
@RequestMapping("/api/evaluation")
@CrossOrigin(origins = "*")
public class LLMEvaluationController {

    @Autowired
    private RAGEvaluationService ragEvaluationService;

    /**
     * Get comprehensive analytics summary for a date range
     */
    @GetMapping("/analytics/summary")
    public ResponseEntity<Map<String, Object>> getAnalyticsSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            Map<String, Object> summary = ragEvaluationService.getAnalyticsSummary(startDate, endDate);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get analytics summary: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get average ratings by prompt variant
     */
    @GetMapping("/analytics/ratings-by-variant")
    public ResponseEntity<List<Object[]>> getAverageRatingsByVariant(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<Object[]> ratings = ragEvaluationService.getAverageRatingsByVariant(startDate, endDate);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get average response times by prompt variant
     */
    @GetMapping("/analytics/response-times-by-variant")
    public ResponseEntity<List<Object[]>> getAverageResponseTimesByVariant(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<Object[]> responseTimes = ragEvaluationService.getAverageResponseTimesByVariant(startDate, endDate);
            return ResponseEntity.ok(responseTimes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get performance by chat type
     */
    @GetMapping("/analytics/performance-by-chat-type")
    public ResponseEntity<List<Object[]>> getPerformanceByChatType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<Object[]> performance = ragEvaluationService.getPerformanceByChatType(startDate, endDate);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get daily performance trends
     */
    @GetMapping("/analytics/daily-trends")
    public ResponseEntity<List<Object[]>> getDailyPerformanceTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<Object[]> trends = ragEvaluationService.getDailyPerformanceTrends(startDate, endDate);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get top performing variants
     */
    @GetMapping("/analytics/top-performing-variants")
    public ResponseEntity<List<Object[]>> getTopPerformingVariants(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @RequestParam(defaultValue = "10") Long minCount) {
        
        try {
            List<Object[]> topVariants = ragEvaluationService.getTopPerformingVariants(since, minCount);
            return ResponseEntity.ok(topVariants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get evaluations by prompt variant
     */
    @GetMapping("/evaluations/by-variant/{variant}")
    public ResponseEntity<List<RAGEvaluationLog>> getEvaluationsByVariant(
            @PathVariable String variant,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<RAGEvaluationLog> evaluations = ragEvaluationService.getEvaluationsByVariant(variant, startDate, endDate);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get evaluations by chat type
     */
    @GetMapping("/evaluations/by-chat-type/{chatType}")
    public ResponseEntity<List<RAGEvaluationLog>> getEvaluationsByChatType(
            @PathVariable String chatType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<RAGEvaluationLog> evaluations = ragEvaluationService.getEvaluationsByChatType(chatType, startDate, endDate);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get evaluations by user
     */
    @GetMapping("/evaluations/by-user/{userId}")
    public ResponseEntity<List<RAGEvaluationLog>> getEvaluationsByUser(@PathVariable String userId) {
        try {
            List<RAGEvaluationLog> evaluations = ragEvaluationService.getEvaluationsByUser(userId);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get recent evaluations
     */
    @GetMapping("/evaluations/recent")
    public ResponseEntity<List<RAGEvaluationLog>> getRecentEvaluations(
            @RequestParam(defaultValue = "50") int limit) {
        
        try {
            List<RAGEvaluationLog> evaluations = ragEvaluationService.getRecentEvaluations(limit);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update user rating for a specific message
     */
    @PostMapping("/rating/update")
    public ResponseEntity<Map<String, Object>> updateUserRating(
            @RequestParam String messageId,
            @RequestParam Double rating) {
        
        try {
            boolean success = ragEvaluationService.updateUserRating(messageId, rating);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Rating updated successfully" : "Message not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to update rating: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get evaluation statistics for dashboard
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(defaultValue = "7") int days) {
        
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            
            Map<String, Object> stats = new HashMap<>();
            
            // Get basic analytics
            Map<String, Object> analytics = ragEvaluationService.getAnalyticsSummary(startDate, endDate);
            stats.putAll(analytics);
            
            // Get recent evaluations
            List<RAGEvaluationLog> recentEvaluations = ragEvaluationService.getRecentEvaluations(10);
            stats.put("recentEvaluations", recentEvaluations);
            
            // Get top performing variants
            List<Object[]> topVariants = ragEvaluationService.getTopPerformingVariants(startDate, 5L);
            stats.put("topPerformingVariants", topVariants);
            
            // Get performance by chat type
            List<Object[]> chatTypePerformance = ragEvaluationService.getPerformanceByChatType(startDate, endDate);
            stats.put("chatTypePerformance", chatTypePerformance);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get dashboard stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Export evaluation data for analysis
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportEvaluationData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "1000") int limit) {
        
        try {
            // Get all evaluations in the date range
            List<RAGEvaluationLog> evaluations = ragEvaluationService.getEvaluationsByVariant("all", startDate, endDate);
            
            // Limit the results
            if (evaluations.size() > limit) {
                evaluations = evaluations.subList(0, limit);
            }
            
            Map<String, Object> export = new HashMap<>();
            export.put("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            export.put("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME));
            export.put("endDate", endDate.format(DateTimeFormatter.ISO_DATE_TIME));
            export.put("totalRecords", evaluations.size());
            export.put("evaluations", evaluations);
            
            return ResponseEntity.ok(export);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to export evaluation data: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        health.put("service", "LLM Evaluation Service");
        return ResponseEntity.ok(health);
    }
} 