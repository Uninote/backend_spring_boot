package com.uninote.backend.controller;

import com.uninote.backend.dto.NoteCollectionDTO;
import com.uninote.backend.entity.NoteCollection;
import com.uninote.backend.entity.NoteCollectionItem;
import com.uninote.backend.service.NoteCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/collections")
public class NoteCollectionController {

    @Autowired
    private NoteCollectionService collectionService; 
    @Autowired
    private NoteCollectionService noteCollectionService;

        @PostMapping("/create")
    public ResponseEntity<NoteCollection> createCollection(@RequestParam Long userId, @RequestParam String name, @RequestParam String description, @RequestParam Boolean isPublic) {
        NoteCollection collection = noteCollectionService.createCollection(userId, name, description, isPublic);
        return ResponseEntity.ok(collection);
    }

    @PostMapping("/{collectionId}/addNote/{noteId}")
    public ResponseEntity<NoteCollection> addNoteToCollection(@PathVariable Long collectionId, @PathVariable Long noteId) {
        NoteCollection collection = noteCollectionService.addNoteToCollection(collectionId, noteId);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteCollection>> getCollectionsByUser(@PathVariable Long userId) {
        List<NoteCollection> collections = noteCollectionService.getCollectionsByUser(userId);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/{collectionId}/notes")
    public ResponseEntity<List<NoteCollectionItem>> getNotesInCollection(@PathVariable Long collectionId) {
        List<NoteCollectionItem> items = noteCollectionService.getNotesInCollection(collectionId);
        return ResponseEntity.ok(items);
    }
    @DeleteMapping("/{collectionId}/removeNote/{noteId}")
    public ResponseEntity<NoteCollection> removeNoteFromCollection(
            @PathVariable Long collectionId,
            @PathVariable Long noteId) {
        NoteCollection collection = noteCollectionService.removeNoteFromCollection(collectionId, noteId);
        return ResponseEntity.ok(collection);
    }

     @GetMapping("/public")
    public ResponseEntity<List<NoteCollectionDTO>> getPublicNoteCollections() {
        List<NoteCollectionDTO> publicNoteCollections = noteCollectionService.getPublicNoteCollections();
        return ResponseEntity.ok(publicNoteCollections);
    }

    @GetMapping("/{collectionId}/likes/user/{userId}")
    public ResponseEntity<Boolean> hasUserLiked(@PathVariable Long collectionId, @PathVariable Long userId) {
        boolean hasLiked = collectionService.hasUserLiked(collectionId, userId);
        return ResponseEntity.ok(hasLiked);
    }
    @GetMapping("/{collectionId}/saves/user/{userId}")
    public ResponseEntity<Boolean> hasUserSaved(@PathVariable Long collectionId, @PathVariable Long userId) {
        boolean hasSaved = collectionService.hasUserSaved(collectionId, userId);
        return ResponseEntity.ok(hasSaved);
    }
    }
