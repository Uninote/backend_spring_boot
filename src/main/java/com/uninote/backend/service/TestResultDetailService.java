package com.uninote.backend.service;

import com.uninote.backend.entity.TestResultDetail;
import com.uninote.backend.repository.TestResultDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultDetailService {

    @Autowired
    private TestResultDetailRepository testResultDetailRepository;

    public TestResultDetail saveTestResultDetail(TestResultDetail testResultDetail) {
        return testResultDetailRepository.save(testResultDetail);
    }

    public TestResultDetail getTestResultDetailById(Long id) {
        return testResultDetailRepository.findById(id).orElse(null);
    }

    public List<TestResultDetail> getAllTestResultDetails() {
        return testResultDetailRepository.findAll();
    }
}
