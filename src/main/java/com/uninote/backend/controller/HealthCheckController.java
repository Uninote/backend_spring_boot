package com.uninote.backend.controller;

import com.uninote.backend.service.SpaceAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SpaceAgentService spaceAgentService;

    /**
     * Comprehensive health check endpoint
     * Checks database connectivity, external services, and application status
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        boolean overallHealthy = true;
        
        // Basic application info
        healthStatus.put("service", "UniNote Backend");
        healthStatus.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        healthStatus.put("version", "1.0-SNAPSHOT");
        
        // Database health check
        Map<String, Object> databaseStatus = checkDatabaseHealth();
        healthStatus.put("database", databaseStatus);
        if (!(Boolean) databaseStatus.get("healthy")) {
            overallHealthy = false;
        }
        
        // External services health check
        Map<String, Object> externalServicesStatus = checkExternalServicesHealth();
        healthStatus.put("external_services", externalServicesStatus);
        if (!(Boolean) externalServicesStatus.get("healthy")) {
            overallHealthy = false;
        }
        
        // Application metrics
        Map<String, Object> applicationMetrics = getApplicationMetrics();
        healthStatus.put("application", applicationMetrics);
        
        // Overall status
        healthStatus.put("healthy", overallHealthy);
        healthStatus.put("status", overallHealthy ? "UP" : "DOWN");
        
        HttpStatus httpStatus = overallHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        
        logger.info("Health check completed - Status: {}", overallHealthy ? "HEALTHY" : "UNHEALTHY");
        
        return new ResponseEntity<>(healthStatus, httpStatus);
    }

    /**
     * Quick health check endpoint for load balancers
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "pong");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check with timeout
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        // Set timeout for health checks
        CompletableFuture<Map<String, Object>> databaseCheck = CompletableFuture
            .supplyAsync(this::checkDatabaseHealth)
            .orTimeout(5, TimeUnit.SECONDS);
            
        CompletableFuture<Map<String, Object>> externalServicesCheck = CompletableFuture
            .supplyAsync(this::checkExternalServicesHealth)
            .orTimeout(10, TimeUnit.SECONDS);
        
        try {
            Map<String, Object> databaseStatus = databaseCheck.get();
            Map<String, Object> externalServicesStatus = externalServicesCheck.get();
            
            healthStatus.put("database", databaseStatus);
            healthStatus.put("external_services", externalServicesStatus);
            healthStatus.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            healthStatus.put("healthy", (Boolean) databaseStatus.get("healthy") && (Boolean) externalServicesStatus.get("healthy"));
            
            return ResponseEntity.ok(healthStatus);
        } catch (Exception e) {
            logger.error("Health check timeout or error", e);
            healthStatus.put("error", "Health check timeout or error: " + e.getMessage());
            healthStatus.put("healthy", false);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(healthStatus);
        }
    }

    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> status = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // 5 second timeout
            status.put("healthy", isValid);
            status.put("connection_pool", "available");
            status.put("response_time_ms", System.currentTimeMillis());
            
            if (isValid) {
                status.put("message", "Database connection is healthy");
            } else {
                status.put("message", "Database connection is invalid");
            }
            
        } catch (SQLException e) {
            logger.error("Database health check failed", e);
            status.put("healthy", false);
            status.put("error", e.getMessage());
            status.put("message", "Database connection failed");
        }
        
        return status;
    }

    private Map<String, Object> checkExternalServicesHealth() {
        Map<String, Object> status = new HashMap<>();
        Map<String, Object> services = new HashMap<>();
        boolean allHealthy = true;
        
        // Check Python agentic service
        try {
            boolean agenticHealthy = spaceAgentService.isAgenticServiceHealthy();
            services.put("python_agentic_service", Map.of(
                "healthy", agenticHealthy,
                "endpoint", "/health",
                "message", agenticHealthy ? "Service is responding" : "Service is not responding"
            ));
            if (!agenticHealthy) {
                allHealthy = false;
            }
        } catch (Exception e) {
            logger.error("Python agentic service health check failed", e);
            services.put("python_agentic_service", Map.of(
                "healthy", false,
                "error", e.getMessage(),
                "message", "Service check failed"
            ));
            allHealthy = false;
        }
        
        // Add more external service checks here as needed
        // Example: Azure OpenAI, Pinecone, etc.
        
        status.put("healthy", allHealthy);
        status.put("services", services);
        status.put("message", allHealthy ? "All external services are healthy" : "Some external services are unhealthy");
        
        return status;
    }

    private Map<String, Object> getApplicationMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Basic JVM metrics
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        metrics.put("memory", Map.of(
            "used_mb", usedMemory / (1024 * 1024),
            "free_mb", freeMemory / (1024 * 1024),
            "total_mb", totalMemory / (1024 * 1024),
            "max_mb", maxMemory / (1024 * 1024),
            "usage_percentage", (double) usedMemory / maxMemory * 100
        ));
        
        metrics.put("threads", Map.of(
            "active_count", Thread.activeCount(),
            "peak_count", Thread.activeCount() // You could track peak separately
        ));
        
        metrics.put("uptime_ms", System.currentTimeMillis());
        
        return metrics;
    }
} 