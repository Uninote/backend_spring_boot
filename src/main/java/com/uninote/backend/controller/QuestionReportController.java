package com.uninote.backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uninote.backend.entity.QuestionReport;
import com.uninote.backend.repository.QuestionReportRepository;

@RestController
@RequestMapping("/reports")
public class QuestionReportController {

    @Autowired
    private QuestionReportRepository incorrectQuestionReportRepository;

    @PostMapping("/create")
    public ResponseEntity<QuestionReport> createReport(@RequestBody QuestionReport incorrectQuestionReport) {
        QuestionReport savedReport = incorrectQuestionReportRepository.save(incorrectQuestionReport);
        return new ResponseEntity<>(savedReport, HttpStatus.CREATED);
    }
}