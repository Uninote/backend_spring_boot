package com.uninote.backend.controller;

import com.uninote.backend.dto.MultipleChoiceQuestionDTO;
import com.uninote.backend.entity.MultipleChoiceQuestion;
import com.uninote.backend.service.MultipleChoiceQuestionService;
import com.uninote.backend.service.QuestionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/multipleChoiceQuestions")
public class MultipleChoiceQuestionController {

    @Autowired
    private MultipleChoiceQuestionService multipleChoiceQuestionService;

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public List<MultipleChoiceQuestion> getAllMultipleChoiceQuestions() {
        return multipleChoiceQuestionService.getAllMultipleChoiceQuestions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MultipleChoiceQuestion> getMultipleChoiceQuestionById(@PathVariable Long id) {
        Optional<MultipleChoiceQuestion> multipleChoiceQuestion = multipleChoiceQuestionService.getMultipleChoiceQuestionById(id);
        return multipleChoiceQuestion.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public MultipleChoiceQuestion createMultipleChoiceQuestion(@RequestBody MultipleChoiceQuestion multipleChoiceQuestion) {
        return multipleChoiceQuestionService.saveMultipleChoiceQuestion(multipleChoiceQuestion);
    }

    

    @GetMapping("/multiple_choice/course/{courseId}")
    public ResponseEntity<List<MultipleChoiceQuestionDTO>> getMultipleChoiceQuestionsByCourseId(@PathVariable Long courseId,  @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(questionService.getRandomMultipleChoiceQuestions(courseId, limit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMultipleChoiceQuestion(@PathVariable Long id) {
        if (!multipleChoiceQuestionService.getMultipleChoiceQuestionById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        multipleChoiceQuestionService.deleteMultipleChoiceQuestion(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{questionId}")
    public ResponseEntity<MultipleChoiceQuestionDTO> updateMultipleChoiceQuestion(
            @PathVariable Long questionId,
            @RequestBody MultipleChoiceQuestionDTO updatedQuestionDTO) {

        MultipleChoiceQuestionDTO updatedQuestion = multipleChoiceQuestionService.updateMultipleChoiceQuestion(questionId, updatedQuestionDTO);

        return ResponseEntity.ok(updatedQuestion);
    }

}
