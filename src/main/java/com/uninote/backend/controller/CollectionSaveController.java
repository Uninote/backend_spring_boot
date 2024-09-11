package com.uninote.backend.controller;



import com.uninote.backend.service.CollectionSaveService;
import com.uninote.backend.service.NoteCollectionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collections")
public class CollectionSaveController {
    

    private final CollectionSaveService collectionSaveService;


    @Autowired
    private NoteCollectionService noteCollectionService;

    @Autowired
    public CollectionSaveController(CollectionSaveService collectionSaveService) {
        this.collectionSaveService = collectionSaveService;
    }

    @PostMapping("/{collectionId}/save/{userId}")
    public ResponseEntity<?> saveCollection(
            @PathVariable Long collectionId,
            @PathVariable Long userId) {
        collectionSaveService.saveCollection(collectionId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{collectionId}/unsave/{userId}")
    public ResponseEntity<?> unsaveCollection(
            @PathVariable Long collectionId,
            @PathVariable Long userId) {
        collectionSaveService.unsaveCollection(collectionId, userId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/toggle-save/{userId}/{collectionId}")
    public ResponseEntity<String> toggleSaveCollection(@PathVariable Long collectionId, @PathVariable Long userId) {
        boolean isSaved = noteCollectionService.hasUserSaved(collectionId, userId);

        if (isSaved) {
            collectionSaveService.unsaveCollection(collectionId, userId);
            return ResponseEntity.ok("Collection unsaved successfully.");
        } else {
            collectionSaveService.saveCollection(collectionId, userId);
            return ResponseEntity.ok("Collection saved successfully.");
        }
    }
}
       
