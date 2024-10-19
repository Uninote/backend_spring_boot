package com.uninote.backend.controller;

import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.interfaceProjection.FlashcardProjection;
import com.uninote.backend.service.FlashcardService;
import com.uninote.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flashcards")
public class FlashcardController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private FlashcardService flashcardService;

    @PostMapping
    public ResponseEntity<Flashcard> createFlashcard(@RequestBody FlashcardDTO flashcardDTO) {
        Flashcard createdFlashcard = questionService.createFlashcard(flashcardDTO);
        return ResponseEntity.ok(createdFlashcard);
    }

    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<FlashcardProjection>> getFlashcardsByCourseId(@PathVariable Long courseId, @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(questionService.getRandomFlashcardsByCourseId(courseId, limit));
    }

    @PutMapping("/{flashcardId}")
    public ResponseEntity<FlashcardDTO> updateFlashcard(
            @PathVariable Long flashcardId,
            @RequestBody FlashcardDTO updatedFlashcardDTO) {

        
        FlashcardDTO updatedFlashcard = flashcardService.updateFlashcard(flashcardId, updatedFlashcardDTO);

        
        return ResponseEntity.ok(updatedFlashcard);
    }
}
