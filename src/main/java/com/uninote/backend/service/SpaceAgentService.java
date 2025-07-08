package com.uninote.backend.service;

import com.uninote.backend.entity.Space;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.SpaceResource;
import com.uninote.backend.repository.SpaceResourceRepository;
import com.uninote.backend.repository.ResourceRepository;
import com.uninote.backend.dto.FileSelectionDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SpaceAgentService {

    @Autowired
    private SpaceResourceRepository spaceResourceRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${agentic.service.url:http://localhost:8001}")
    private String agenticServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get agentic file selection from Python service
     */
    public List<Resource> selectRelevantFiles(String userQuery, Long spaceId, int maxFiles) {
        try {
            // Prepare request for Python agentic service
            Map<String, Object> request = new HashMap<>();
            request.put("query", userQuery);
            request.put("space_id", spaceId.toString());
            request.put("max_files", maxFiles);

            // Call Python agentic service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                agenticServiceUrl + "/select-files",
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> selectedFiles = (List<Map<String, Object>>) responseBody.get("selected_files");
                
                // Convert to Resource objects
                List<Long> selectedIds = selectedFiles.stream()
                    .map(file -> Long.parseLong(file.get("id").toString()))
                    .collect(Collectors.toList());
                
                return resourceRepository.findAllById(selectedIds);
            }

        } catch (Exception e) {
            // Fallback to simple selection if Python service fails
            return fallbackFileSelection(userQuery, spaceId, maxFiles);
        }

        return new ArrayList<>();
    }

    /**
     * Get recommended files (cached)
     */
    @Cacheable(value = "spaceRecommendations", key = "#spaceId")
    public List<Resource> getRecommendedFiles(Long spaceId, int maxFiles) {
        Set<Long> resourceIds = new HashSet<>(spaceResourceRepository.findResourceIdsBySpaceId(spaceId));
        List<Resource> resources = resourceRepository.findAllById(resourceIds);
        
        // Sort by creation date (newest first)
        return resources.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(maxFiles)
            .collect(Collectors.toList());
    }

    /**
     * Get file suggestions with both AI selection and recommendations
     */
    public Map<String, Object> getFileSuggestions(String userQuery, Long spaceId, int maxFiles) {
        Map<String, Object> suggestions = new HashMap<>();
        
        try {
            // Get AI-selected files
            List<Resource> aiSelectedFiles = selectRelevantFiles(userQuery, spaceId, maxFiles);
            suggestions.put("aiSelectedFiles", convertToDTOs(aiSelectedFiles));
            
            // Get recommended files
            List<Resource> recommendedFiles = getRecommendedFiles(spaceId, maxFiles);
            suggestions.put("recommendedFiles", convertToDTOs(recommendedFiles));
            
            suggestions.put("query", userQuery);
            suggestions.put("spaceId", spaceId);
            
        } catch (Exception e) {
            // Fallback to just recommendations
            List<Resource> recommendedFiles = getRecommendedFiles(spaceId, maxFiles);
            suggestions.put("recommendedFiles", convertToDTOs(recommendedFiles));
            suggestions.put("error", "AI selection failed, showing recommendations only");
        }
        
        return suggestions;
    }

    /**
     * Check if Python agentic service is healthy
     */
    public boolean isAgenticServiceHealthy() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                agenticServiceUrl + "/health",
                Map.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clear cache when resources are updated
     */
    @CacheEvict(value = "spaceRecommendations", key = "#spaceId")
    public void clearRecommendationsCache(Long spaceId) {
        // Cache will be cleared automatically
    }

    /**
     * Fallback file selection when Python service is unavailable
     */
    private List<Resource> fallbackFileSelection(String userQuery, Long spaceId, int maxFiles) {
        Set<Long> resourceIds = new HashSet<>(spaceResourceRepository.findResourceIdsBySpaceId(spaceId));
        List<Resource> resources = resourceRepository.findAllById(resourceIds);
        
        // Simple keyword-based selection
        return resources.stream()
            .filter(resource -> {
                if (resource.getTitle() == null) return false;
                String title = resource.getTitle().toLowerCase();
                String query = userQuery.toLowerCase();
                return Arrays.stream(query.split("\\s+"))
                    .anyMatch(word -> word.length() > 3 && title.contains(word));
            })
            .limit(maxFiles)
            .collect(Collectors.toList());
    }

    /**
     * Convert Resource entities to DTOs
     */
    private List<FileSelectionDTO> convertToDTOs(List<Resource> resources) {
        return resources.stream()
            .map(resource -> new FileSelectionDTO(resource, 0.0)) // Default score
            .collect(Collectors.toList());
    }
} 