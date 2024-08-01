package com.uninote.backend.controller;

import com.uninote.backend.dto.AddChoicesRequestDTO;
import com.uninote.backend.dto.ChoiceDTO;
import com.uninote.backend.dto.CorrectChoiceRequestDTO;
import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.dto.MultipleChoiceQuestionDTO;
import com.uninote.backend.dto.QuestionDTO;
import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.entity.MultipleChoiceQuestion;
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
        flashcardDTO.setQuestionTypeId(1L);
        Flashcard createdFlashcard = questionService.createFlashcard(flashcardDTO);
        return ResponseEntity.ok(createdFlashcard);
    }

    @PostMapping("/true_false")
    public ResponseEntity<TrueFalseQuestion> createTrueFalseQuestion(@RequestBody TrueFalseQuestionDTO trueFalseQuestionDTO) {
        trueFalseQuestionDTO.setQuestionTypeId(2L);
        TrueFalseQuestion createdTrueFalseQuestion = questionService.createTrueFalseQuestion(trueFalseQuestionDTO);
        return ResponseEntity.ok(createdTrueFalseQuestion);
    }

    @PostMapping("/multiple_choice")
    public ResponseEntity<MultipleChoiceQuestion> createMultipleChoiceQuestion(@RequestBody MultipleChoiceQuestionDTO multipleChoiceQuestionDTO) {
        multipleChoiceQuestionDTO.setQuestionTypeId(3L);;
        MultipleChoiceQuestion createdMultipleChoiceQuestion = questionService.createMultipleChoiceQuestion(multipleChoiceQuestionDTO);
        return ResponseEntity.ok(createdMultipleChoiceQuestion);
    }

    @PostMapping("/multiple_choice/{id}/choices")
    public ResponseEntity<MultipleChoiceQuestion> addChoicesToMultipleChoiceQuestion(@PathVariable Long id, @RequestBody AddChoicesRequestDTO addChoicesRequest) {
        MultipleChoiceQuestion updatedMultipleChoiceQuestion = questionService.addChoicesToMultipleChoiceQuestion(id, addChoicesRequest.getChoices());
        return ResponseEntity.ok(updatedMultipleChoiceQuestion);
    }

    @PostMapping("/multiple_choice/{id}/correct_choice")
    public ResponseEntity<MultipleChoiceQuestion> setCorrectChoiceForMultipleChoiceQuestion(@PathVariable Long id, @RequestBody CorrectChoiceRequestDTO correctChoiceRequest) {
        MultipleChoiceQuestion updatedMultipleChoiceQuestion = questionService.setCorrectChoiceForMultipleChoiceQuestion(id, correctChoiceRequest.getCorrectChoiceLabel());
        return ResponseEntity.ok(updatedMultipleChoiceQuestion);
    }

    @GetMapping("/random_test")
    public ResponseEntity<?> getRandomTest(@RequestParam Long courseId, @RequestParam String type, @RequestParam int count) {
        if (type.equalsIgnoreCase("flashcard")) {
            List<FlashcardDTO> flashcards = questionService.getRandomFlashcardsByCourse(courseId, count);
            return ResponseEntity.ok(flashcards);
        } else if (type.equalsIgnoreCase("true_false")) {
            List<TrueFalseQuestionDTO> trueFalseQuestions = questionService.getRandomTrueFalseQuestionsByCourse(courseId, count);
            return ResponseEntity.ok(trueFalseQuestions);
        } else if (type.equalsIgnoreCase("multiple_choice")) {
            List<MultipleChoiceQuestionDTO> multipleChoiceQuestions = questionService.getRandomMultipleChoiceQuestionsByCourse(courseId, count);
            return ResponseEntity.ok(multipleChoiceQuestions);
        } else {
            return ResponseEntity.badRequest().body("Invalid test type specified");
        }
    }
}
