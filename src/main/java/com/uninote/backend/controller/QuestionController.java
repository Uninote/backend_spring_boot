package com.uninote.backend.controller;

import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.dto.QuestionDTO;
import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.entity.Question;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByCourseId(@PathVariable Long courseId) {
        List<QuestionDTO> questions = questionService.getQuestionsByCourseId(courseId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestBody QuestionDTO questionDTO) {
        Question createdQuestion = questionService.createQuestion(questionDTO);
        return ResponseEntity.ok(createdQuestion);
    }

    @PostMapping("/flashcard")
    public ResponseEntity<Flashcard> createFlashcard(@RequestBody FlashcardDTO flashcardDTO) {
        Flashcard createdFlashcard = questionService.createFlashcard(flashcardDTO);
        return ResponseEntity.ok(createdFlashcard);
    }

    @PostMapping("/true_false")
    public ResponseEntity<TrueFalseQuestion> createTrueFalseQuestion(@RequestBody TrueFalseQuestionDTO trueFalseQuestionDTO) {
        TrueFalseQuestion createdTrueFalseQuestion = questionService.createTrueFalseQuestion(trueFalseQuestionDTO);
        return ResponseEntity.ok(createdTrueFalseQuestion);
    }
}
