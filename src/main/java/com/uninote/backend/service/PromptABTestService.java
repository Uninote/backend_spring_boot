package com.uninote.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.PromptEvaluationLog;
import com.uninote.backend.entity.PromptVariant;
import com.uninote.backend.repository.PromptEvaluationLogRepository;
import com.uninote.backend.repository.PromptVariantRepository;

@Service
public class PromptABTestService {

    @Autowired
    private PromptVariantRepository promptVariantRepository;

    @Autowired
    private PromptEvaluationLogRepository evaluationLogRepository;

    private final Random random = new Random();

    /**
     * Get a random prompt variant for all chat types
     * @return Randomly selected prompt variant
     */
    public PromptVariant getRandomPromptVariant() {
        List<PromptVariant> activeVariants = promptVariantRepository.findByActiveTrue();
        
        if (activeVariants.isEmpty()) {
            throw new RuntimeException("No active prompt variants found");
        }

        int index = random.nextInt(activeVariants.size());
        return activeVariants.get(index);
    }

    /**
     * Get a random prompt variant for a specific chat type (DEPRECATED - use getRandomPromptVariant() instead)
     * @param chatType The type of chat (simple, resource, space)
     * @return Randomly selected prompt variant for the chat type
     * @deprecated Use getRandomPromptVariant() for unified prompt approach
     */
    @Deprecated
    public PromptVariant getRandomPromptVariantForChatType(String chatType) {
        // For backward compatibility, but now just returns a random variant
        return getRandomPromptVariant();
    }

    /**
     * Get a specific prompt variant by name
     * @param variantName The name of the variant
     * @return Optional containing the prompt variant
     */
    public Optional<PromptVariant> getPromptVariant(String variantName) {
        return promptVariantRepository.findByName(variantName);
    }

    /**
     * Log which variant was used for a message
     * @param promptVariant The variant name used
     * @param userId User identifier
     * @param messageId Message identifier
     * @param requestId Request identifier
     * @param additionalData Additional JSON data
     */
    public void logVariantUsage(String promptVariant, String userId, String messageId, 
                               String requestId, String additionalData) {
        PromptEvaluationLog log = new PromptEvaluationLog(promptVariant, userId, messageId, requestId);
        log.setAdditionalData(additionalData);
        
        evaluationLogRepository.save(log);
    }

    /**
     * Get usage statistics by variant
     * @param startDate Start of the time range
     * @param endDate End of the time range
     * @return List of usage counts by variant
     */
    public List<Object[]> getUsageStatistics(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return evaluationLogRepository.getUsageCountByVariant(startDate, endDate);
    }

    /**
     * Get usage statistics by user
     * @param userId User identifier
     * @return List of usage counts by variant for the user
     */
    public List<Object[]> getUserUsageStatistics(String userId) {
        return evaluationLogRepository.getUsageCountByUser(userId);
    }

    /**
     * Get usage statistics by chat type
     * @param chatType The chat type to filter by
     * @param startDate Start of the time range
     * @param endDate End of the time range
     * @return List of usage counts by variant for the chat type
     */
    public List<Object[]> getUsageStatisticsByChatType(String chatType, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        // This would require a custom query in the repository
        // For now, return general statistics
        return getUsageStatistics(startDate, endDate);
    }

    /**
     * Create a new prompt variant
     * @param name Variant name
     * @param description Description
     * @param content Prompt content
     * @return The created prompt variant
     */
    public PromptVariant createPromptVariant(String name, String description, String content) {
        PromptVariant variant = new PromptVariant();
        variant.setName(name);
        variant.setDescription(description);
        variant.setContent(content);
        variant.setActive(true);
        
        return promptVariantRepository.save(variant);
    }

    /**
     * Update a prompt variant
     * @param id Variant ID
     * @param name New name
     * @param description New description
     * @param content New content
     * @param active Whether the variant is active
     * @return The updated prompt variant
     */
    public Optional<PromptVariant> updatePromptVariant(Long id, String name, String description, 
                                                      String content, boolean active) {
        Optional<PromptVariant> optional = promptVariantRepository.findById(id);
        if (optional.isPresent()) {
            PromptVariant variant = optional.get();
            variant.setName(name);
            variant.setDescription(description);
            variant.setContent(content);
            variant.setActive(active);
            
            return Optional.of(promptVariantRepository.save(variant));
        }
        return Optional.empty();
    }

    /**
     * Get all active prompt variants
     * @return List of active variants
     */
    public List<PromptVariant> getActiveVariants() {
        return promptVariantRepository.findByActiveTrue();
    }

    /**
     * Get all prompt variants (active and inactive)
     * @return List of all variants
     */
    public List<PromptVariant> getAllVariants() {
        return promptVariantRepository.findAll();
    }
} 