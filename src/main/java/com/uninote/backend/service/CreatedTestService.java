package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.CreatedTest;
import com.uninote.backend.repository.CreatedTestRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

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


    

    public Long closeTest(Long testId) {
        Optional<CreatedTest> testOptional = createdTestRepository.findById(testId);
        
        if (testOptional.isPresent()) {
            CreatedTest test = testOptional.get();
            
            test.setClosingTime(LocalDateTime.now());
            
           createdTestRepository.save(test);
            
            
            return test.getTestId();
        } else {
            throw new IllegalArgumentException("Test with ID " + testId + " not found");
        }
    }
}
