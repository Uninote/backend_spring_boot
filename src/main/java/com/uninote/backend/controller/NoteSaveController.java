package com.uninote.backend.controller;

import com.uninote.backend.service.NoteSaveService;
import com.uninote.backend.service.UserSessionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteSaveController {

    @Autowired
    private NoteSaveService noteSaveService;

    @Autowired
    private UserSessionService userSessionService;

    @PostMapping("/{noteId}/save/{userId}")
    public ResponseEntity<Void> saveNoteSave(@PathVariable Long noteId, @PathVariable Long userId) {
        noteSaveService.saveNote(noteId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{noteId}/unsave/{userId}")
    public ResponseEntity<Void> deleteNoteSave(@PathVariable Long noteId, @PathVariable Long userId) {
        noteSaveService.unsaveNote(noteId, userId);
        return ResponseEntity.noContent().build();
    }


    
    @PostMapping("/{noteId}/toggle-save/{userId}")
    public ResponseEntity<String> toggleSave(@PathVariable Long noteId,
                                             @PathVariable Long userId,
                                             @RequestParam(required = false) Long sessionId) {
        if (sessionId == null || !userSessionService.isSessionValid(sessionId)) {
            sessionId = userSessionService.findLastSessionForUser(userId);
            if (sessionId == null) {
                return ResponseEntity.badRequest().body("No active session found for the user.");
            }
        }                                       
        noteSaveService.toggleSave(noteId, userId, sessionId);
        
        return ResponseEntity.ok("Toggled save successfully.");
    }
}
