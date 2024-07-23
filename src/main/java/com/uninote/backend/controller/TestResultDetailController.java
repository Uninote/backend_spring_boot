package com.uninote.backend.controller;

import com.uninote.backend.entity.TestResultDetail;
import com.uninote.backend.service.TestResultDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-result-details")
public class TestResultDetailController {

    @Autowired
    private TestResultDetailService testResultDetailService;

    @PostMapping
    public ResponseEntity<TestResultDetail> createTestResultDetail(@RequestBody TestResultDetail testResultDetail) {
        TestResultDetail createdTestResultDetail = testResultDetailService.saveTestResultDetail(testResultDetail);
        return ResponseEntity.ok(createdTestResultDetail);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestResultDetail> getTestResultDetailById(@PathVariable Long id) {
        TestResultDetail testResultDetail = testResultDetailService.getTestResultDetailById(id);
        return ResponseEntity.ok(testResultDetail);
    }

    @GetMapping
    public ResponseEntity<List<TestResultDetail>> getAllTestResultDetails() {
        List<TestResultDetail> testResultDetails = testResultDetailService.getAllTestResultDetails();
        return ResponseEntity.ok(testResultDetails);
    }
}
