package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.CreatedTest;
import com.uninote.backend.repository.CreatedTestRepository;

import java.util.Date;

@Service
public class CreatedTestService {

    @Autowired
    private CreatedTestRepository createdTestRepository;

    public Long getTotalTestsCount() {
        return createdTestRepository.countTotalTests();
    }

    public Long getTestsCountByUser(Long userId) {
        return createdTestRepository.countTestsByUser(userId);
    }

    public Long getTestsCountByType(Long testTypeId) {
        return createdTestRepository.countTestsByType(testTypeId);
    }

    public Long getTestsCountByDateRange(Date start, Date end) {
        return createdTestRepository.countTestsByDateRange(start, end);
    }

    public Long getMostCommonTestType() {
        return createdTestRepository.findMostCommonTestType();
    }

    public void deleteTest(Long testId) {
        createdTestRepository.deleteById(testId);
    }
}
