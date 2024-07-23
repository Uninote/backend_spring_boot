package com.uninote.backend.service;

import com.uninote.backend.entity.TestResult;
import com.uninote.backend.repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultService {

    @Autowired
    private TestResultRepository testResultRepository;

    public TestResult saveTestResult(TestResult testResult) {
        return testResultRepository.save(testResult);
    }

    public TestResult getTestResultById(Long id) {
        return testResultRepository.findById(id).orElse(null);
    }

    public List<TestResult> getAllTestResults() {
        return testResultRepository.findAll();
    }
}
