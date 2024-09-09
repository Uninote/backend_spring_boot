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
import com.uninote.backend.repository.MultipleChoiceQuestionRepository;
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

    @Autowired
    private MultipleChoiceQuestionRepository multipleChoiceQuestionRepository;

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
        multipleChoiceQuestionDTO.setQuestionTypeId(3L);
        MultipleChoiceQuestion createdMultipleChoiceQuestion = questionService.createMultipleChoiceQuestion(multipleChoiceQuestionDTO);
        //createdMultipleChoiceQuestion = questionService.addChoicesMultipleChoice(createdMultipleChoiceQuestion,multipleChoiceQuestionDTO.getChoices());
        return ResponseEntity.ok(createdMultipleChoiceQuestion);
    }

    @PostMapping("/multiple_choice/choices")
    public ResponseEntity<MultipleChoiceQuestion> addChoicesMultipleChoice(@RequestBody MultipleChoiceQuestionDTO multipleChoiceQuestionDTO) {
        MultipleChoiceQuestion createdMultipleChoiceQuestion = multipleChoiceQuestionRepository.findById(multipleChoiceQuestionDTO.getId())
            .orElseThrow(() -> new IllegalArgumentException("Incorrect Question Id"));
        createdMultipleChoiceQuestion = questionService.addChoicesMultipleChoice(createdMultipleChoiceQuestion,multipleChoiceQuestionDTO.getChoices());
        return ResponseEntity.ok(createdMultipleChoiceQuestion);
    }

   

    @PostMapping("/multiple_choice/{id}/correct_choice")
    public ResponseEntity<MultipleChoiceQuestion> setCorrectChoiceForMultipleChoiceQuestion(@PathVariable Long id, @RequestBody CorrectChoiceRequestDTO correctChoiceRequest) {
        MultipleChoiceQuestion updatedMultipleChoiceQuestion = questionService.setCorrectChoiceForMultipleChoiceQuestion(id, correctChoiceRequest.getCorrectChoiceLabel());
        return ResponseEntity.ok(updatedMultipleChoiceQuestion);
    }


    @PostMapping("/multiple_choice/full")
public ResponseEntity<MultipleChoiceQuestion> createFullMultipleChoiceQuestion(@RequestBody MultipleChoiceQuestionDTO multipleChoiceQuestionDTO) {
    // Step 1: Create the multiple-choice question
    
    MultipleChoiceQuestion createdMultipleChoiceQuestion = questionService.fullMultipleChoiceQuestionCreation(multipleChoiceQuestionDTO);
    
    
    
    
    return ResponseEntity.ok(createdMultipleChoiceQuestion);
}


    
}
