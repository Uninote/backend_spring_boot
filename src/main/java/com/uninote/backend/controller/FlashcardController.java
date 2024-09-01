package com.uninote.backend.controller;

import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.interfaceProjection.FlashcardProjection;
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

    @PostMapping
    public ResponseEntity<Flashcard> createFlashcard(@RequestBody FlashcardDTO flashcardDTO) {
        Flashcard createdFlashcard = questionService.createFlashcard(flashcardDTO);
        return ResponseEntity.ok(createdFlashcard);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<FlashcardProjection>> getFlashcardsByCourseId(@PathVariable Long courseId, @RequestParam("limit") int limit) {
        return ResponseEntity.ok(questionService.getRandomFlashcardsByCourseId(courseId, limit));
    }
}
