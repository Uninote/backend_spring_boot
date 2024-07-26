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

    @PutMapping("/{id}")
    public ResponseEntity<MultipleChoiceQuestion> updateMultipleChoiceQuestion(@PathVariable Long id, @RequestBody MultipleChoiceQuestion multipleChoiceQuestion) {
        if (!multipleChoiceQuestionService.getMultipleChoiceQuestionById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        multipleChoiceQuestion.setId(id);
        MultipleChoiceQuestion updatedMultipleChoiceQuestion = multipleChoiceQuestionService.saveMultipleChoiceQuestion(multipleChoiceQuestion);
        return ResponseEntity.ok(updatedMultipleChoiceQuestion);
    }

    @GetMapping("/multiple_choice/course/{courseId}")
    public ResponseEntity<List<MultipleChoiceQuestionDTO>> getMultipleChoiceQuestionsByCourseId(@PathVariable Long courseId) {
        List<MultipleChoiceQuestionDTO> multipleChoiceQuestions = questionService.getMultipleChoiceQuestionsByCourseId(courseId);
        return ResponseEntity.ok(multipleChoiceQuestions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMultipleChoiceQuestion(@PathVariable Long id) {
        if (!multipleChoiceQuestionService.getMultipleChoiceQuestionById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        multipleChoiceQuestionService.deleteMultipleChoiceQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
