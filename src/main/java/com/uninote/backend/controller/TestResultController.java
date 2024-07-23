package com.uninote.backend.controller;

import com.uninote.backend.entity.TestResult;
import com.uninote.backend.service.TestResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test-results")
public class TestResultController {

    @Autowired
    private TestResultService testResultService;

    @PostMapping
    public ResponseEntity<TestResult> createTestResult(@RequestBody TestResult testResult) {
        TestResult createdTestResult = testResultService.saveTestResult(testResult);
        return ResponseEntity.ok(createdTestResult);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestResult> getTestResultById(@PathVariable Long id) {
        TestResult testResult = testResultService.getTestResultById(id);
        return ResponseEntity.ok(testResult);
    }

    @GetMapping
    public ResponseEntity<List<TestResult>> getAllTestResults() {
        List<TestResult> testResults = testResultService.getAllTestResults();
        return ResponseEntity.ok(testResults);
    }
}
