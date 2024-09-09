package com.uninote.backend.controller;

import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.interfaceProjection.TrueFalseQuestionProjection;
import com.uninote.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/true_false_questions")
public class TrueFalseQuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<TrueFalseQuestion> createTrueFalseQuestion(@RequestBody TrueFalseQuestionDTO trueFalseQuestionDTO) {
        TrueFalseQuestion createdTrueFalseQuestion = questionService.createTrueFalseQuestion(trueFalseQuestionDTO);
        return ResponseEntity.ok(createdTrueFalseQuestion);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TrueFalseQuestionProjection>> getTrueFalseQuestionsByCourseId(@PathVariable Long courseId, @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(questionService.getRandomTrueFalseQuestionsByCourseId(courseId, limit));
    }
}

