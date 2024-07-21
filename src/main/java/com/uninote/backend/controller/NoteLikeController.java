package com.uninote.backend.controller;

import com.uninote.backend.service.NoteLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteLikeController {

    @Autowired
    private NoteLikeService noteLikeService;

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
}
