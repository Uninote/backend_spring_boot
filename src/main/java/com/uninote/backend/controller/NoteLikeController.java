package com.uninote.backend.controller;

import com.uninote.backend.service.NoteLikeService;
import com.uninote.backend.service.UserSessionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteLikeController {

    @Autowired
    private NoteLikeService noteLikeService;

    @Autowired
    private UserSessionService userSessionService;

    @PostMapping("/{noteId}/like/{userId}")
    public ResponseEntity<Void> likeNote(@PathVariable Long noteId, @PathVariable Long userId) {
        noteLikeService.likeNote(noteId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{noteId}/unlike/{userId}")
    public ResponseEntity<Void> unlikeNote(@PathVariable Long noteId, @PathVariable Long userId) {
        noteLikeService.unlikeNote(noteId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{noteId}/toggle-like/{userId}")
    public ResponseEntity<String> toggleLike(@PathVariable Long noteId,
                                             @PathVariable Long userId,
                                             @RequestParam(required = false) Long sessionId) {
        if (sessionId == null || !userSessionService.isSessionValid(sessionId)) {
            sessionId = userSessionService.findLastSessionForUser(userId);
            if (sessionId == null) {
                return ResponseEntity.badRequest().body("No active session found for the user.");
            }
        }                                       
        noteLikeService.toggleLike(noteId, userId, sessionId);
        
        return ResponseEntity.ok("Toggled like successfully.");
    }
}
