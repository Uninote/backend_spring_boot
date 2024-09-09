package com.uninote.backend.controller;

import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.service.NoteClickService;
import com.uninote.backend.service.UserSessionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteClickController {

    @Autowired
    private NoteClickService noteClickService;

    @Autowired
    private UserSessionService userSessionService;

    @PostMapping("/{noteId}/click/{userId}")
    public ResponseEntity<String> trackClick(@PathVariable Long noteId, @PathVariable Long userId, @RequestParam(required = false) Long sessionId) {
        if (sessionId == null || !userSessionService.isSessionValid(sessionId)) {
            sessionId = userSessionService.findLastSessionForUser(userId);
            if (sessionId == null) {
                return ResponseEntity.badRequest().body("No active session found for the user.");
            }
        }  
        noteClickService.trackClick(noteId, userId, sessionId);
        return ResponseEntity.ok("Recorded Click");
    }
}
