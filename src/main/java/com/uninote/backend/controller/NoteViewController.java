package com.uninote.backend.controller;

import com.uninote.backend.entity.NoteView;
import com.uninote.backend.service.NoteViewService;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteViewController {

    @Autowired
    private NoteViewService noteViewService;

    @PostMapping("/{noteId}/view/{userId}")
    public ResponseEntity<String> trackView(@PathVariable Long noteId, @PathVariable Long userId) {
        CompletableFuture.runAsync(() -> {
            noteViewService.trackView(noteId, userId);
        });
        return ResponseEntity.accepted().body("View tracking initiated.");    
    }
}
