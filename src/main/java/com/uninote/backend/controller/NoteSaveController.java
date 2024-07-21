package com.uninote.backend.controller;

import com.uninote.backend.service.NoteSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteSaveController {

    @Autowired
    private NoteSaveService noteSaveService;

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
}
