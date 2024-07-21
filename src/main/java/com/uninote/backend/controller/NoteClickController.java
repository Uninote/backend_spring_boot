package com.uninote.backend.controller;

import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.service.NoteClickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteClickController {

    @Autowired
    private NoteClickService noteClickService;

    @PostMapping("/{noteId}/click/{userId}")
    public ResponseEntity<NoteClick> trackClick(@PathVariable Long noteId, @PathVariable Long userId) {
        NoteClick noteClick = noteClickService.trackClick(noteId, userId);
        return ResponseEntity.ok(noteClick);
    }
}
