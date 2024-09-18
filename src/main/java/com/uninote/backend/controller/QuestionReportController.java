package com.uninote.backend.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.dto.MultipleChoiceQuestionDTO;
import com.uninote.backend.dto.QuestionDTO;
import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.entity.QuestionReport;
import com.uninote.backend.repository.QuestionReportRepository;
import com.uninote.backend.service.FlashcardService;
import com.uninote.backend.service.MultipleChoiceQuestionService;
import com.uninote.backend.service.QuestionService;
import com.uninote.backend.service.TrueFalseQuestionService;

import oracle.net.aso.f;

@RestController
@RequestMapping("/reports")
public class QuestionReportController {

    @Autowired
    private QuestionReportRepository incorrectQuestionReportRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private TrueFalseQuestionService trueFalseQuestionService;

    @Autowired
    private MultipleChoiceQuestionService multipleChoiceQuestionService;

    @PostMapping("/create")
    public ResponseEntity<QuestionReport> createReport(@RequestBody QuestionReport incorrectQuestionReport) {
        QuestionReport savedReport = incorrectQuestionReportRepository.save(incorrectQuestionReport);
        return new ResponseEntity<>(savedReport, HttpStatus.CREATED);
    }

     @GetMapping("/distinct")
    public List<QuestionDTO> getDistinctReportedQuestions() {
        return questionService.findDistinctReportedQuestionDTOs();
    }

    @GetMapping("/flashcards")
    public ResponseEntity<List<FlashcardDTO>> getAllReportedFlashcards() {
        List<FlashcardDTO> reportedQuestions = flashcardService.findReportedFlashcards();
        return ResponseEntity.ok(reportedQuestions);
    }

    @GetMapping("/true-false")
    public ResponseEntity<List<TrueFalseQuestionDTO>> getAllReportedTrueFalseQuestions() {
        List<TrueFalseQuestionDTO> reportedQuestions = trueFalseQuestionService.findReportedTrueFalseQuestions();
        return ResponseEntity.ok(reportedQuestions);
    }

    @GetMapping("/multiple-choice")
    public ResponseEntity<List<MultipleChoiceQuestionDTO>> getAllReportedMultipleChoiceQuestions() {
        List<MultipleChoiceQuestionDTO> reportedQuestions = multipleChoiceQuestionService.findReportedMultipleChoiceQuestions();
        return ResponseEntity.ok(reportedQuestions);
    }
}