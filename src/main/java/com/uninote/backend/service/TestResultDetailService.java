package com.uninote.backend.service;

import com.uninote.backend.dto.TestResultDetailDTO;
import com.uninote.backend.entity.TestResultDetail;
import com.uninote.backend.repository.QuestionRepository;
import com.uninote.backend.repository.TestResultDetailRepository;
import com.uninote.backend.repository.TestResultRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultDetailService {

    @Autowired
    private TestResultDetailRepository testResultDetailRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private QuestionRepository questionRepository;


    public TestResultDetail saveTestResultDetail(TestResultDetailDTO testResultDetailDTO) {
        TestResultDetail testResultDetail = new TestResultDetail();
        testResultDetail.setTestResult(testResultRepository.findById(testResultDetailDTO.getTestResultId()).orElse(null));
        testResultDetail.setQuestion(questionRepository.findById(testResultDetailDTO.getQuestionId()).orElse(null));
        testResultDetail.setCorrect(testResultDetailDTO.isCorrect());
        return testResultDetailRepository.save(testResultDetail);
    }
    public TestResultDetail getTestResultDetailById(Long id) {
        return testResultDetailRepository.findById(id).orElse(null);
    }

    public List<TestResultDetail> getAllTestResultDetails() {
        return testResultDetailRepository.findAll();
    }
}
