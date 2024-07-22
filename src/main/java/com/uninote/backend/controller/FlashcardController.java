package com.uninote.backend.controller;

import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.entity.Flashcard;
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
    public ResponseEntity<List<FlashcardDTO>> getFlashcardsByCourseId(@PathVariable Long courseId) {
        List<FlashcardDTO> flashcards = questionService.getFlashcardsByCourseId(courseId);
        return ResponseEntity.ok(flashcards);
    }
}
