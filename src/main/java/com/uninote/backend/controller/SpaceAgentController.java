package com.uninote.backend.controller;

import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.Space;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.SpaceRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.SpaceAgentService;
import com.uninote.backend.dto.FileSelectionDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/spaces/agent")
public class SpaceAgentController {

    @Autowired
    private SpaceAgentService spaceAgentService;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get agentic file recommendations for a space based on user query
     */
    @PostMapping("/{spaceUuid}/select-files")
    public ResponseEntity<?> selectRelevantFiles(
            @PathVariable String spaceUuid,
            @RequestBody Map<String, Object> request) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
            String userUid = firebaseAuth.getUid();
            User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify user owns the space
            Space space = spaceRepository.findByUuidAndUser_FirebaseUid(spaceUuid, userUid)
                .orElseThrow(() -> new RuntimeException("Space not found or access denied"));

            String userQuery = (String) request.get("query");
            Integer maxFiles = (Integer) request.getOrDefault("maxFiles", 5);

            if (userQuery == null || userQuery.trim().isEmpty()) {
                return new ResponseEntity<>("Query is required", HttpStatus.BAD_REQUEST);
            }

            List<Resource> selectedFiles = spaceAgentService.selectRelevantFiles(
                userQuery, space.getId(), maxFiles);

            List<FileSelectionDTO> fileDTOs = FileSelectionDTO.fromResources(selectedFiles);

            Map<String, Object> response = new HashMap<>();
            response.put("selectedFiles", fileDTOs);
            response.put("query", userQuery);
            response.put("spaceId", space.getId());
            response.put("totalSelected", selectedFiles.size());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error selecting files: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get recommended files for a space (based on recency and activity)
     */
    @GetMapping("/{spaceUuid}/recommended-files")
    public ResponseEntity<?> getRecommendedFiles(
            @PathVariable String spaceUuid,
            @RequestParam(defaultValue = "5") int maxFiles) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
            String userUid = firebaseAuth.getUid();

            // Verify user owns the space
            Space space = spaceRepository.findByUuidAndUser_FirebaseUid(spaceUuid, userUid)
                .orElseThrow(() -> new RuntimeException("Space not found or access denied"));

            List<Resource> recommendedFiles = spaceAgentService.getRecommendedFiles(
                space.getId(), maxFiles);

            List<FileSelectionDTO> fileDTOs = FileSelectionDTO.fromResources(recommendedFiles);

            Map<String, Object> response = new HashMap<>();
            response.put("recommendedFiles", fileDTOs);
            response.put("spaceId", space.getId());
            response.put("totalRecommended", recommendedFiles.size());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error getting recommended files: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get file selection suggestions for a space (combines AI selection with recommendations)
     */
    @PostMapping("/{spaceUuid}/suggestions")
    public ResponseEntity<?> getFileSuggestions(
            @PathVariable String spaceUuid,
            @RequestBody Map<String, Object> request) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
            String userUid = firebaseAuth.getUid();

            // Verify user owns the space
            Space space = spaceRepository.findByUuidAndUser_FirebaseUid(spaceUuid, userUid)
                .orElseThrow(() -> new RuntimeException("Space not found or access denied"));

            String userQuery = (String) request.get("query");
            Integer maxFiles = (Integer) request.getOrDefault("maxFiles", 5);

            Map<String, Object> suggestions = spaceAgentService.getFileSuggestions(
                userQuery, space.getId(), maxFiles);

            return new ResponseEntity<>(suggestions, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error getting file suggestions: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if the Python agentic service is healthy
     */
    @GetMapping("/health")
    public ResponseEntity<?> checkAgenticServiceHealth() {
        try {
            boolean isHealthy = spaceAgentService.isAgenticServiceHealthy();
            Map<String, Object> response = new HashMap<>();
            response.put("healthy", isHealthy);
            response.put("service", "python-agentic-service");
            
            return new ResponseEntity<>(response, isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("healthy", false);
            response.put("error", e.getMessage());
            response.put("service", "python-agentic-service");
            
            return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Clear cache for a specific space
     */
    @PostMapping("/{spaceUuid}/clear-cache")
    public ResponseEntity<?> clearCache(@PathVariable String spaceUuid) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
            String userUid = firebaseAuth.getUid();

            // Verify user owns the space
            Space space = spaceRepository.findByUuidAndUser_FirebaseUid(spaceUuid, userUid)
                .orElseThrow(() -> new RuntimeException("Space not found or access denied"));

            spaceAgentService.clearRecommendationsCache(space.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache cleared successfully");
            response.put("spaceId", space.getId());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error clearing cache: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 