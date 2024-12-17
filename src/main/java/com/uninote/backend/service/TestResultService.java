package com.uninote.backend.service;

import com.uninote.backend.dto.TestResultDTO;
import com.uninote.backend.entity.TestResult;
import com.uninote.backend.repository.TestRepository;
import com.uninote.backend.repository.TestResultRepository;
import com.uninote.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultService {

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private UserRepository userRepository;

    public TestResult saveTestResult(TestResultDTO testResultDTO) {
        TestResult testResult = new TestResult();
        testResult.setTest(testRepository.findById(testResultDTO.getTestId()).orElse(null));
        testResult.setScore(testResultDTO.getScore());
        return testResultRepository.save(testResult);
    }

    public TestResult getTestResultById(Long id) {
        return testResultRepository.findById(id).orElse(null);
    }

    public List<TestResult> getAllTestResults() {
        return testResultRepository.findAll();
    }
}
