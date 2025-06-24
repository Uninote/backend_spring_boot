package com.uninote.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.entity.PromptVariant;
import com.uninote.backend.service.PromptABTestService;

@RestController
@RequestMapping("/api/prompt-variants")
@CrossOrigin(origins = "*")
public class PromptABTestController {

    @Autowired
    private PromptABTestService promptABTestService;

    /**
     * Get a random prompt variant
     */
    @GetMapping("/random")
    public ResponseEntity<Map<String, Object>> getRandomPromptVariant() {
        
        try {
            PromptVariant variant = promptABTestService.getRandomPromptVariant();
            
            Map<String, Object> response = new HashMap<>();
            response.put("variantName", variant.getName());
            response.put("variantContent", variant.getContent());
            response.put("variantDescription", variant.getDescription());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Log which variant was used for a message
     */
    @PostMapping("/log-usage")
    public ResponseEntity<Map<String, Object>> logVariantUsage(
            @RequestParam String promptVariant,
            @RequestParam(required = false) String userId,
            @RequestParam String messageId,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String additionalData) {
        
        try {
            promptABTestService.logVariantUsage(promptVariant, userId, messageId, requestId, additionalData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Variant usage logged successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get usage statistics
     */
    @GetMapping("/usage-statistics")
    public ResponseEntity<Map<String, Object>> getUsageStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<Object[]> usageStats = promptABTestService.getUsageStatistics(startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("usageStatistics", usageStats);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get usage statistics for a specific user
     */
    @GetMapping("/user-usage/{userId}")
    public ResponseEntity<Map<String, Object>> getUserUsageStatistics(@PathVariable String userId) {
        
        try {
            List<Object[]> userStats = promptABTestService.getUserUsageStatistics(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userUsageStatistics", userStats);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Create a new prompt variant
     */
    @PostMapping("/variants")
    public ResponseEntity<Map<String, Object>> createPromptVariant(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String content) {
        
        try {
            PromptVariant variant = promptABTestService.createPromptVariant(name, description, content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", variant.getId());
            response.put("name", variant.getName());
            response.put("description", variant.getDescription());
            response.put("active", variant.isActive());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update a prompt variant
     */
    @PutMapping("/variants/{id}")
    public ResponseEntity<Map<String, Object>> updatePromptVariant(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String content,
            @RequestParam(defaultValue = "true") boolean active) {
        
        try {
            Optional<PromptVariant> optional = promptABTestService.updatePromptVariant(id, name, description, content, active);
            
            if (optional.isPresent()) {
                PromptVariant variant = optional.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", variant.getId());
                response.put("name", variant.getName());
                response.put("description", variant.getDescription());
                response.put("active", variant.isActive());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Prompt variant not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get all active variants
     */
    @GetMapping("/variants")
    public ResponseEntity<Map<String, Object>> getActiveVariants() {
        try {
            List<PromptVariant> variants = promptABTestService.getActiveVariants();
            
            Map<String, Object> response = new HashMap<>();
            response.put("variants", variants);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get all variants (active and inactive)
     */
    @GetMapping("/variants/all")
    public ResponseEntity<Map<String, Object>> getAllVariants() {
        try {
            List<PromptVariant> variants = promptABTestService.getAllVariants();
            
            Map<String, Object> response = new HashMap<>();
            response.put("variants", variants);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get a specific variant by name
     */
    @GetMapping("/variants/{name}")
    public ResponseEntity<Map<String, Object>> getPromptVariant(@PathVariable String name) {
        try {
            Optional<PromptVariant> optional = promptABTestService.getPromptVariant(name);
            
            if (optional.isPresent()) {
                PromptVariant variant = optional.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", variant.getId());
                response.put("name", variant.getName());
                response.put("description", variant.getDescription());
                response.put("content", variant.getContent());
                response.put("active", variant.isActive());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 