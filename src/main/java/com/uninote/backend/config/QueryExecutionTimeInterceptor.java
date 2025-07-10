package com.uninote.backend.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class QueryExecutionTimeInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionTimeInterceptor.class);
    private static final Logger queryLogger = LoggerFactory.getLogger("QUERY_PERFORMANCE");
    
    private final ConcurrentHashMap<String, AtomicLong> queryCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> queryTotalTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> queryMaxTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> queryMinTime = new ConcurrentHashMap<>();
    
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong slowRequests = new AtomicLong(0);
    private final AtomicLong verySlowRequests = new AtomicLong(0);

    @PostConstruct
    public void init() {
        logger.info("QueryExecutionTimeInterceptor initialized - Statistics will be logged every 5 minutes");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute("startTime");
        if (startTime != null) {
            long executionTime = System.currentTimeMillis() - startTime;
            String endpoint = request.getRequestURI();
            
            totalRequests.incrementAndGet();
            
            // Log slow requests (> 1000ms)
            if (executionTime > 1000) {
                slowRequests.incrementAndGet();
                logger.warn("Slow request detected - Endpoint: {}, Execution time: {}ms, Method: {}", 
                    endpoint, executionTime, request.getMethod());
            }
            
            // Log very slow requests (> 5000ms)
            if (executionTime > 5000) {
                verySlowRequests.incrementAndGet();
                logger.error("Very slow request detected - Endpoint: {}, Execution time: {}ms, Method: {}, User-Agent: {}", 
                    endpoint, executionTime, request.getMethod(), request.getHeader("User-Agent"));
            }
        }
    }

    /**
     * Log individual query execution time
     * Call this method from your repository or service layer
     */
    public void logQueryExecution(String queryType, String queryName, long executionTimeMs, String additionalInfo) {
        String key = queryType + ":" + queryName;
        
        // Update counters and timing statistics
        queryCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        queryTotalTime.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(executionTimeMs);
        
        queryMaxTime.merge(key, executionTimeMs, Math::max);
        queryMinTime.merge(key, executionTimeMs, Math::min);
        
        // Log based on execution time thresholds
        if (executionTimeMs > 5000) {
            queryLogger.error("VERY SLOW QUERY - Type: {}, Name: {}, Time: {}ms, Info: {}", 
                queryType, queryName, executionTimeMs, additionalInfo);
        } else if (executionTimeMs > 1000) {
            queryLogger.warn("SLOW QUERY - Type: {}, Name: {}, Time: {}ms, Info: {}", 
                queryType, queryName, executionTimeMs, additionalInfo);
        } else if (executionTimeMs > 500) {
            queryLogger.info("MODERATE QUERY - Type: {}, Name: {}, Time: {}ms, Info: {}", 
                queryType, queryName, executionTimeMs, additionalInfo);
        } else {
            queryLogger.debug("FAST QUERY - Type: {}, Name: {}, Time: {}ms, Info: {}", 
                queryType, queryName, executionTimeMs, additionalInfo);
        }
    }

    /**
     * Scheduled task to log query statistics every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    public void logQueryStatisticsScheduled() {
        if (!queryCounters.isEmpty()) {
            logQueryStatistics();
        }
    }

    /**
     * Scheduled task to log request statistics every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes = 600,000 milliseconds
    public void logRequestStatisticsScheduled() {
        long total = totalRequests.get();
        long slow = slowRequests.get();
        long verySlow = verySlowRequests.get();
        
        if (total > 0) {
            double slowPercentage = (double) slow / total * 100;
            double verySlowPercentage = (double) verySlow / total * 100;
            
            logger.info("=== REQUEST STATISTICS (Last 10 minutes) ===");
            logger.info("Total Requests: {}", total);
            logger.info("Slow Requests (>1000ms): {} ({}%)", slow, String.format("%.2f", slowPercentage));
            logger.info("Very Slow Requests (>5000ms): {} ({}%)", verySlow, String.format("%.2f", verySlowPercentage));
            logger.info("=== END REQUEST STATISTICS ===");
            
            // Reset counters after logging
            totalRequests.set(0);
            slowRequests.set(0);
            verySlowRequests.set(0);
        }
    }

    /**
     * Get query performance statistics
     */
    public void logQueryStatistics() {
        queryLogger.info("=== QUERY PERFORMANCE STATISTICS ===");
        
        queryCounters.forEach((key, count) -> {
            long totalTime = queryTotalTime.get(key).get();
            long avgTime = totalTime / count.get();
            long maxTime = queryMaxTime.get(key);
            long minTime = queryMinTime.get(key);
            
            queryLogger.info("Query: {} | Count: {} | Avg: {}ms | Min: {}ms | Max: {}ms | Total: {}ms", 
                key, count.get(), avgTime, minTime, maxTime, totalTime);
        });
        
        queryLogger.info("=== END QUERY STATISTICS ===");
    }

    /**
     * Reset query statistics
     */
    public void resetQueryStatistics() {
        queryCounters.clear();
        queryTotalTime.clear();
        queryMaxTime.clear();
        queryMinTime.clear();
        queryLogger.info("Query performance statistics reset");
    }
} 