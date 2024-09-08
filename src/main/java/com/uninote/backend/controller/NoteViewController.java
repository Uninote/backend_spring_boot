package com.uninote.backend.controller;

import com.uninote.backend.entity.NoteView;
import com.uninote.backend.service.NoteViewService;
import com.uninote.backend.service.UserService;
import com.uninote.backend.service.UserSessionService;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteViewController {

    @Autowired
    private NoteViewService noteViewService;

    @Autowired
    private UserSessionService userSessionService;

    @PostMapping("/{noteId}/view/{userId}")
    public ResponseEntity<Long> trackView(@PathVariable Long noteId, @PathVariable Long userId, @RequestParam(required = false) Long sessionId) {
        if (sessionId == null || !userSessionService.isSessionValid(sessionId)) {
            sessionId = userSessionService.findLastSessionForUser(userId);
            if (sessionId == null) {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }   
        Long noteViewId =  noteViewService.trackView(noteId, userId, sessionId);
    
        return ResponseEntity.ok(noteViewId);    
    }

    @PostMapping("/view/{noteViewId}/end")
    public ResponseEntity<String> trackViewEnd(@PathVariable Long noteViewId) {
        Long updatedNoteViewId = noteViewService.trackViewEnd(noteViewId);

        return ResponseEntity.ok("View ended for NoteView ID: " + updatedNoteViewId);
    }
}
