package com.uninote.backend.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uninote.backend.dto.QuestionDTO;
import com.uninote.backend.entity.QuestionReport;
import com.uninote.backend.repository.QuestionReportRepository;
import com.uninote.backend.service.QuestionService;

@RestController
@RequestMapping("/reports")
public class QuestionReportController {

    @Autowired
    private QuestionReportRepository incorrectQuestionReportRepository;

    @Autowired
    private QuestionService questionService;

    @PostMapping("/create")
    public ResponseEntity<QuestionReport> createReport(@RequestBody QuestionReport incorrectQuestionReport) {
        QuestionReport savedReport = incorrectQuestionReportRepository.save(incorrectQuestionReport);
        return new ResponseEntity<>(savedReport, HttpStatus.CREATED);
    }

     @GetMapping("/distinct")
    public List<QuestionDTO> getDistinctReportedQuestions() {
        return questionService.getDistinctReportedQuestionDTOs();
    }
}