package com.uninote.backend.controller;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.TestResultDTO;
import com.uninote.backend.dto.TestResultDetailDTO;
import com.uninote.backend.entity.TestResult;
import com.uninote.backend.entity.TestResultDetail;
import com.uninote.backend.service.TestResultDetailService;
import com.uninote.backend.service.TestResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test-results")
public class TestResultController {

     @Autowired
    private TestResultService testResultService;

    @PostMapping
    public ResponseEntity<TestResultDTO> createTestResult(@RequestBody TestResultDTO testResultDTO) {
        TestResult createdTestResult = testResultService.saveTestResult(testResultDTO);
        return ResponseEntity.ok(EntityToDTOConverter.convertTestResultToDto(createdTestResult));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestResultDTO> getTestResultById(@PathVariable Long id) {
        TestResult testResult = testResultService.getTestResultById(id);
        return ResponseEntity.ok(EntityToDTOConverter.convertTestResultToDto(testResult));
    }

    @GetMapping
    public ResponseEntity<List<TestResultDTO>> getAllTestResults() {
        List<TestResult> testResults = testResultService.getAllTestResults();
        List<TestResultDTO> testResultDTOs = testResults.stream().map(EntityToDTOConverter::convertTestResultToDto).collect(Collectors.toList());
        return ResponseEntity.ok(testResultDTOs);
    }
}

   
