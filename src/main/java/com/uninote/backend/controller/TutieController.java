package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import com.uninote.backend.service.TokenQuotaService;
import com.uninote.backend.service.TutieService;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/tutie")
public class TutieController {

    @Autowired
    private TutieService tutieService;

    @Autowired
    private TokenQuotaService tokenQuotaService;

    

    @GetMapping("/{noteId}/content")
    public ResponseEntity<Map<String, String>> getSummariesAndQuizzes(@PathVariable Long noteId) {
        try {
            Map<String, String> result = tutieService.getSummaryAndQuizzesByNoteId(noteId);

            if (result == null || result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Note content not found or cannot be digitized for ID: " + noteId));
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred while retrieving content for Note ID: " + noteId));
        }
    }


    
    @PostMapping("/prompt")
    public ResponseEntity<Map<String, Object>> getAnswerAndRelatedNotes(
            @RequestHeader("User-Id") Long userId,
            @RequestBody Map<String, String> requestBody) {
        String prompt = requestBody.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required."));
        }

        int tokensNeeded = tutieService.calculateTokens(prompt);

        if (tokensNeeded ==-1 || !tokenQuotaService.hasSufficientQuota(userId, tokensNeeded)) {
            return ResponseEntity.status(429).body(Map.of("error", "Daily token quota exceeded."));
        }

        Map<String, Object> response = tutieService.getAnswerAndRelatedNotes(prompt);

        if (response.containsKey("error")) {
            return ResponseEntity.internalServerError().body(response);
        }

        tokenQuotaService.updateTokenUsage(userId, tokensNeeded);

        return ResponseEntity.ok(response);
    }



    @PostMapping("/upload-note")
    public ResponseEntity<?> uploadNoteFile(@RequestParam("file") MultipartFile file) {
        try {
            String sessionId = tutieService.uploadNoteFile(file);
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "session_id", sessionId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestParam("session_id") String sessionId,
                                  @RequestParam("message") String message) {
        try {
            Map<String, Object> response = tutieService.handleChat(sessionId, message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
    
}

